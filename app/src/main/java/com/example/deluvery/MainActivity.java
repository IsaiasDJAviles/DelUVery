package com.example.deluvery;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.deluvery.activities.LocalesActivity;
import com.example.deluvery.activities.PedidosActivity;
import com.example.deluvery.activities.ProfileActivity;
import com.example.deluvery.activities.RepartidoresActivity;
import com.example.deluvery.adapters.LocalAdapter;
import com.example.deluvery.models.Local;
import com.example.deluvery.utils.DataSeeder;
import com.example.deluvery.viewmodels.LocalViewModel;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    // Vistas del header
    private ImageView imgUsuarioPerfil;
    private TextView tvUsuarioNombre;
    private TextView tvUbicacion;

    // Tarjetas de acceso rápido
    private CardView cardMisPedidos;
    private CardView cardRepartidores;

    // RecyclerView y componentes
    private RecyclerView recyclerLocales;
    private LocalAdapter localAdapter;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private TextView tvVerTodos;

    // ViewModel
    private LocalViewModel localViewModel;

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inicializar Firebase
        FirebaseApp.initializeApp(this);
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Ocultar ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Ajustar insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Inicializar vistas
        inicializarVistas();

        // Configurar RecyclerView
        setupRecyclerView();

        // Inicializar ViewModel
        localViewModel = new ViewModelProvider(this).get(LocalViewModel.class);

        // Observar LiveData
        observarViewModel();

        // Cargar datos del usuario
        cargarDatosUsuario();

        // Cargar locales disponibles
        localViewModel.cargarLocalesDisponibles();

        // Configurar listeners
        configurarListeners();

        // Test de conexión Firebase
        testFirebase();
    }

    private void inicializarVistas() {
        // Header
        imgUsuarioPerfil = findViewById(R.id.img_usuario_perfil);
        tvUsuarioNombre = findViewById(R.id.tv_usuario_nombre);
        tvUbicacion = findViewById(R.id.tv_ubicacion);

        // Tarjetas de acceso rápido
        cardMisPedidos = findViewById(R.id.card_mis_pedidos);
        cardRepartidores = findViewById(R.id.card_repartidores);

        // RecyclerView y componentes
        recyclerLocales = findViewById(R.id.recycler_locales);
        progressBar = findViewById(R.id.progress_bar);
        tvEmpty = findViewById(R.id.tv_empty);
        tvVerTodos = findViewById(R.id.tv_ver_todos);
    }

    private void setupRecyclerView() {
        localAdapter = new LocalAdapter();
        recyclerLocales.setLayoutManager(new LinearLayoutManager(this));
        recyclerLocales.setAdapter(localAdapter);
        recyclerLocales.setNestedScrollingEnabled(false);

        // Configurar listener del adapter
        localAdapter.setOnLocalClickListener(new LocalAdapter.OnLocalClickListener() {
            @Override
            public void onLocalClick(Local local) {
                abrirMenuLocal(local);
            }
        });
    }

    private void observarViewModel() {
        // Observar locales disponibles
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
            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_LONG).show();
                localViewModel.limpiarError();
            }
        });
    }

    private void cargarDatosUsuario() {
        if (currentUser != null) {
            // Obtener nombre del usuario
            String nombreUsuario = currentUser.getDisplayName();
            if (nombreUsuario != null && !nombreUsuario.isEmpty()) {
                tvUsuarioNombre.setText("Hola, " + nombreUsuario);
            } else {
                String email = currentUser.getEmail();
                if (email != null) {
                    String nombre = email.split("@")[0];
                    tvUsuarioNombre.setText("Hola, " + nombre);
                }
            }

            // Cargar foto de perfil
            if (currentUser.getPhotoUrl() != null) {
                Glide.with(this)
                        .load(currentUser.getPhotoUrl())
                        .circleCrop()
                        .placeholder(R.mipmap.ic_launcher)
                        .into(imgUsuarioPerfil);
            }
        } else {
            tvUsuarioNombre.setText("Bienvenido");
        }
    }

    private void configurarListeners() {
        // Click en tarjeta Mis Pedidos
        cardMisPedidos.setOnClickListener(v -> abrirMisPedidos());

        // Click en tarjeta Repartidores
        cardRepartidores.setOnClickListener(v -> abrirRepartidores());

        // Click en Ver Todos
        tvVerTodos.setOnClickListener(v -> abrirTodosLocales());

        // Click en foto de perfil
        imgUsuarioPerfil.setOnClickListener(v -> abrirPerfil());
    }

    private void abrirMenuLocal(Local local) {
        if (!local.isDisponible()) {
            Toast.makeText(this,
                    local.getNombre() + " está cerrado actualmente",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(this, com.example.deluvery.activities.MenuActivity.class);
        intent.putExtra("localID", local.getId());
        intent.putExtra("localNombre", local.getNombre());
        startActivity(intent);
    }

    private void abrirMisPedidos() {
        String clienteID = currentUser != null ? currentUser.getUid() : "EST001";
        Intent intent = new Intent(this, PedidosActivity.class);
        intent.putExtra("clienteID", clienteID);
        startActivity(intent);
    }

    private void abrirRepartidores() {
        Intent intent = new Intent(this, RepartidoresActivity.class);
        startActivity(intent);
    }

    private void abrirTodosLocales() {
        Intent intent = new Intent(this, LocalesActivity.class);
        startActivity(intent);
    }

    private void abrirPerfil() {
        Intent intent = new Intent(this, ProfileActivity.class);
        startActivity(intent);
    }

    private void testFirebase() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("test")
                .document("conexion_test")
                .set(new TestData("Firebase conectado desde MainActivity"))
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Firebase conectado correctamente");
                    // Verificar si hay datos, si no, poblar
                    verificarYPoblarDatos();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error de conexión Firebase", e);
                });
    }

    private void verificarYPoblarDatos() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("locales")
                .limit(1)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (querySnapshot.isEmpty()) {
                        Log.d(TAG, "No hay datos, poblando base de datos...");
                        DataSeeder seeder = new DataSeeder();
                        seeder.poblarDatosDePrueba();

                        // Recargar después de 2 segundos
                        recyclerLocales.postDelayed(() -> {
                            localViewModel.cargarLocalesDisponibles();
                        }, 2000);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al verificar datos", e);
                });
    }

    public static class TestData {
        public String mensaje;

        public TestData() {}

        public TestData(String mensaje) {
            this.mensaje = mensaje;
        }
    }
}