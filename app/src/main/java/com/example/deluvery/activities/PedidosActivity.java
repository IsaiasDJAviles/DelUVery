package com.example.deluvery.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.deluvery.R;
import com.example.deluvery.adapters.PedidoAdapter;
import com.example.deluvery.models.Pedido;
import com.example.deluvery.viewmodels.PedidoViewModel;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class PedidosActivity extends AppCompatActivity {

    private static final String TAG = "PedidosActivity";

    private PedidoViewModel viewModel;
    private PedidoAdapter adapter;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView tvEmpty;

    // Firebase
    private FirebaseFirestore db;

    // Pedido seleccionado para mostrar detalles
    private Pedido pedidoSeleccionadoParaDetalles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pedidos);

        // Configurar toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Mis Pedidos");
        }

        // Inicializar Firebase
        db = FirebaseFirestore.getInstance();

        // Inicializar vistas
        recyclerView = findViewById(R.id.recycler_pedidos);
        progressBar = findViewById(R.id.progress_bar);
        tvEmpty = findViewById(R.id.tv_empty);

        // Configurar RecyclerView
        setupRecyclerView();

        // Inicializar ViewModel
        viewModel = new ViewModelProvider(this).get(PedidoViewModel.class);

        // Observar LiveData
        observarViewModel();

        // Obtener clienteID del intent
        String clienteID = getIntent().getStringExtra("clienteID");
        if (clienteID == null) {
            clienteID = "EST001"; // Valor por defecto
        }

        // Cargar datos
        viewModel.cargarPedidosCliente(clienteID);
    }

    private void setupRecyclerView() {
        adapter = new PedidoAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        adapter.setOnPedidoClickListener(new PedidoAdapter.OnPedidoClickListener() {
            @Override
            public void onPedidoClick(Pedido pedido) {
                // MODIFICADO: Abrir seguimiento si el pedido esta activo
                abrirSeguimientoODetalles(pedido);
            }

            @Override
            public void onVerDetallesClick(Pedido pedido) {
                // MODIFICADO: Abrir seguimiento si el pedido esta activo
                abrirSeguimientoODetalles(pedido);
            }
        });
    }
    private void abrirSeguimientoODetalles(Pedido pedido) {
        String estado = pedido.getEstado();

        // Si el pedido esta activo (pendiente, asignado, en_camino, esperando_confirmacion)
        // abrir la pantalla de seguimiento
        if ("pendiente".equals(estado) ||
                "asignado".equals(estado) ||
                "en_camino".equals(estado) ||
                "esperando_confirmacion".equals(estado)) {

            Intent intent = new Intent(this, SeguimientoPedidoActivity.class);
            intent.putExtra("pedidoId", pedido.getId());
            intent.putExtra("miLat", pedido.getLat());
            intent.putExtra("miLng", pedido.getLng());
            startActivity(intent);

        } else {
            // Si el pedido ya esta entregado o cancelado, mostrar detalles normales
            mostrarDetallesPedido(pedido);
        }
    }


    private void observarViewModel() {
        // Observar lista de pedidos
        viewModel.getPedidos().observe(this, pedidos -> {
            if (pedidos != null && !pedidos.isEmpty()) {
                adapter.setPedidos(pedidos);
                recyclerView.setVisibility(View.VISIBLE);
                tvEmpty.setVisibility(View.GONE);
            } else {
                recyclerView.setVisibility(View.GONE);
                tvEmpty.setVisibility(View.VISIBLE);
            }
        });

        // Observar estado de carga
        viewModel.getCargando().observe(this, cargando -> {
            progressBar.setVisibility(cargando ? View.VISIBLE : View.GONE);
        });

        // Observar errores
        viewModel.getError().observe(this, error -> {
            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_LONG).show();
                viewModel.limpiarMensajes();
            }
        });

        // Observar mensajes de exito
        viewModel.getMensajeExito().observe(this, mensaje -> {
            if (mensaje != null) {
                Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show();
                viewModel.limpiarMensajes();
            }
        });
    }

    private void mostrarDetallesPedido(Pedido pedido) {
        progressBar.setVisibility(View.VISIBLE);
        pedidoSeleccionadoParaDetalles = pedido;

        // Cargar articulos del pedido directamente desde Firestore
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
                            cantidades.put(articuloID, cantidadLong != null ?
                                    cantidadLong.intValue() : 1);
                            subtotales.put(articuloID, subtotalDoc != null ?
                                    subtotalDoc : 0.0);
                        }
                    }

                    Log.d(TAG, "Articulos encontrados: " + articuloIDs.size());

                    // Verificar si hay articulos
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
                                nombreArticulo = "Articulo " + articuloID;
                            }
                        } else {
                            nombreArticulo = "Articulo " + articuloID;
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

                        // Cuando se hayan procesado todos los articulos
                        if (completados[0] == articuloIDs.size()) {
                            progressBar.setVisibility(View.GONE);
                            mostrarDialogoConLayout(pedido, articulosTexto.toString(),
                                    subtotalTotal[0], pedido.getAnotaciones());
                        }
                    })
                    .addOnFailureListener(e -> {
                        completados[0]++;
                        Log.e(TAG, "Error obteniendo articulo: " + articuloID, e);

                        // Agregar con ID si falla
                        int cantidad = cantidades.get(articuloID);
                        double subtotal = subtotales.get(articuloID);
                        subtotalTotal[0] += subtotal;

                        articulosTexto.append("- ")
                                .append(cantidad)
                                .append("x Articulo ")
                                .append(articuloID)
                                .append(" - $")
                                .append(String.format(Locale.getDefault(), "%.2f", subtotal))
                                .append("\n");

                        if (completados[0] == articuloIDs.size()) {
                            progressBar.setVisibility(View.GONE);
                            mostrarDialogoConLayout(pedido, articulosTexto.toString(),
                                    subtotalTotal[0], pedido.getAnotaciones());
                        }
                    });
        }
    }

    private void mostrarDialogoDetalles(Pedido pedido, String articulosTexto, double subtotal) {
        mostrarDialogoConLayout(pedido, articulosTexto, subtotal, pedido.getAnotaciones());
    }

    private void mostrarDialogoConLayout(Pedido pedido, String articulosTexto,
                                         double subtotal, String anotaciones) {
        View dialogView = LayoutInflater.from(this)
                .inflate(R.layout.dialogo_detalles_pedido, null);

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

    private String calcularTiempoTranscurrido(Date fecha) {
        if (fecha == null) {
            return "Fecha desconocida";
        }

        long diffMillis = System.currentTimeMillis() - fecha.getTime();
        long minutos = TimeUnit.MILLISECONDS.toMinutes(diffMillis);
        long horas = TimeUnit.MILLISECONDS.toHours(diffMillis);
        long dias = TimeUnit.MILLISECONDS.toDays(diffMillis);

        if (minutos < 1) {
            return "Hace un momento";
        } else if (minutos < 60) {
            return "Hace " + minutos + " min";
        } else if (horas < 24) {
            return "Hace " + horas + " hora" + (horas > 1 ? "s" : "");
        } else if (dias < 7) {
            return "Hace " + dias + " dia" + (dias > 1 ? "s" : "");
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            return sdf.format(fecha);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}