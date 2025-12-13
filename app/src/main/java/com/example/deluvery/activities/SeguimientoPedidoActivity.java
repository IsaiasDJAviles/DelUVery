package com.example.deluvery.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.deluvery.R;
import com.example.deluvery.models.Pedido;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.Locale;

/**
 * Activity para que el cliente pueda:
 * 1. Ver el estado de su pedido en tiempo real
 * 2. Ver la distancia del repartidor (cuando esta en camino)
 * 3. Escanear el codigo QR cuando el repartidor llegue
 */
public class SeguimientoPedidoActivity extends AppCompatActivity {

    private static final String TAG = "SeguimientoPedido";
    private static final int REQUEST_ESCANEAR_QR = 1001;

    // Views - Header
    private TextView tvDistancia;
    private TextView tvEstadoPedido;

    // Views - Detalles entrega
    private TextView tvDireccionEntrega;
    private TextView tvPedidoId;
    private TextView tvTotalPedido;

    // Views - Detalles pedido
    private TextView tvArticulosPedido;
    private TextView tvAnotaciones;
    private LinearLayout layoutAnotaciones;

    // Views - Acciones
    private Button btnEscanearQR;
    private Button btnCancelarPedido;
    private ProgressBar progressBar;

    // Cards
    private CardView cardDetallesEntrega;
    private CardView cardDetallesPedido;
    private LinearLayout layoutEscanearQR;

    // Firebase
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private ListenerRegistration pedidoListener;

