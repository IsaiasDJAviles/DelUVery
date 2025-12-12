package com.example.deluvery.activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.deluvery.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EscanearQRActivity extends AppCompatActivity {

    private static final String TAG = "EscanearQR";
    private static final int CAMERA_PERMISSION_REQUEST = 1002;

    private DecoratedBarcodeView barcodeView;
    private TextView tvInstrucciones;
    private TextView tvPedidoInfo;
    private ProgressBar progressBar;
    private Button btnCancelar;

    private String pedidoId;
    private String codigoEsperado;
    private boolean escaneando = true;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_escanear_qr);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Confirmar Entrega");
        }

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        pedidoId = getIntent().getStringExtra("pedidoId");
        codigoEsperado = getIntent().getStringExtra("codigoQR");

        if (pedidoId == null) {
            Toast.makeText(this, "Error: No se recibio el ID del pedido", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        inicializarVistas();
        verificarPermisos();
    }

    private void inicializarVistas() {
        barcodeView = findViewById(R.id.barcode_scanner);
        tvInstrucciones = findViewById(R.id.tv_instrucciones);
        tvPedidoInfo = findViewById(R.id.tv_pedido_info);
        progressBar = findViewById(R.id.progress_bar_scan);
        btnCancelar = findViewById(R.id.btn_cancelar_scan);

        tvPedidoInfo.setText("Pedido #" + pedidoId);

        btnCancelar.setOnClickListener(v -> finish());

        barcodeView.decodeContinuous(new BarcodeCallback() {
            @Override
            public void barcodeResult(BarcodeResult result) {
                if (result != null && escaneando) {
                    escaneando = false;
                    procesarCodigoEscaneado(result.getText());
                }
            }

            @Override
            public void possibleResultPoints(List resultPoints) {
                // No necesario
            }
        });
    }

    private void verificarPermisos() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    CAMERA_PERMISSION_REQUEST);
        } else {
            iniciarEscaneo();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                iniciarEscaneo();
            } else {
                Toast.makeText(this, "Se necesita permiso de camara para escanear",
                        Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    private void iniciarEscaneo() {
        barcodeView.resume();
        tvInstrucciones.setText("Apunta la camara al codigo QR del repartidor");
    }

    private void procesarCodigoEscaneado(String codigoEscaneado) {
        barcodeView.pause();
        progressBar.setVisibility(View.VISIBLE);
        tvInstrucciones.setText("Verificando codigo...");

        // Verificar el codigo contra Firebase
        db.collection("pedidos")
                .document(pedidoId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String codigoEnFirebase = documentSnapshot.getString("codigoQR");

                        if (codigoEnFirebase != null && codigoEnFirebase.equals(codigoEscaneado)) {
                            confirmarEntrega();
                        } else {
                            mostrarErrorCodigo();
                        }
                    } else {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(this, "Pedido no encontrado", Toast.LENGTH_SHORT).show();
                        reintentarEscaneo();
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Error de conexion: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error verificando codigo", e);
                    reintentarEscaneo();
                });
    }

    private void confirmarEntrega() {
        Map<String, Object> updates = new HashMap<>();
        updates.put("estado", "entregado");
        updates.put("qrValidado", true);
        updates.put("fechaEntrega", new Date());
        updates.put("confirmadoPor", mAuth.getCurrentUser() != null ?
                mAuth.getCurrentUser().getUid() : "cliente");

        db.collection("pedidos")
                .document(pedidoId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    progressBar.setVisibility(View.GONE);
                    mostrarExito();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Error al confirmar: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error confirmando entrega", e);
                    reintentarEscaneo();
                });
    }

    private void mostrarExito() {
        new AlertDialog.Builder(this)
                .setTitle("Entrega Confirmada")
                .setMessage("Has confirmado la recepcion de tu pedido exitosamente. " +
                        "Gracias por usar DelUVery.")
                .setPositiveButton("Aceptar", (dialog, which) -> {
                    setResult(RESULT_OK);
                    finish();
                })
                .setCancelable(false)
                .show();
    }

    private void mostrarErrorCodigo() {
        progressBar.setVisibility(View.GONE);

        new AlertDialog.Builder(this)
                .setTitle("Codigo Invalido")
                .setMessage("El codigo QR escaneado no corresponde a este pedido. " +
                        "Por favor, verifica con el repartidor.")
                .setPositiveButton("Reintentar", (dialog, which) -> reintentarEscaneo())
                .setNegativeButton("Cancelar", (dialog, which) -> finish())
                .show();
    }

    private void reintentarEscaneo() {
        escaneando = true;
        barcodeView.resume();
        tvInstrucciones.setText("Apunta la camara al codigo QR del repartidor");
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            barcodeView.resume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        barcodeView.pause();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}