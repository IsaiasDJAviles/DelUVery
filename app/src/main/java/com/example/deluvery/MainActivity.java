package com.example.deluvery;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.deluvery.activities.LocalesDisponiblesActivity;
import com.example.deluvery.activities.LoginActivity;
import com.example.deluvery.activities.MenuActivity;
import com.example.deluvery.activities.PedidosActivity;
import com.example.deluvery.activities.ProfileActivity;
import com.example.deluvery.activities.RepartosActivity;
import com.example.deluvery.adapters.LocalAdapter;
import com.example.deluvery.models.Local;
import com.example.deluvery.utils.CarritoManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private TextView tvUsername;
    private ImageView imgUsuarioPerfil;
    private Button btnMisPedidos;
    private Button btnRepartos;
    private RecyclerView recyclerLocales;
    private TextView tvVerTodos;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private LocalAdapter localAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inicializar Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // VERIFICAR SESION - Si no hay usuario, ir a Login
        if (mAuth.getCurrentUser() == null) {
            irALogin();
            return; // Detener ejecucion del resto del onCreate
        }

        setContentView(R.layout.activity_main);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        db = FirebaseFirestore.getInstance();

        inicializarVistas();
        configurarBotones();
        cargarLocalesDisponibles();
        cargarDatosUsuario();
    }

    // Metodo para ir a Login
    private void irALogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void inicializarVistas() {
        tvUsername = findViewById(R.id.tv_username);
        imgUsuarioPerfil = findViewById(R.id.img_usuario_perfil);
        btnMisPedidos = findViewById(R.id.btn_mis_pedidos);
        btnRepartos = findViewById(R.id.btn_repartos);
        recyclerLocales = findViewById(R.id.recycler_locales);
        tvVerTodos = findViewById(R.id.tv_ver_todos);
    }

    private void cargarDatosUsuario() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            // Mostrar nombre del usuario
            String email = currentUser.getEmail();
            if (email != null) {
                String nombre = email.split("@")[0];
                tvUsername.setText("Hola, " + nombre);
            }
        }
    }

    private void configurarBotones() {
        // Click en icono del pato para abrir perfil
        imgUsuarioPerfil.setOnClickListener(v -> {
            Intent intent = new Intent(this, ProfileActivity.class);
            startActivity(intent);
        });

        btnMisPedidos.setOnClickListener(v -> {
            FirebaseUser currentUser = mAuth.getCurrentUser();
            if (currentUser != null) {
                Intent intent = new Intent(this, PedidosActivity.class);
                intent.putExtra("clienteID", currentUser.getUid());
                startActivity(intent);
            }
        });

        btnRepartos.setOnClickListener(v -> {
            Intent intent = new Intent(this, RepartosActivity.class);
            startActivity(intent);
        });

        tvVerTodos.setOnClickListener(v -> {
            Intent intent = new Intent(this, LocalesDisponiblesActivity.class);
            startActivity(intent);
        });
    }

    private void cargarLocalesDisponibles() {
        localAdapter = new LocalAdapter();
        recyclerLocales.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recyclerLocales.setAdapter(localAdapter);

        localAdapter.setOnLocalClickListener(local -> {
            Intent intent = new Intent(MainActivity.this, MenuActivity.class);
            intent.putExtra("localID", local.getId());
            intent.putExtra("localNombre", local.getNombre());
            startActivity(intent);
        });

        db.collection("locales")
                .whereEqualTo("disponible", true)
                .limit(5)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Local> locales = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Local local = document.toObject(Local.class);
                        locales.add(local);
                    }
                    localAdapter.setLocales(locales);
                    Log.d(TAG, "Locales disponibles: " + locales.size());
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al cargar locales", e);
                });
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Verificar si el usuario sigue logueado
        if (mAuth.getCurrentUser() == null) {
            irALogin();
            return;
        }

        // Actualizar datos del usuario
        cargarDatosUsuario();

        // Actualizar UI del carrito
        CarritoManager carritoManager = CarritoManager.getInstance();
        Log.d(TAG, "Items en carrito: " + carritoManager.getCantidadTotal());
    }
}