package com.example.deluvery.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.deluvery.R;
import com.example.deluvery.models.Pedido;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class NavegacionEntregaActivity extends AppCompatActivity {

    private static final String TAG = "NavegacionEntrega";
    private static final int LOCATION_PERMISSION_REQUEST = 1001;
    private static final float DISTANCIA_LLEGADA_METROS = 50.0f; // 50 metros para considerar llegada

    // Views
    private TextView tvDistancia;
    private TextView tvEstadoEntrega;
    private TextView tvDireccionEntrega;
    private TextView tvPedidoId;
    private TextView tvTotalPedido;
    private ProgressBar progressBar;
    private Button btnIniciarNavegacion;
    private Button btnLlegue;
    private Button btnCancelar;

    // Cards
    private CardView cardDetallesEntrega;
    private CardView cardDetallesPedido;
    private CardView cardQR;

    // QR
    private ImageView imgQRCode;
    private TextView tvCodigoQR;

    // Detalles del pedido
    private TextView tvArticulosPedido;
    private TextView tvAnotaciones;
    private LinearLayout layoutAnotaciones;

    // Location
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private boolean isTracking = false;

    // Data
    private Pedido pedido;
    private String pedidoId;
    private double destinoLat;
    private double destinoLng;
    private String codigoQRGenerado;

    // Firebase
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navegacion_entrega);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Navegacion de Entrega");
        }

        // Inicializar Firebase
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Inicializar Location
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Obtener datos del intent
        pedidoId = getIntent().getStringExtra("pedidoId");
        destinoLat = getIntent().getDoubleExtra("destinoLat", 0.0);
        destinoLng = getIntent().getDoubleExtra("destinoLng", 0.0);

        if (pedidoId == null) {
            Toast.makeText(this, "Error: No se recibio el ID del pedido", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        inicializarVistas();
        configurarBotones();
        cargarDatosPedido();
        configurarLocationCallback();
    }

    private void inicializarVistas() {
        tvDistancia = findViewById(R.id.tv_distancia);
        tvEstadoEntrega = findViewById(R.id.tv_estado_entrega);
        tvDireccionEntrega = findViewById(R.id.tv_direccion_entrega);
        tvPedidoId = findViewById(R.id.tv_pedido_id_nav);
        tvTotalPedido = findViewById(R.id.tv_total_pedido_nav);
        progressBar = findViewById(R.id.progress_bar_nav);
        btnIniciarNavegacion = findViewById(R.id.btn_iniciar_navegacion);
        btnLlegue = findViewById(R.id.btn_llegue);
        btnCancelar = findViewById(R.id.btn_cancelar_entrega);

        // Cards
        cardDetallesEntrega = findViewById(R.id.card_detalles_entrega);
        cardDetallesPedido = findViewById(R.id.card_detalles_pedido);
        cardQR = findViewById(R.id.card_qr);

        // QR Views
        imgQRCode = findViewById(R.id.img_qr_code);
        tvCodigoQR = findViewById(R.id.tv_codigo_qr);

        // Detalles pedido
        tvArticulosPedido = findViewById(R.id.tv_articulos_pedido);
        tvAnotaciones = findViewById(R.id.tv_anotaciones);
        layoutAnotaciones = findViewById(R.id.layout_anotaciones);

        // Estado inicial
        cardQR.setVisibility(View.GONE);
        btnLlegue.setVisibility(View.GONE);
    }

    private void configurarBotones() {
        btnIniciarNavegacion.setOnClickListener(v -> {
            if (verificarPermisos()) {
                iniciarTracking();
            }
        });

        btnLlegue.setOnClickListener(v -> {
            confirmarLlegada();
        });

        btnCancelar.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Cancelar entrega")
                    .setMessage("¿Estas seguro de que deseas cancelar esta entrega?")
                    .setPositiveButton("Si, cancelar", (dialog, which) -> cancelarEntrega())
                    .setNegativeButton("No", null)
                    .show();
        });
    }

    private void cargarDatosPedido() {
        progressBar.setVisibility(View.VISIBLE);

        db.collection("pedidos")
                .document(pedidoId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    progressBar.setVisibility(View.GONE);

                    if (documentSnapshot.exists()) {
                        pedido = documentSnapshot.toObject(Pedido.class);
                        if (pedido != null) {
                            pedido.setId(documentSnapshot.getId());
                            mostrarDatosPedido();
                            cargarArticulosPedido();
                        }
                    } else {
                        Toast.makeText(this, "Pedido no encontrado", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Error al cargar pedido: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error cargando pedido", e);
                });
    }

    private void mostrarDatosPedido() {
        tvPedidoId.setText("Pedido #" + pedido.getId());
        tvTotalPedido.setText(String.format(Locale.getDefault(), "$%.2f", pedido.getTotal()));
        tvDireccionEntrega.setText(pedido.getSalonEntrega());
        tvEstadoEntrega.setText("EN CAMINO");

        // Usar coordenadas del pedido si existen
        if (pedido.getLat() != 0.0 && pedido.getLng() != 0.0) {
            destinoLat = pedido.getLat();
            destinoLng = pedido.getLng();
        }
    }

    private void cargarArticulosPedido() {
        db.collection("pedidos")
                .document(pedidoId)
                .collection("articulos")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    StringBuilder articulos = new StringBuilder();

                    for (var doc : querySnapshot) {
                        String articuloId = doc.getString("articuloID");
                        Long cantidad = doc.getLong("cantidad");
                        Double subtotal = doc.getDouble("subtotal");

                        articulos.append("- ")
                                .append(articuloId != null ? articuloId : "Articulo")
                                .append(" x")
                                .append(cantidad != null ? cantidad : 1)
                                .append(" - $")
                                .append(String.format(Locale.getDefault(), "%.2f",
                                        subtotal != null ? subtotal : 0.0))
                                .append("\n");
                    }

                    if (articulos.length() > 0) {
                        tvArticulosPedido.setText(articulos.toString().trim());
                    } else {
                        tvArticulosPedido.setText("Sin articulos");
                    }

                    // Cargar anotaciones si existen
                    cargarAnotaciones();
                })
                .addOnFailureListener(e -> {
                    tvArticulosPedido.setText("Error al cargar articulos");
                    Log.e(TAG, "Error cargando articulos", e);
                });
    }

    private void cargarAnotaciones() {
        db.collection("pedidos")
                .document(pedidoId)
                .get()
                .addOnSuccessListener(doc -> {
                    String anotaciones = doc.getString("anotaciones");
                    if (anotaciones != null && !anotaciones.isEmpty()) {
                        layoutAnotaciones.setVisibility(View.VISIBLE);
                        tvAnotaciones.setText(anotaciones);
                    } else {
                        layoutAnotaciones.setVisibility(View.GONE);
                    }
                });
    }

    private boolean verificarPermisos() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    },
                    LOCATION_PERMISSION_REQUEST);
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                iniciarTracking();
            } else {
                Toast.makeText(this, "Se necesitan permisos de ubicacion para navegar",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    private void configurarLocationCallback() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                for (Location location : locationResult.getLocations()) {
                    actualizarDistancia(location);
                }
            }
        };
    }

    private void iniciarTracking() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        isTracking = true;
        btnIniciarNavegacion.setEnabled(false);
        btnIniciarNavegacion.setText("Navegando...");
        btnLlegue.setVisibility(View.VISIBLE);

        // Actualizar estado en Firebase
        actualizarEstadoPedido("en_camino");

        LocationRequest locationRequest = new LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY, 5000)
                .setMinUpdateIntervalMillis(2000)
                .build();

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback,
                Looper.getMainLooper());

        Toast.makeText(this, "Navegacion iniciada", Toast.LENGTH_SHORT).show();
    }

    private void actualizarDistancia(Location ubicacionActual) {
        if (destinoLat == 0.0 && destinoLng == 0.0) {
            tvDistancia.setText("Destino no configurado");
            return;
        }

        Location destino = new Location("");
        destino.setLatitude(destinoLat);
        destino.setLongitude(destinoLng);

        float distanciaMetros = ubicacionActual.distanceTo(destino);

        // Mostrar distancia
        if (distanciaMetros >= 1000) {
            tvDistancia.setText(String.format(Locale.getDefault(), "%.1f km", distanciaMetros / 1000));
        } else {
            tvDistancia.setText(String.format(Locale.getDefault(), "%.0f m", distanciaMetros));
        }

        // Verificar si llego al destino
        if (distanciaMetros <= DISTANCIA_LLEGADA_METROS) {
            onLlegadaDetectada();
        }

        // Actualizar ubicacion en Firebase
        actualizarUbicacionRepartidor(ubicacionActual.getLatitude(), ubicacionActual.getLongitude());
    }

    private void onLlegadaDetectada() {
        if (!isTracking) return;

        detenerTracking();
        tvEstadoEntrega.setText("LLEGASTE AL DESTINO");
        tvDistancia.setText("0 m");

        new AlertDialog.Builder(this)
                .setTitle("Has llegado al destino")
                .setMessage("Parece que has llegado al punto de entrega. " +
                        "¿Deseas generar el codigo QR para que el cliente confirme la entrega?")
                .setPositiveButton("Generar QR", (dialog, which) -> generarCodigoQR())
                .setNegativeButton("Aun no", null)
                .show();
    }

    private void confirmarLlegada() {
        new AlertDialog.Builder(this)
                .setTitle("Confirmar llegada")
                .setMessage("¿Has llegado al punto de entrega?")
                .setPositiveButton("Si, generar QR", (dialog, which) -> {
                    detenerTracking();
                    generarCodigoQR();
                })
                .setNegativeButton("No", null)
                .show();
    }

    private void generarCodigoQR() {
        // Generar codigo unico
        codigoQRGenerado = "DEL_" + pedidoId + "_" + System.currentTimeMillis();

        // Guardar codigo en Firebase
        Map<String, Object> updates = new HashMap<>();
        updates.put("codigoQR", codigoQRGenerado);
        updates.put("estado", "esperando_confirmacion");
        updates.put("qrGeneradoEn", new Date());

        db.collection("pedidos")
                .document(pedidoId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    mostrarQR();
                    enviarNotificacionCliente();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al generar QR: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error generando QR", e);
                });
    }

    private void mostrarQR() {
        cardQR.setVisibility(View.VISIBLE);
        tvCodigoQR.setText("Codigo: " + codigoQRGenerado);
        tvEstadoEntrega.setText("ESPERANDO CONFIRMACION");
        btnLlegue.setVisibility(View.GONE);

        try {
            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix bitMatrix = writer.encode(codigoQRGenerado, BarcodeFormat.QR_CODE, 512, 512);

            int width = bitMatrix.getWidth();
            int height = bitMatrix.getHeight();
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);

            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    bitmap.setPixel(x, y, bitMatrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF);
                }
            }

            imgQRCode.setImageBitmap(bitmap);

            // Escuchar cambios en el pedido para detectar cuando se escanee
            escucharConfirmacionEntrega();

        } catch (WriterException e) {
            Log.e(TAG, "Error generando imagen QR", e);
            Toast.makeText(this, "Error al generar codigo QR", Toast.LENGTH_SHORT).show();
        }
    }

    private void escucharConfirmacionEntrega() {
        db.collection("pedidos")
                .document(pedidoId)
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Error escuchando cambios", error);
                        return;
                    }

                    if (snapshot != null && snapshot.exists()) {
                        String estado = snapshot.getString("estado");
                        Boolean qrValidado = snapshot.getBoolean("qrValidado");

                        if ("entregado".equals(estado) || (qrValidado != null && qrValidado)) {
                            onEntregaConfirmada();
                        }
                    }
                });
    }

    private void onEntregaConfirmada() {
        new AlertDialog.Builder(this)
                .setTitle("Entrega confirmada")
                .setMessage("El cliente ha confirmado la recepcion del pedido. " +
                        "¡Gracias por tu servicio!")
                .setPositiveButton("Aceptar", (dialog, which) -> {
                    setResult(RESULT_OK);
                    finish();
                })
                .setCancelable(false)
                .show();
    }

    private void enviarNotificacionCliente() {
        Map<String, Object> notificacion = new HashMap<>();
        notificacion.put("usuarioID", pedido.getClienteID());
        notificacion.put("pedidoID", pedidoId);
        notificacion.put("tipo", "repartidor_llego");
        notificacion.put("titulo", "Tu pedido ha llegado");
        notificacion.put("mensaje", "El repartidor ha llegado al punto de entrega. " +
                "Abre la app para escanear el codigo QR y confirmar la entrega.");
        notificacion.put("fecha", new Date());
        notificacion.put("leida", false);
        notificacion.put("codigoQR", codigoQRGenerado);

        db.collection("notificaciones")
                .add(notificacion)
                .addOnSuccessListener(docRef -> Log.d(TAG, "Notificacion enviada al cliente"))
                .addOnFailureListener(e -> Log.e(TAG, "Error enviando notificacion", e));
    }

    private void actualizarEstadoPedido(String estado) {
        db.collection("pedidos")
                .document(pedidoId)
                .update("estado", estado)
                .addOnFailureListener(e -> Log.e(TAG, "Error actualizando estado", e));
    }

    private void actualizarUbicacionRepartidor(double lat, double lng) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("repartidorLat", lat);
        updates.put("repartidorLng", lng);
        updates.put("ultimaActualizacion", new Date());

        db.collection("pedidos")
                .document(pedidoId)
                .update(updates)
                .addOnFailureListener(e -> Log.e(TAG, "Error actualizando ubicacion", e));
    }

    private void cancelarEntrega() {
        detenerTracking();

        Map<String, Object> updates = new HashMap<>();
        updates.put("estado", "asignado");
        updates.put("repartidorLat", null);
        updates.put("repartidorLng", null);

        db.collection("pedidos")
                .document(pedidoId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Entrega cancelada", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al cancelar", Toast.LENGTH_SHORT).show();
                });
    }

    private void detenerTracking() {
        if (isTracking) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
            isTracking = false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        detenerTracking();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}