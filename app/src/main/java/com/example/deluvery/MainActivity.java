package com.example.deluvery;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.deluvery.activities.LocalesDisponiblesActivity;
import com.example.deluvery.activities.MenuActivity;
import com.example.deluvery.activities.PedidosActivity;
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
        setContentView(R.layout.activity_main);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        inicializarVistas();
        configurarBotones();
        cargarLocalesDisponibles();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            tvUsername.setText("Hola, " + currentUser.getEmail().split("@")[0]);
        }
    }

    private void inicializarVistas() {
        tvUsername = findViewById(R.id.tv_username);
        btnMisPedidos = findViewById(R.id.btn_mis_pedidos);
        btnRepartos = findViewById(R.id.btn_repartos);
        recyclerLocales = findViewById(R.id.recycler_locales);
        tvVerTodos = findViewById(R.id.tv_ver_todos);
    }

    private void configurarBotones() {
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
        // Actualizar UI si es necesario
        CarritoManager carritoManager = CarritoManager.getInstance();
        Log.d(TAG, "Items en carrito: " + carritoManager.getCantidadTotal());
    }
}