package com.example.deluvery.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.deluvery.MainActivity;
import com.example.deluvery.R;
import com.example.deluvery.adapters.CarritoAdapter;
import com.example.deluvery.models.ArticuloPedido;
import com.example.deluvery.models.CarritoItem;
import com.example.deluvery.models.Pedido;
import com.example.deluvery.utils.CarritoManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class CarritoActivity extends AppCompatActivity {

    private RecyclerView recyclerCarrito;
    private CarritoAdapter adapter;
    private TextView tvSubtotal;
    private TextView tvCostoServicio;
    private TextView tvTotal;
    private Button btnContinuar;
    private LinearLayout layoutEmpty;
    private LinearLayout layoutResumen;
    private ProgressBar progressBar;

    private CarritoManager carritoManager;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_carrito);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        carritoManager = CarritoManager.getInstance();
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        inicializarVistas();
        configurarRecyclerView();
        configurarBotones();
        actualizarCarrito();
    }

    private void inicializarVistas() {
        recyclerCarrito = findViewById(R.id.recycler_carrito);
        tvSubtotal = findViewById(R.id.tv_subtotal_valor);
        tvCostoServicio = findViewById(R.id.tv_costo_servicio_valor);
        tvTotal = findViewById(R.id.tv_total_valor);
        btnContinuar = findViewById(R.id.btn_continuar);
        layoutEmpty = findViewById(R.id.layout_empty);
        layoutResumen = findViewById(R.id.layout_resumen);
        progressBar = findViewById(R.id.progress_bar);
    }

    private void configurarRecyclerView() {
        adapter = new CarritoAdapter();
        recyclerCarrito.setLayoutManager(new LinearLayoutManager(this));
        recyclerCarrito.setAdapter(adapter);

        adapter.setOnCarritoItemListener(new CarritoAdapter.OnCarritoItemListener() {
            @Override
            public void onIncrementar(CarritoItem item) {
                carritoManager.incrementarCantidad(item.getArticuloID());
                actualizarCarrito();
            }

            @Override
            public void onDecrementar(CarritoItem item) {
                if (item.getCantidad() > 1) {
                    carritoManager.decrementarCantidad(item.getArticuloID());
                    actualizarCarrito();
                } else {
                    confirmarEliminacion(item);
                }
            }

            @Override
            public void onEliminar(CarritoItem item) {
                confirmarEliminacion(item);
            }
        });
    }

    private void configurarBotones() {
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        btnContinuar.setOnClickListener(v -> {
            if (!carritoManager.estaVacio()) {
                mostrarDialogoLugarEntrega();
            }
        });

        findViewById(R.id.btn_agregar_productos).setOnClickListener(v -> finish());
    }

    private void mostrarDialogoLugarEntrega() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialogo_lugar_entrega, null);
        EditText etLugarEntrega = dialogView.findViewById(R.id.et_lugar_entrega);
        EditText etAnotaciones = dialogView.findViewById(R.id.et_anotaciones);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Detalles de entrega")
                .setView(dialogView)
                .setPositiveButton("Confirmar pedido", null) // null inicialmente
                .setNegativeButton("Cancelar", null)
                .create();

        dialog.setOnShowListener(dialogInterface -> {
            Button btnConfirmar = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            btnConfirmar.setOnClickListener(v -> {
                String lugarEntrega = etLugarEntrega.getText().toString().trim();
                String anotaciones = "";

                // Verificar que etAnotaciones no sea null
                if (etAnotaciones != null) {
                    anotaciones = etAnotaciones.getText().toString().trim();
                }

                if (lugarEntrega.isEmpty()) {
                    Toast.makeText(this,
                            "Por favor ingresa el lugar de entrega",
                            Toast.LENGTH_SHORT).show();
                    return; // No cerrar el dialogo
                }

                dialog.dismiss(); // Cerrar dialogo manualmente
                crearPedido(lugarEntrega, anotaciones);
            });
        });

        dialog.show();
    }

    private void crearPedido(String lugarEntrega, String anotaciones) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Debes iniciar sesion", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnContinuar.setEnabled(false);

        // Crear ID único para el pedido
        String pedidoID = "PED_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        // Crear objeto Pedido
        Pedido pedido = new Pedido();
        pedido.setId(pedidoID);
        pedido.setClienteID(currentUser.getUid());
        pedido.setLocalID(carritoManager.getLocalID());
        pedido.setEstado("pendiente");
        pedido.setTotal(carritoManager.getTotal());
        pedido.setFecha(new Date());
        pedido.setSalonEntrega(lugarEntrega);
        pedido.setAnotaciones(anotaciones);
        pedido.setLat(0.0); // Se actualizará con GPS posteriormente
        pedido.setLng(0.0); // Se actualizará con GPS posteriormente
        pedido.setCodigoQR(""); // Se generará al momento de entrega

        // Guardar pedido en Firestore
        db.collection("pedidos")
                .document(pedidoID)
                .set(pedido)
                .addOnSuccessListener(aVoid -> {
                    guardarArticulosPedido(pedidoID);
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    btnContinuar.setEnabled(true);
                    Toast.makeText(this,
                            "Error al crear pedido: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }

    private void guardarArticulosPedido(String pedidoID) {
        List<CarritoItem> items = carritoManager.getItems();
        int totalItems = items.size();
        int[] completados = {0};

        for (CarritoItem item : items) {
            ArticuloPedido articuloPedido = new ArticuloPedido(
                    item.getArticuloID(),
                    item.getCantidad(),
                    item.getSubtotal()
            );

            db.collection("pedidos")
                    .document(pedidoID)
                    .collection("articulos")
                    .add(articuloPedido)
                    .addOnSuccessListener(docRef -> {
                        completados[0]++;
                        if (completados[0] == totalItems) {
                            pedidoCreadoExitosamente(pedidoID);
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this,
                                "Error al guardar artículos",
                                Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void pedidoCreadoExitosamente(String pedidoID) {
        progressBar.setVisibility(View.GONE);

        // Limpiar carrito
        carritoManager.limpiar();

        // Mostrar diálogo de éxito
        new AlertDialog.Builder(this)
                .setTitle("Pedido creado")
                .setMessage("Tu pedido #" + pedidoID + " ha sido creado exitosamente.\n\n" +
                        "Los repartidores disponibles podrán verlo y aceptarlo.")
                .setPositiveButton("Ver mis pedidos", (dialog, which) -> {
                    Intent intent = new Intent(this, com.example.deluvery.activities.PedidosActivity.class);
                    intent.putExtra("clienteID", mAuth.getCurrentUser().getUid());
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Ir al inicio", (dialog, which) -> {
                    Intent intent = new Intent(this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                })
                .setCancelable(false)
                .show();
    }

    private void actualizarCarrito() {
        if (carritoManager.estaVacio()) {
            mostrarCarritoVacio();
        } else {
            mostrarCarritoConItems();
        }
    }

    private void mostrarCarritoVacio() {
        layoutEmpty.setVisibility(View.VISIBLE);
        recyclerCarrito.setVisibility(View.GONE);
        layoutResumen.setVisibility(View.GONE);
        btnContinuar.setEnabled(false);
        btnContinuar.setAlpha(0.5f);
    }

    private void mostrarCarritoConItems() {
        layoutEmpty.setVisibility(View.GONE);
        recyclerCarrito.setVisibility(View.VISIBLE);
        layoutResumen.setVisibility(View.VISIBLE);
        btnContinuar.setEnabled(true);
        btnContinuar.setAlpha(1.0f);

        adapter.setItems(carritoManager.getItems());

        double subtotal = carritoManager.getSubtotal();
        double costoServicio = carritoManager.getCostoServicio();
        double total = carritoManager.getTotal();

        tvSubtotal.setText(String.format(Locale.getDefault(), "$%.2f", subtotal));
        tvCostoServicio.setText(String.format(Locale.getDefault(), "$%.2f", costoServicio));
        tvTotal.setText(String.format(Locale.getDefault(), "$%.2f", total));

        TextView tvLocalNombre = findViewById(R.id.tv_carrito_local);
        if (tvLocalNombre != null && carritoManager.getLocalNombre() != null) {
            tvLocalNombre.setText(carritoManager.getLocalNombre());
        }
    }

    private void confirmarEliminacion(CarritoItem item) {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar producto")
                .setMessage("¿Deseas eliminar " + item.getNombre() + " del carrito?")
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    carritoManager.eliminarArticulo(item.getArticuloID());
                    actualizarCarrito();
                    Toast.makeText(this, "Producto eliminado", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        actualizarCarrito();
    }
}