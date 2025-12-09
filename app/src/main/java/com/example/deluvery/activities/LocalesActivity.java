package com.example.deluvery.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.deluvery.R;
import com.example.deluvery.adapters.LocalAdapter;
import com.example.deluvery.models.Local;
import com.example.deluvery.viewmodels.LocalViewModel;

public class LocalesActivity extends AppCompatActivity {

    private LocalViewModel viewModel;
    private LocalAdapter adapter;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView tvEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_locales);

        // Ocultar ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Inicializar vistas
        recyclerView = findViewById(R.id.recycler_locales);
        progressBar = findViewById(R.id.progress_bar);
        tvEmpty = findViewById(R.id.tv_empty);

        // Configurar RecyclerView
        setupRecyclerView();

        // Inicializar ViewModel
        viewModel = new ViewModelProvider(this).get(LocalViewModel.class);

        // Observar LiveData
        observarViewModel();

        // Cargar datos
        viewModel.cargarLocalesDisponibles();
    }

    private void setupRecyclerView() {
        adapter = new LocalAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        adapter.setOnLocalClickListener(new LocalAdapter.OnLocalClickListener() {
            @Override
            public void onLocalClick(Local local) {
                abrirMenu(local);
            }
        });
    }

    private void observarViewModel() {
        viewModel.getLocalesDisponibles().observe(this, locales -> {
            if (locales != null && !locales.isEmpty()) {
                adapter.setLocales(locales);
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

    private void abrirMenu(Local local) {
        Intent intent = new Intent(this, MenuActivity.class);
        intent.putExtra("localID", local.getId());
        intent.putExtra("localNombre", local.getNombre());
        startActivity(intent);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}