package com.example.deluvery.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.deluvery.R;
import com.example.deluvery.adapters.LocalAdapter;
import com.example.deluvery.models.Local;
import com.example.deluvery.viewmodels.LocalViewModel;

/**
 * Activity que muestra todos los locales disponibles en formato de grid
 * Se accede desde MainActivity cuando el usuario hace clic en "Ver todos"
 */
public class LocalesDisponiblesActivity extends AppCompatActivity {

    private static final String TAG = "LocalesDisponibles";

    // Views
    private RecyclerView recyclerLocales;
    private ProgressBar progressBar;
    private TextView tvEmpty;

    // Adapters y ViewModels
    private LocalAdapter localAdapter;
    private LocalViewModel localViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_locales_disponibles);

        // Configurar ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Locales Disponibles");
        }

        inicializarVistas();
        configurarRecyclerView();
        configurarViewModel();
        cargarLocales();
    }

    /**
     * Inicializa todas las vistas de la Activity
     */
    private void inicializarVistas() {
        recyclerLocales = findViewById(R.id.recycler_locales);
        progressBar = findViewById(R.id.progress_bar);
        tvEmpty = findViewById(R.id.tv_empty);
    }

    /**
     * Configura el RecyclerView con GridLayoutManager (2 columnas)
     */
    private void configurarRecyclerView() {
        localAdapter = new LocalAdapter();

        // Grid de 2 columnas
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        recyclerLocales.setLayoutManager(gridLayoutManager);
        recyclerLocales.setAdapter(localAdapter);

        // Configurar click listener
        localAdapter.setOnLocalClickListener(local -> {
            abrirMenuLocal(local);
        });
    }

    /**
     * Configura el ViewModel y sus observadores
     */
    private void configurarViewModel() {
        localViewModel = new LocalViewModel();

        // Observar la lista de locales disponibles
        localViewModel.getLocalesDisponibles().observe(this, locales -> {
            if (locales != null && !locales.isEmpty()) {
                localAdapter.setLocales(locales);
                recyclerLocales.setVisibility(View.VISIBLE);
                tvEmpty.setVisibility(View.GONE);
                Log.d(TAG, "Locales cargados: " + locales.size());
            } else {
                recyclerLocales.setVisibility(View.GONE);
                tvEmpty.setVisibility(View.VISIBLE);
            }
        });

        // Observar estado de carga
        localViewModel.getCargando().observe(this, cargando -> {
            progressBar.setVisibility(cargando ? View.VISIBLE : View.GONE);
        });

        // Observar errores
        localViewModel.getError().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Log.e(TAG, "Error: " + error);
                tvEmpty.setText(error);
                tvEmpty.setVisibility(View.VISIBLE);
            }
        });
    }

    /**
     * Carga todos los locales disponibles
     */
    private void cargarLocales() {
        localViewModel.cargarLocalesDisponibles();
    }

    /**
     * Abre la pantalla de menú del local seleccionado
     */
    private void abrirMenuLocal(Local local) {
        Intent intent = new Intent(this, MenuActivity.class);
        intent.putExtra("localID", local.getId());
        intent.putExtra("localNombre", local.getNombre());
        startActivity(intent);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}