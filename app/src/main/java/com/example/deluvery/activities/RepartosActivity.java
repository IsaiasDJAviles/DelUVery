package com.example.deluvery.activities;

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
import com.example.deluvery.models.Pedido;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RepartosActivity extends AppCompatActivity {

    private static final String TAG = "RepartosActivity";

    private RecyclerView recyclerPedidos;
    private PedidoPendienteAdapter adapter;
    private ProgressBar progressBar;
    private TextView tvEmpty;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

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
        new AlertDialog.Builder(this)
                .setTitle("Aceptar pedido")
                .setMessage("¿Deseas aceptar el pedido #" + pedido.getId() +
                        "?\n\nLugar de entrega: " + pedido.getSalonEntrega() +
                        "\n\nTotal: $" + String.format("%.2f", pedido.getTotal()))
                .setPositiveButton("Aceptar", (dialog, which) -> aceptarPedido(pedido))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void aceptarPedido(Pedido pedido) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Debes iniciar sesión", Toast.LENGTH_SHORT).show();
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

                    // Enviar notificación al cliente
                    enviarNotificacionCliente(pedido.getClienteID(), pedido.getId());

                    Toast.makeText(this,
                            "Pedido aceptado exitosamente",
                            Toast.LENGTH_SHORT).show();

                    // Recargar lista
                    cargarPedidosPendientes();

                    // Mostrar información del pedido
                    mostrarInfoPedidoAceptado(pedido);
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this,
                            "Error al aceptar pedido: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error al aceptar pedido", e);
                });
    }

    private void enviarNotificacionCliente(String clienteID, String pedidoID) {
        // Crear notificación en Firestore
        Map<String, Object> notificacion = new HashMap<>();
        notificacion.put("clienteID", clienteID);
        notificacion.put("pedidoID", pedidoID);
        notificacion.put("tipo", "pedido_aceptado");
        notificacion.put("mensaje", "Tu pedido #" + pedidoID + " ha sido aceptado por un repartidor");
        notificacion.put("fecha", new Date());
        notificacion.put("leida", false);

        db.collection("notificaciones")
                .add(notificacion)
                .addOnSuccessListener(docRef ->
                        Log.d(TAG, "Notificación enviada al cliente"))
                .addOnFailureListener(e ->
                        Log.e(TAG, "Error al enviar notificación", e));
    }

    private void mostrarInfoPedidoAceptado(Pedido pedido) {
        new AlertDialog.Builder(this)
                .setTitle("Pedido aceptado")
                .setMessage("Lugar de entrega:\n" + pedido.getSalonEntrega() +
                        "\n\nEl sistema de navegación GPS se habilitará próximamente." +
                        "\n\nPor ahora, contacta al cliente para coordinar la entrega.")
                .setPositiveButton("Entendido", null)
                .show();
    }

    private void mostrarDetallesPedido(Pedido pedido) {
        // Cargar artículos del pedido
        db.collection("pedidos")
                .document(pedido.getId())
                .collection("articulos")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    StringBuilder detalles = new StringBuilder();
                    detalles.append("Pedido: #").append(pedido.getId()).append("\n\n");
                    detalles.append("Lugar de entrega:\n").append(pedido.getSalonEntrega()).append("\n\n");
                    detalles.append("Artículos:\n");

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String nombre = doc.getString("articuloID");
                        int cantidad = doc.getLong("cantidad").intValue();
                        double subtotal = doc.getDouble("subtotal");
                        detalles.append("• ").append(cantidad).append("x ")
                                .append(nombre).append(" - $")
                                .append(String.format("%.2f", subtotal)).append("\n");
                    }

                    detalles.append("\nTotal: $").append(String.format("%.2f", pedido.getTotal()));

                    new AlertDialog.Builder(this)
                            .setTitle("Detalles del pedido")
                            .setMessage(detalles.toString())
                            .setPositiveButton("Cerrar", null)
                            .show();
                });
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