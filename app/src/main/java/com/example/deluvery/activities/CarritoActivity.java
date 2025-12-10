package com.example.deluvery.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.deluvery.R;
import com.example.deluvery.adapters.CarritoAdapter;
import com.example.deluvery.models.CarritoItem;
import com.example.deluvery.utils.CarritoManager;

import java.util.Locale;

public class CarritoActivity extends AppCompatActivity {

    private RecyclerView recyclerCarrito;
    private CarritoAdapter adapter;
    private TextView tvSubtotal;
    private TextView tvCostoServicio;
    private TextView tvTotal;
    private Button btnContinuar;
    private LinearLayout layoutEmpty;
    private LinearLayout layoutResumen;

    private CarritoManager carritoManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_carrito);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        carritoManager = CarritoManager.getInstance();

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
                // Aquí irías a la pantalla de confirmación/pago
                Toast.makeText(this,
                        "Funcionalidad de pago próximamente",
                        Toast.LENGTH_SHORT).show();
                // Intent intent = new Intent(this, CheckoutActivity.class);
                // startActivity(intent);
            }
        });

        findViewById(R.id.btn_agregar_productos).setOnClickListener(v -> finish());
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

        // Actualizar resumen
        double subtotal = carritoManager.getSubtotal();
        double costoServicio = carritoManager.getCostoServicio();
        double total = carritoManager.getTotal();

        tvSubtotal.setText(String.format(Locale.getDefault(), "$%.2f", subtotal));
        tvCostoServicio.setText(String.format(Locale.getDefault(), "$%.2f", costoServicio));
        tvTotal.setText(String.format(Locale.getDefault(), "$%.2f", total));

        // Actualizar título con nombre del local
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