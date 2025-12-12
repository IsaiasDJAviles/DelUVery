package com.example.deluvery.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.deluvery.R;
import com.example.deluvery.adapters.PedidoPendienteAdapter;
import com.example.deluvery.models.Notificacion;
import com.example.deluvery.models.Pedido;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class RepartosActivity extends AppCompatActivity {

    private static final String TAG = "RepartosActivity";

    private RecyclerView recyclerPedidos;
    private PedidoPendienteAdapter adapter;
    private ProgressBar progressBar;
    private TextView tvEmpty;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private SimpleDateFormat timeFormatter;

    private AlertDialog currentDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_repartos);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Pedidos Disponibles");
        }

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        timeFormatter = new SimpleDateFormat("HH:mm", Locale.getDefault());

        inicializarVistas();
        configurarRecyclerView();
        cargarPedidosPendientes();
    }

    private void inicializarVistas() {
        recyclerPedidos = findViewById(R.id.recycler_pedidos_pendientes);
        progressBar = findViewById(R.id.progress_bar);
        tvEmpty = findViewById(R.id.tv_empty);
    }

    private void configurarRecyclerView() {
        adapter = new PedidoPendienteAdapter();
        recyclerPedidos.setLayoutManager(new LinearLayoutManager(this));
        recyclerPedidos.setAdapter(adapter);

        adapter.setOnPedidoClickListener(new PedidoPendienteAdapter.OnPedidoClickListener() {
            @Override
            public void onAceptarPedido(Pedido pedido) {
                confirmarAceptacion(pedido);
            }

            @Override
            public void onVerDetalles(Pedido pedido) {
                mostrarDetallesPedido(pedido);
            }
        });
    }

    private void cargarPedidosPendientes() {
        progressBar.setVisibility(View.VISIBLE);

        db.collection("pedidos")
                .whereEqualTo("estado", "pendiente")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Pedido> pedidos = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Pedido pedido = document.toObject(Pedido.class);
                        if (pedido != null) {
                            pedidos.add(pedido);
                        }
                    }

                    progressBar.setVisibility(View.GONE);

                    if (pedidos.isEmpty()) {
                        recyclerPedidos.setVisibility(View.GONE);
                        tvEmpty.setVisibility(View.VISIBLE);
                    } else {
                        recyclerPedidos.setVisibility(View.VISIBLE);
                        tvEmpty.setVisibility(View.GONE);
                        adapter.setPedidos(pedidos);
                    }

                    Log.d(TAG, "Pedidos pendientes cargados: " + pedidos.size());
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this,
                            "Error al cargar pedidos: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                    Log.e(TAG, "Error al cargar pedidos", e);
                });
    }

    private void confirmarAceptacion(Pedido pedido) {
        String tiempoTranscurrido = calcularTiempoTranscurrido(pedido.getFecha());

        // Cerrar dialogo anterior si existe
        dismissCurrentDialog();

        currentDialog = new AlertDialog.Builder(this)
                .setTitle("Aceptar pedido")
                .setMessage("Pedido: #" + pedido.getId() +
                        "\nCreado: " + tiempoTranscurrido +
                        "\nLugar de entrega: " + pedido.getSalonEntrega() +
                        "\nTotal: $" + String.format(Locale.getDefault(), "%.2f", pedido.getTotal()) +
                        "\n\nAl aceptar, el cliente sera notificado y el pedido quedara asignado a ti.")
                .setPositiveButton("Aceptar", (dialog, which) -> aceptarPedido(pedido))
                .setNegativeButton("Cancelar", null)
                .create();

        currentDialog.show();
    }

    private void aceptarPedido(Pedido pedido) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Debes iniciar sesion", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        Map<String, Object> updates = new HashMap<>();
        updates.put("repartidorID", currentUser.getUid());
        updates.put("estado", "asignado");

        db.collection("pedidos")
                .document(pedido.getId())
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    progressBar.setVisibility(View.GONE);

                    // Enviar notificacion al cliente
                    enviarNotificacionCliente(pedido);

                    Toast.makeText(this,
                            "Pedido aceptado exitosamente",
                            Toast.LENGTH_SHORT).show();

                    // Recargar lista
                    cargarPedidosPendientes();

                    // MODIFICADO: Abrir NavegacionEntregaActivity en lugar de mostrar dialogo
                    abrirNavegacion(pedido);
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this,
                            "Error al aceptar pedido: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error al aceptar pedido", e);
                });
    }

    // NUEVO METODO
    private void abrirNavegacion(Pedido pedido) {
        dismissCurrentDialog();

        Intent intent = new Intent(this, NavegacionEntregaActivity.class);
        intent.putExtra("pedidoId", pedido.getId());
        intent.putExtra("destinoLat", pedido.getLat());
        intent.putExtra("destinoLng", pedido.getLng());
        startActivity(intent);
    }

    private void enviarNotificacionCliente(Pedido pedido) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return;

        String repartidorNombre = currentUser.getDisplayName() != null ?
                currentUser.getDisplayName() : "Un repartidor";

        Notificacion notificacion = new Notificacion();
        notificacion.setUsuarioID(pedido.getClienteID());
        notificacion.setPedidoID(pedido.getId());
        notificacion.setTipo("pedido_aceptado");
        notificacion.setTitulo("Pedido aceptado");
        notificacion.setMensaje(repartidorNombre + " ha aceptado tu pedido #" + pedido.getId() +
                ". Pronto estará en camino.");
        notificacion.setFecha(new Date());
        notificacion.setLeida(false);

        db.collection("notificaciones")
                .add(notificacion)
                .addOnSuccessListener(docRef -> {
                    Log.d(TAG, "Notificación enviada al cliente: " + pedido.getClienteID());
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al enviar notificación", e);
                });
    }

    private void mostrarInfoPedidoAceptado(Pedido pedido) {
        new AlertDialog.Builder(this)
                .setTitle("Pedido aceptado")
                .setMessage("Lugar de entrega:\n" + pedido.getSalonEntrega() +
                        "\n\nEl sistema de navegación GPS se habilitará próximamente." +
                        "\n\nPor ahora, puedes contactar al cliente para coordinar la entrega." +
                        "\n\nUna vez que llegues al punto de entrega, marca el pedido como entregado.")
                .setPositiveButton("Entendido", null)
                .show();
    }

    private void mostrarDetallesPedido(Pedido pedido) {
        progressBar.setVisibility(View.VISIBLE);

        // Primero obtener los articulos del pedido
        db.collection("pedidos")
                .document(pedido.getId())
                .collection("articulos")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {

                    // Lista para almacenar los IDs de articulos
                    List<String> articuloIDs = new ArrayList<>();
                    Map<String, Integer> cantidades = new HashMap<>();
                    Map<String, Double> subtotales = new HashMap<>();

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String articuloID = doc.getString("articuloID");
                        Long cantidadLong = doc.getLong("cantidad");
                        Double subtotalDoc = doc.getDouble("subtotal");

                        if (articuloID != null) {
                            articuloIDs.add(articuloID);
                            cantidades.put(articuloID, cantidadLong != null ? cantidadLong.intValue() : 1);
                            subtotales.put(articuloID, subtotalDoc != null ? subtotalDoc : 0.0);
                        }
                    }

                    // Ahora consultar los nombres de los articulos
                    if (articuloIDs.isEmpty()) {
                        progressBar.setVisibility(View.GONE);
                        mostrarDialogoDetalles(pedido, "Sin articulos", 0.0);
                        return;
                    }

                    // Obtener nombres de articulos desde la coleccion "articulos"
                    obtenerNombresArticulos(pedido, articuloIDs, cantidades, subtotales);
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Error al cargar detalles", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error cargando articulos del pedido", e);
                });
    }

    private void mostrarDialogoConLayout(Pedido pedido, String articulosTexto,
                                         double subtotal, String anotaciones) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialogo_detalles_pedido, null);

        // Referencias a las vistas
        TextView tvPedidoId = dialogView.findViewById(R.id.tv_detalle_pedido_id);
        TextView tvFecha = dialogView.findViewById(R.id.tv_detalle_fecha);
        TextView tvLugar = dialogView.findViewById(R.id.tv_detalle_lugar);
        TextView tvArticulos = dialogView.findViewById(R.id.tv_detalle_articulos);
        TextView tvSubtotal = dialogView.findViewById(R.id.tv_detalle_subtotal);
        TextView tvServicio = dialogView.findViewById(R.id.tv_detalle_servicio);
        TextView tvTotal = dialogView.findViewById(R.id.tv_detalle_total);
        LinearLayout layoutAnotaciones = dialogView.findViewById(R.id.layout_detalle_anotaciones);
        TextView tvAnotaciones = dialogView.findViewById(R.id.tv_detalle_anotaciones);

        // Llenar datos
        tvPedidoId.setText("Pedido: #" + pedido.getId());
        tvFecha.setText("Creado: " + calcularTiempoTranscurrido(pedido.getFecha()));
        tvLugar.setText(pedido.getSalonEntrega());
        tvArticulos.setText(articulosTexto.trim());

        double costoServicio = 5.0;
        double total = subtotal + costoServicio;

        tvSubtotal.setText(String.format(Locale.getDefault(), "$%.2f", subtotal));
        tvServicio.setText(String.format(Locale.getDefault(), "$%.2f", costoServicio));
        tvTotal.setText(String.format(Locale.getDefault(), "$%.2f", total));

        // Mostrar anotaciones si existen
        if (anotaciones != null && !anotaciones.trim().isEmpty()) {
            layoutAnotaciones.setVisibility(View.VISIBLE);
            tvAnotaciones.setText(anotaciones);
        } else {
            layoutAnotaciones.setVisibility(View.GONE);
        }

        new AlertDialog.Builder(this)
                .setTitle("Detalles del pedido")
                .setView(dialogView)
                .setPositiveButton("Cerrar", null)
                .show();
    }

    private void obtenerNombresArticulos(Pedido pedido, List<String> articuloIDs,
                                         Map<String, Integer> cantidades,
                                         Map<String, Double> subtotales) {

        StringBuilder articulosTexto = new StringBuilder();
        final double[] subtotalTotal = {0.0};
        final int[] completados = {0};

        for (String articuloID : articuloIDs) {
            db.collection("articulos")
                    .document(articuloID)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        completados[0]++;

                        String nombreArticulo;
                        if (documentSnapshot.exists()) {
                            nombreArticulo = documentSnapshot.getString("nombre");
                            if (nombreArticulo == null) {
                                nombreArticulo = articuloID; // Fallback al ID
                            }
                        } else {
                            nombreArticulo = articuloID; // Fallback al ID
                        }

                        int cantidad = cantidades.get(articuloID);
                        double subtotal = subtotales.get(articuloID);
                        subtotalTotal[0] += subtotal;

                        articulosTexto.append("- ")
                                .append(cantidad)
                                .append("x ")
                                .append(nombreArticulo)
                                .append(" - $")
                                .append(String.format(Locale.getDefault(), "%.2f", subtotal))
                                .append("\n");

                        // Cuando todos los articulos se han procesado
                        if (completados[0] == articuloIDs.size()) {
                            progressBar.setVisibility(View.GONE);

                            if (isFinishing() || isDestroyed()) {
                                return;
                            }

                            mostrarDialogoDetalles(pedido, articulosTexto.toString(), subtotalTotal[0]);
                        }
                    })
                    .addOnFailureListener(e -> {
                        completados[0]++;

                        // Usar el ID como fallback en caso de error
                        int cantidad = cantidades.get(articuloID);
                        double subtotal = subtotales.get(articuloID);
                        subtotalTotal[0] += subtotal;

                        articulosTexto.append("- ")
                                .append(cantidad)
                                .append("x ")
                                .append(articuloID)
                                .append(" - $")
                                .append(String.format(Locale.getDefault(), "%.2f", subtotal))
                                .append("\n");

                        if (completados[0] == articuloIDs.size()) {
                            progressBar.setVisibility(View.GONE);

                            if (isFinishing() || isDestroyed()) {
                                return;
                            }

                            mostrarDialogoDetalles(pedido, articulosTexto.toString(), subtotalTotal[0]);
                        }
                    });
        }
    }
    private void mostrarDialogoDetalles(Pedido pedido, String articulosTexto, double subtotal) {
        db.collection("pedidos")
                .document(pedido.getId())
                .get()
                .addOnSuccessListener(doc -> {
                    if (isFinishing() || isDestroyed()) {
                        return;
                    }
                    String anotaciones = doc.getString("anotaciones");
                    mostrarDialogoConLayout(pedido, articulosTexto, subtotal, anotaciones);
                })
                .addOnFailureListener(e -> {
                    if (isFinishing() || isDestroyed()) {
                        return;
                    }
                    mostrarDialogoConLayout(pedido, articulosTexto, subtotal, null);
                });
    }

    private void dismissCurrentDialog() {
        if (currentDialog != null && currentDialog.isShowing()) {
            currentDialog.dismiss();
            currentDialog = null;
        }
    }

    private String calcularTiempoTranscurrido(Date fecha) {
        if (fecha == null) return "Hace un momento";

        long diff = System.currentTimeMillis() - fecha.getTime();
        long segundos = diff / 1000;
        long minutos = segundos / 60;
        long horas = minutos / 60;
        long dias = horas / 24;

        if (dias > 0) {
            return "Hace " + dias + (dias == 1 ? " día" : " días");
        } else if (horas > 0) {
            return "Hace " + horas + (horas == 1 ? " hora" : " horas");
        } else if (minutos > 0) {
            return "Hace " + minutos + (minutos == 1 ? " minuto" : " minutos");
        } else {
            return "Hace un momento";
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarPedidosPendientes();
    }

    @Override
    protected void onDestroy() {
        dismissCurrentDialog();
        super.onDestroy();
    }

}