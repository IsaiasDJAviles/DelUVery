package com.example.deluvery.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.deluvery.R;
import com.example.deluvery.models.Articulo;
import com.example.deluvery.utils.CarritoManager;
import com.example.deluvery.viewmodels.ArticuloViewModel;

import java.util.Locale;

public class MenuActivity extends AppCompatActivity {

    private ArticuloViewModel viewModel;
    private LinearLayout containerProductos;
    private ImageView imgLocalHeader;
    private TextView tvLocalNombre;
    private TextView tvCarritoCantidad;
    private Button btnVerCarrito;
    private ProgressBar progressBar;
    private TextView tvEmpty;

    private String localID;
    private String localNombre;
    private CarritoManager carritoManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        localID = getIntent().getStringExtra("localID");
        localNombre = getIntent().getStringExtra("localNombre");
        carritoManager = CarritoManager.getInstance();

        inicializarVistas();
        configurarBotones();

        viewModel = new ViewModelProvider(this).get(ArticuloViewModel.class);
        observarViewModel();

        if (localID != null) {
            viewModel.cargarArticulosDisponibles(localID);
        }

        actualizarBadgeCarrito();
    }

    private void inicializarVistas() {
        containerProductos = findViewById(R.id.container_productos);
        imgLocalHeader = findViewById(R.id.img_local_header);
        tvLocalNombre = findViewById(R.id.tv_local_nombre);
        tvCarritoCantidad = findViewById(R.id.tv_carrito_cantidad);
        btnVerCarrito = findViewById(R.id.btn_ver_carrito);
        progressBar = findViewById(R.id.progress_bar);
        tvEmpty = findViewById(R.id.tv_empty);

        tvLocalNombre.setText(localNombre);

        Glide.with(this)
                .load(R.mipmap.ic_launcher)
                .centerCrop()
                .into(imgLocalHeader);
    }

    private void configurarBotones() {
        btnVerCarrito.setOnClickListener(v -> abrirCarrito());

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
    }

    private void observarViewModel() {
        viewModel.getArticulos().observe(this, articulos -> {
            if (articulos != null && !articulos.isEmpty()) {
                mostrarProductos(articulos);
                containerProductos.setVisibility(View.VISIBLE);
                tvEmpty.setVisibility(View.GONE);
            } else {
                containerProductos.setVisibility(View.GONE);
                tvEmpty.setVisibility(View.VISIBLE);
            }
        });

        viewModel.getCargando().observe(this, cargando -> {
            progressBar.setVisibility(cargando ? View.VISIBLE : View.GONE);
        });

        viewModel.getError().observe(this, error -> {
            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_LONG).show();
                viewModel.limpiarError();
            }
        });
    }

    private void mostrarProductos(java.util.List<Articulo> articulos) {
        containerProductos.removeAllViews();

        for (Articulo articulo : articulos) {
            View itemView = LayoutInflater.from(this)
                    .inflate(R.layout.item_producto_menu, containerProductos, false);

            ImageView imgProducto = itemView.findViewById(R.id.img_producto);
            TextView tvNombre = itemView.findViewById(R.id.tv_producto_nombre);
            TextView tvDescripcion = itemView.findViewById(R.id.tv_producto_descripcion);
            TextView tvPrecio = itemView.findViewById(R.id.tv_producto_precio);
            Button btnAgregar = itemView.findViewById(R.id.btn_agregar);

            tvNombre.setText(articulo.getNombre());
            tvDescripcion.setText(articulo.getDescripcion());
            tvPrecio.setText(String.format(Locale.getDefault(), "$%.2f", articulo.getPrecio()));

            if (articulo.getImagenURL() != null && !articulo.getImagenURL().isEmpty()) {
                Glide.with(this)
                        .load(articulo.getImagenURL())
                        .placeholder(R.drawable.ic_store)
                        .into(imgProducto);
            }

            btnAgregar.setOnClickListener(v -> {
                carritoManager.agregarArticulo(articulo, localNombre);
                actualizarBadgeCarrito();
                Toast.makeText(this, "Agregado al carrito", Toast.LENGTH_SHORT).show();
            });

            containerProductos.addView(itemView);
        }
    }

    private void actualizarBadgeCarrito() {
        int cantidad = carritoManager.getCantidadTotal();
        if (cantidad > 0) {
            tvCarritoCantidad.setVisibility(View.VISIBLE);
            tvCarritoCantidad.setText(String.valueOf(cantidad));
            btnVerCarrito.setEnabled(true);
            btnVerCarrito.setAlpha(1.0f);
        } else {
            tvCarritoCantidad.setVisibility(View.GONE);
            btnVerCarrito.setEnabled(false);
            btnVerCarrito.setAlpha(0.5f);
        }
    }

    private void abrirCarrito() {
        Intent intent = new Intent(this, CarritoActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        actualizarBadgeCarrito();
    }
}