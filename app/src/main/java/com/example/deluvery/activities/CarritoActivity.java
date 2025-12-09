package com.example.deluvery.activities;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.deluvery.R;
import com.example.deluvery.models.Articulo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CarritoActivity extends AppCompatActivity {

    private LinearLayout containerCarritoItems;
    private Button btnTotal;

    private List<Articulo> carritoItems;
    private double totalCarrito = 0.0;
    private double costoEnvio = 15.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_carrito);

        // Ocultar ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Inicializar vistas
        containerCarritoItems = findViewById(R.id.container_carrito_items);
        btnTotal = findViewById(R.id.btn_total);

        // Obtener items del carrito (deberías pasarlos desde MenuActivity)
        carritoItems = new ArrayList<>(); // Temporal

        // Cargar items
        cargarItemsCarrito();

        // Botón de procesar pedido
        btnTotal.setOnClickListener(v -> procesarPedido());
    }

    private void cargarItemsCarrito() {
        containerCarritoItems.removeAllViews();
        totalCarrito = 0.0;

        // Agrupar por producto y contar cantidad
        Map<String, Integer> cantidades = new HashMap<>();
        Map<String, Articulo> productos = new HashMap<>();

        for (Articulo articulo : carritoItems) {
            String id = articulo.getId();
            cantidades.put(id, cantidades.getOrDefault(id, 0) + 1);
            productos.put(id, articulo);
        }

        // Mostrar cada item
        for (Map.Entry<String, Integer> entry : cantidades.entrySet()) {
            String id = entry.getKey();
            int cantidad = entry.getValue();
            Articulo articulo = productos.get(id);

            if (articulo != null) {
                agregarItemCarrito(cantidad, articulo);
                totalCarrito += articulo.getPrecio() * cantidad;
            }
        }

        // Agregar costo de envío
        agregarItemEnvio();

        // Actualizar total
        double totalFinal = totalCarrito + costoEnvio;
        btnTotal.setText(String.format(Locale.getDefault(), "$%.2f", totalFinal));
    }

    private void agregarItemCarrito(int cantidad, Articulo articulo) {
        View itemView = LayoutInflater.from(this)
                .inflate(R.layout.item_carrito, containerCarritoItems, false);

        TextView tvNombre = itemView.findViewById(R.id.tv_item_nombre);
        Button btnPrecio = itemView.findViewById(R.id.btn_precio_item);

        double subtotal = articulo.getPrecio() * cantidad;

        tvNombre.setText(String.format(Locale.getDefault(),
                "%d - %s", cantidad, articulo.getNombre()));
        btnPrecio.setText(String.format(Locale.getDefault(), "$%.2f", subtotal));

        containerCarritoItems.addView(itemView);
    }

    private void agregarItemEnvio() {
        View itemView = LayoutInflater.from(this)
                .inflate(R.layout.item_carrito, containerCarritoItems, false);

        TextView tvNombre = itemView.findViewById(R.id.tv_item_nombre);
        Button btnPrecio = itemView.findViewById(R.id.btn_precio_item);

        tvNombre.setText("Envio");
        btnPrecio.setText(String.format(Locale.getDefault(), "$%.2f", costoEnvio));

        containerCarritoItems.addView(itemView);
    }

    private void procesarPedido() {
        if (carritoItems.isEmpty()) {
            Toast.makeText(this, "El carrito está vacío", Toast.LENGTH_SHORT).show();
            return;
        }

        // Aquí implementarías la creación del pedido en Firebase
        Toast.makeText(this, "Procesando pedido...", Toast.LENGTH_SHORT).show();

        // Luego navegarías a la pantalla de confirmación
    }
}