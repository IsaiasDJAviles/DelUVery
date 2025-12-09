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
import com.example.deluvery.viewmodels.ArticuloViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MenuActivity extends AppCompatActivity {

    private ArticuloViewModel viewModel;
    private LinearLayout containerProductos;
    private ImageView imgLocalHeader;
    private TextView tvLocalNombre;
    private ProgressBar progressBar;
    private TextView tvEmpty;

    private String localID;
    private String localNombre;
    private List<Articulo> carritoItems = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        // Ocultar ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Obtener datos del intent
        localID = getIntent().getStringExtra("localID");
        localNombre = getIntent().getStringExtra("localNombre");

        // Inicializar vistas
        containerProductos = findViewById(R.id.container_productos);
        imgLocalHeader = findViewById(R.id.img_local_header);
        tvLocalNombre = findViewById(R.id.tv_local_nombre);
        progressBar = findViewById(R.id.progress_bar);
        tvEmpty = findViewById(R.id.tv_empty);

        // Mostrar nombre del local
        tvLocalNombre.setText(localNombre);

        // Cargar imagen del local
        Glide.with(this)
                .load(R.mipmap.ic_launcher)
                .centerCrop()
                .into(imgLocalHeader);

        // Inicializar ViewModel
        viewModel = new ViewModelProvider(this).get(ArticuloViewModel.class);

        // Observar LiveData
        observarViewModel();

        // Cargar datos
        if (localID != null) {
            viewModel.cargarArticulosDisponibles(localID);
        }
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

    private void mostrarProductos(List<Articulo> articulos) {
        containerProductos.removeAllViews();

        for (Articulo articulo : articulos) {
            View itemView = LayoutInflater.from(this)
                    .inflate(R.layout.item_producto, containerProductos, false);

            TextView tvNombre = itemView.findViewById(R.id.tv_producto_nombre);
            TextView tvDescripcion = itemView.findViewById(R.id.tv_producto_descripcion);
            Button btnPrecio = itemView.findViewById(R.id.btn_precio);

            tvNombre.setText(articulo.getNombre());
            tvDescripcion.setText(articulo.getDescripcion());
            btnPrecio.setText(String.format(Locale.getDefault(), "$%.2f", articulo.getPrecio()));

            btnPrecio.setOnClickListener(v -> agregarAlCarrito(articulo));

            containerProductos.addView(itemView);
        }
    }

    private void agregarAlCarrito(Articulo articulo) {
        carritoItems.add(articulo);
        Toast.makeText(this,
                "Agregado: " + articulo.getNombre(),
                Toast.LENGTH_SHORT).show();

        // Aquí podrías abrir el carrito o actualizar un badge
    }
}