    // Data
    private String pedidoId;
    private Pedido pedido;
    private double miLat = 0.0;
    private double miLng = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seguimiento_pedido);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Seguimiento de Pedido");
        }

        // Inicializar Firebase
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Obtener datos del intent
        pedidoId = getIntent().getStringExtra("pedidoId");
        miLat = getIntent().getDoubleExtra("miLat", 0.0);
        miLng = getIntent().getDoubleExtra("miLng", 0.0);

        if (pedidoId == null) {
            Toast.makeText(this, "Error: No se recibio el ID del pedido", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        inicializarVistas();
        configurarBotones();
        iniciarEscuchaPedido();
    }

    private void inicializarVistas() {
        // Header
        tvDistancia = findViewById(R.id.tv_distancia_repartidor);
        tvEstadoPedido = findViewById(R.id.tv_estado_pedido_cliente);

        // Detalles entrega
        tvDireccionEntrega = findViewById(R.id.tv_direccion_entrega_cliente);
        tvPedidoId = findViewById(R.id.tv_pedido_id_cliente);
        tvTotalPedido = findViewById(R.id.tv_total_pedido_cliente);

        // Detalles pedido
        tvArticulosPedido = findViewById(R.id.tv_articulos_pedido_cliente);
        tvAnotaciones = findViewById(R.id.tv_anotaciones_cliente);
        layoutAnotaciones = findViewById(R.id.layout_anotaciones_cliente);

        // Acciones
        btnEscanearQR = findViewById(R.id.btn_escanear_qr);
        btnCancelarPedido = findViewById(R.id.btn_cancelar_pedido_cliente);
        progressBar = findViewById(R.id.progress_bar_seguimiento);

        // Cards
        cardDetallesEntrega = findViewById(R.id.card_detalles_entrega_cliente);
        cardDetallesPedido = findViewById(R.id.card_detalles_pedido_cliente);
        layoutEscanearQR = findViewById(R.id.layout_escanear_qr);

        // Estado inicial
        btnEscanearQR.setVisibility(View.GONE);
        layoutEscanearQR.setVisibility(View.GONE);
    }

    private void configurarBotones() {
        btnEscanearQR.setOnClickListener(v -> abrirEscanerQR());

        btnCancelarPedido.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Cancelar pedido")
                    .setMessage("¿Estas seguro de que deseas cancelar este pedido?")
                    .setPositiveButton("Si, cancelar", (dialog, which) -> cancelarPedido())
                    .setNegativeButton("No", null)
                    .show();
        });
    }

    /**
     * Escucha cambios en tiempo real del pedido
     */
    private void iniciarEscuchaPedido() {
        progressBar.setVisibility(View.VISIBLE);

        pedidoListener = db.collection("pedidos")
                .document(pedidoId)
                .addSnapshotListener((snapshot, error) -> {
                    progressBar.setVisibility(View.GONE);

                    if (error != null) {
                        Log.e(TAG, "Error escuchando pedido", error);
                        Toast.makeText(this, "Error de conexion", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (snapshot != null && snapshot.exists()) {
                        pedido = snapshot.toObject(Pedido.class);
                        if (pedido != null) {
                            pedido.setId(snapshot.getId());
                            actualizarUI(snapshot);
                        }
                    } else {
                        Toast.makeText(this, "Pedido no encontrado", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
    }

    /**
     * Actualiza la UI segun el estado del pedido
     */
    private void actualizarUI(DocumentSnapshot snapshot) {
        // Datos basicos
        tvPedidoId.setText("Pedido #" + pedido.getId());
        tvTotalPedido.setText(String.format(Locale.getDefault(), "$%.2f", pedido.getTotal()));
        tvDireccionEntrega.setText(pedido.getSalonEntrega());

        // Estado del pedido
        String estado = pedido.getEstado();
        actualizarEstadoUI(estado);

        // Calcular distancia si el repartidor esta en camino
        Double repartidorLat = snapshot.getDouble("repartidorLat");
        Double repartidorLng = snapshot.getDouble("repartidorLng");

        if (repartidorLat != null && repartidorLng != null &&
                repartidorLat != 0.0 && repartidorLng != 0.0) {
            calcularYMostrarDistancia(repartidorLat, repartidorLng);
        } else {
            tvDistancia.setText("Esperando ubicacion...");
        }

        // Verificar si el QR esta disponible
        String codigoQR = snapshot.getString("codigoQR");
        if (codigoQR != null && !codigoQR.isEmpty() &&
                ("esperando_confirmacion".equals(estado) || "en_camino".equals(estado))) {
            mostrarBotonEscanearQR();
        }

        // Si el pedido ya fue entregado
        if ("entregado".equals(estado)) {
            mostrarPedidoEntregado();
        }

        // Cargar articulos
        cargarArticulosPedido();
    }

    private void actualizarEstadoUI(String estado) {
        String textoEstado;
        int colorFondo;

        switch (estado.toLowerCase()) {
            case "pendiente":
                textoEstado = "BUSCANDO REPARTIDOR";
                btnCancelarPedido.setVisibility(View.VISIBLE);
                break;
            case "asignado":
                textoEstado = "REPARTIDOR ASIGNADO";
                btnCancelarPedido.setVisibility(View.VISIBLE);
                break;
            case "en_camino":
                textoEstado = "EN CAMINO";
                btnCancelarPedido.setVisibility(View.GONE);
                break;
            case "esperando_confirmacion":
                textoEstado = "REPARTIDOR HA LLEGADO";
                btnCancelarPedido.setVisibility(View.GONE);
                break;
            case "entregado":
                textoEstado = "ENTREGADO";
                btnCancelarPedido.setVisibility(View.GONE);
                break;
            case "cancelado":
                textoEstado = "CANCELADO";
                btnCancelarPedido.setVisibility(View.GONE);
                break;
            default:
                textoEstado = estado.toUpperCase();
                break;
        }

        tvEstadoPedido.setText(textoEstado);
    }

    private void calcularYMostrarDistancia(double repartidorLat, double repartidorLng) {
        if (miLat == 0.0 && miLng == 0.0) {
            // Usar coordenadas del pedido si no tenemos las del cliente
            miLat = pedido.getLat();
            miLng = pedido.getLng();
        }

        if (miLat == 0.0 && miLng == 0.0) {
            tvDistancia.setText("Ubicacion no disponible");
            return;
        }

        // Calcular distancia usando la formula de Haversine simplificada
        float[] results = new float[1];
        android.location.Location.distanceBetween(miLat, miLng, repartidorLat, repartidorLng, results);
        float distanciaMetros = results[0];

        // Mostrar distancia
        if (distanciaMetros >= 1000) {
            tvDistancia.setText(String.format(Locale.getDefault(), "A %.1f km de ti", distanciaMetros / 1000));
        } else {
            tvDistancia.setText(String.format(Locale.getDefault(), "A %.0f metros de ti", distanciaMetros));
        }

        // Si esta muy cerca, mostrar boton de escanear
        if (distanciaMetros <= 100) {
            mostrarBotonEscanearQR();
        }
    }

    private void mostrarBotonEscanearQR() {
        layoutEscanearQR.setVisibility(View.VISIBLE);
        btnEscanearQR.setVisibility(View.VISIBLE);
        btnEscanearQR.setEnabled(true);
    }

    private void mostrarPedidoEntregado() {
        tvEstadoPedido.setText("ENTREGADO");
        tvDistancia.setText("Pedido completado");
        btnEscanearQR.setVisibility(View.GONE);
        layoutEscanearQR.setVisibility(View.GONE);
        btnCancelarPedido.setVisibility(View.GONE);

        new AlertDialog.Builder(this)
                .setTitle("Pedido entregado")
                .setMessage("Tu pedido ha sido entregado exitosamente. Gracias por usar DelUVery.")
                .setPositiveButton("Aceptar", (dialog, which) -> finish())
                .setCancelable(false)
                .show();
    }

    private void cargarArticulosPedido() {
        db.collection("pedidos")
                .document(pedidoId)
                .collection("articulos")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (querySnapshot.isEmpty()) {
                        tvArticulosPedido.setText("Sin articulos");
                        return;
                    }

                    StringBuilder articulos = new StringBuilder();
                    final int[] completados = {0};
                    final int total = querySnapshot.size();

                    for (var doc : querySnapshot) {
                        String articuloId = doc.getString("articuloID");
                        Long cantidad = doc.getLong("cantidad");
                        Double subtotal = doc.getDouble("subtotal");

                        if (articuloId != null) {
                            // Obtener nombre del articulo
                            db.collection("articulos")
                                    .document(articuloId)
                                    .get()
                                    .addOnSuccessListener(artDoc -> {
                                        completados[0]++;
                                        String nombre = artDoc.exists() ?
                                                artDoc.getString("nombre") : articuloId;

                                        articulos.append("- ")
                                                .append(nombre != null ? nombre : articuloId)
                                                .append(" x")
                                                .append(cantidad != null ? cantidad : 1)
                                                .append(" - $")
                                                .append(String.format(Locale.getDefault(), "%.2f",
                                                        subtotal != null ? subtotal : 0.0))
                                                .append("\n");

                                        if (completados[0] == total) {
                                            tvArticulosPedido.setText(articulos.toString().trim());
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        completados[0]++;
                                        if (completados[0] == total) {
                                            tvArticulosPedido.setText(articulos.toString().trim());
                                        }
                                    });
                        }
                    }

                    // Cargar anotaciones
                    String anotaciones = pedido.getAnotaciones();
                    if (anotaciones != null && !anotaciones.isEmpty()) {
                        layoutAnotaciones.setVisibility(View.VISIBLE);
                        tvAnotaciones.setText(anotaciones);
                    } else {
                        layoutAnotaciones.setVisibility(View.GONE);
                    }
                })
                .addOnFailureListener(e -> {
                    tvArticulosPedido.setText("Error al cargar articulos");
                    Log.e(TAG, "Error cargando articulos", e);
                });
    }

    private void abrirEscanerQR() {
        Intent intent = new Intent(this, EscanearQRActivity.class);
        intent.putExtra("pedidoId", pedidoId);
        startActivityForResult(intent, REQUEST_ESCANEAR_QR);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_ESCANEAR_QR && resultCode == RESULT_OK) {
            // El QR fue escaneado exitosamente, el pedido ya se marco como entregado
            mostrarPedidoEntregado();
        }
    }

    private void cancelarPedido() {
        progressBar.setVisibility(View.VISIBLE);

        db.collection("pedidos")
                .document(pedidoId)
                .update("estado", "cancelado")
                .addOnSuccessListener(aVoid -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Pedido cancelado", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Error al cancelar: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Detener listener
        if (pedidoListener != null) {
            pedidoListener.remove();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}