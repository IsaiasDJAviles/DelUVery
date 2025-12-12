package com.example.deluvery.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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

        new AlertDialog.Builder(this)
                .setTitle("Aceptar pedido")
                .setMessage("Pedido: #" + pedido.getId() +
                        "\nCreado: " + tiempoTranscurrido +
                        "\nLugar de entrega: " + pedido.getSalonEntrega() +
                        "\nTotal: $" + String.format(Locale.getDefault(), "%.2f", pedido.getTotal()) +
                        "\n\nAl aceptar, el cliente será notificado y el pedido quedará asignado a ti.")
                .setPositiveButton("Aceptar", (dialog, which) -> aceptarPedido(pedido))
                .setNegativeButton("Cancelar", null)
                .show();
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

        db.collection("pedidos")
                .document(pedido.getId())
                .collection("articulos")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    progressBar.setVisibility(View.GONE);

                    StringBuilder detalles = new StringBuilder();
                    detalles.append("Pedido: #").append(pedido.getId()).append("\n");
                    detalles.append("Creado: ").append(calcularTiempoTranscurrido(pedido.getFecha())).append("\n\n");
                    detalles.append("Lugar de entrega:\n").append(pedido.getSalonEntrega()).append("\n\n");
                    detalles.append("Artículos:\n");

                    double subtotal = 0.0;
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String articuloID = doc.getString("articuloID");
                        Long cantidadLong = doc.getLong("cantidad");
                        Double subtotalDoc = doc.getDouble("subtotal");

                        int cantidad = cantidadLong != null ? cantidadLong.intValue() : 0;
                        double subtotalItem = subtotalDoc != null ? subtotalDoc : 0.0;

                        detalles.append("• ").append(cantidad).append("x ")
                                .append(articuloID).append(" - $")
                                .append(String.format(Locale.getDefault(), "%.2f", subtotalItem))
                                .append("\n");

                        subtotal += subtotalItem;
                    }

                    detalles.append("\nSubtotal: $").append(String.format(Locale.getDefault(), "%.2f", subtotal));
                    detalles.append("\nCosto de servicio: $5.00");
                    detalles.append("\nTotal: $").append(String.format(Locale.getDefault(), "%.2f", pedido.getTotal()));

                    new AlertDialog.Builder(this)
                            .setTitle("Detalles del pedido")
                            .setMessage(detalles.toString())
                            .setPositiveButton("Cerrar", null)
                            .show();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this,
                            "Error al cargar detalles: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
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
}