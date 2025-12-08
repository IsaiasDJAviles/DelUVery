package com.example.deluvery.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.deluvery.R;
import com.example.deluvery.adapters.ArticuloAdapter;
import com.example.deluvery.models.Articulo;
import com.example.deluvery.viewmodels.ArticuloViewModel;

public class MenuActivity extends AppCompatActivity {

    private ArticuloViewModel viewModel;
    private ArticuloAdapter adapter;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView tvEmpty;

    private String localID;
    private String localNombre;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        // Obtener datos del intent
        localID = getIntent().getStringExtra("localID");
        localNombre = getIntent().getStringExtra("localNombre");

        // Configurar toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Menú - " + localNombre);
        }

        // Inicializar vistas
        recyclerView = findViewById(R.id.recycler_menu);
        progressBar = findViewById(R.id.progress_bar);
        tvEmpty = findViewById(R.id.tv_empty);

        // Configurar RecyclerView
        setupRecyclerView();

        // Inicializar ViewModel
        viewModel = new ViewModelProvider(this).get(ArticuloViewModel.class);

        // Observar LiveData
        observarViewModel();

        // Cargar datos
        if (localID != null) {
            viewModel.cargarArticulosDisponibles(localID);
        }
    }

    private void setupRecyclerView() {
        adapter = new ArticuloAdapter();
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerView.setAdapter(adapter);

        adapter.setOnArticuloClickListener(new ArticuloAdapter.OnArticuloClickListener() {
            @Override
            public void onArticuloClick(Articulo articulo) {
                mostrarDetallesArticulo(articulo);
            }

            @Override
            public void onAgregarCarritoClick(Articulo articulo) {
                agregarAlCarrito(articulo);
            }
        });
    }

    private void observarViewModel() {
        viewModel.getArticulos().observe(this, articulos -> {
            if (articulos != null && !articulos.isEmpty()) {
                adapter.setArticulos(articulos);
                recyclerView.setVisibility(View.VISIBLE);
                tvEmpty.setVisibility(View.GONE);
            } else {
                recyclerView.setVisibility(View.GONE);
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

    private void mostrarDetallesArticulo(Articulo articulo) {
        Toast.makeText(this,
                articulo.getNombre() + " - $" + articulo.getPrecio(),
                Toast.LENGTH_SHORT).show();
    }

    private void agregarAlCarrito(Articulo articulo) {
        // Aquí implementarías la lógica del carrito
        Toast.makeText(this,
                "Agregado: " + articulo.getNombre(),
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}