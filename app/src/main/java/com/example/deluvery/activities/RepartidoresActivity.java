package com.example.deluvery.activities;

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
import com.example.deluvery.adapters.EstudianteAdapter;
import com.example.deluvery.models.Estudiante;
import com.example.deluvery.viewmodels.EstudianteViewModel;

public class RepartidoresActivity extends AppCompatActivity {

    private EstudianteViewModel viewModel;
    private EstudianteAdapter adapter;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView tvEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_repartidores);

        // Configurar toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Repartidores Activos");
        }

        // Inicializar vistas
        recyclerView = findViewById(R.id.recycler_repartidores);
        progressBar = findViewById(R.id.progress_bar);
        tvEmpty = findViewById(R.id.tv_empty);

        // Configurar RecyclerView
        setupRecyclerView();

        // Inicializar ViewModel
        viewModel = new ViewModelProvider(this).get(EstudianteViewModel.class);

        // Observar LiveData
        observarViewModel();

        // Cargar datos
        viewModel.cargarRepartidoresActivos();
    }

    private void setupRecyclerView() {
        adapter = new EstudianteAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        adapter.setOnEstudianteClickListener(new EstudianteAdapter.OnEstudianteClickListener() {
            @Override
            public void onEstudianteClick(Estudiante estudiante) {
                mostrarInfoRepartidor(estudiante);
            }

            @Override
            public void onEstudianteLongClick(Estudiante estudiante) {
                // Acción adicional
            }
        });
    }

    private void observarViewModel() {
        viewModel.getRepartidoresActivos().observe(this, repartidores -> {
            if (repartidores != null && !repartidores.isEmpty()) {
                adapter.setEstudiantes(repartidores);
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

    private void mostrarInfoRepartidor(Estudiante repartidor) {
        String info = repartidor.getNombre() + "\n" + repartidor.getTelefono();
        Toast.makeText(this, info, Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}