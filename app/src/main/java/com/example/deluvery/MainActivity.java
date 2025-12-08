package com.example.deluvery;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.deluvery.activities.LocalesActivity;
import com.example.deluvery.activities.PedidosActivity;
import com.example.deluvery.activities.RepartidoresActivity;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private Button btnLocales;
    private Button btnMisPedidos;
    private Button btnRepartidores;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inicializar Firebase
        FirebaseApp.initializeApp(this);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Ajustar insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Inicializar vistas
        inicializarVistas();

        // Test de conexión Firebase
        testFirebase();
    }

    private void inicializarVistas() {
        btnLocales = findViewById(R.id.btn_locales);
        btnMisPedidos = findViewById(R.id.btn_mis_pedidos);
        btnRepartidores = findViewById(R.id.btn_repartidores);

        // Click listeners
        btnLocales.setOnClickListener(v -> abrirLocales());
        btnMisPedidos.setOnClickListener(v -> abrirMisPedidos());
        btnRepartidores.setOnClickListener(v -> abrirRepartidores());
    }

    private void abrirLocales() {
        Intent intent = new Intent(this, LocalesActivity.class);
        startActivity(intent);
    }

    private void abrirMisPedidos() {
        // Aquí deberías obtener el ID del usuario actual
        String clienteID = "EST001"; // Temporal

        Intent intent = new Intent(this, PedidosActivity.class);
        intent.putExtra("clienteID", clienteID);
        startActivity(intent);
    }

    private void abrirRepartidores() {
        Intent intent = new Intent(this, RepartidoresActivity.class);
        startActivity(intent);
    }

    private void testFirebase() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("test")
                .document("conexion_test")
                .set(new TestData("Firebase conectado desde MainActivity"))
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Firebase conectado correctamente");
                    Toast.makeText(this, "Firebase OK", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error de conexión Firebase", e);
                    Toast.makeText(this, "Error Firebase", Toast.LENGTH_SHORT).show();
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