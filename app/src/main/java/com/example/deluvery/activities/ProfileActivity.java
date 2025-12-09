package com.example.deluvery.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.deluvery.R;
import com.example.deluvery.models.Estudiante;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileActivity extends AppCompatActivity {

    private ImageView imgProfile;
    private Button btnNickname;
    private EditText etNombre;
    private EditText etDirecciones;
    private EditText etCuentaBanco;

    private FirebaseFirestore db;
    private String estudianteId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Ocultar ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Inicializar Firestore
        db = FirebaseFirestore.getInstance();

        // Obtener ID del estudiante
        estudianteId = getIntent().getStringExtra("estudianteId");
        if (estudianteId == null) {
            estudianteId = "EST001"; // Por defecto
        }

        // Inicializar vistas
        imgProfile = findViewById(R.id.img_profile);
        btnNickname = findViewById(R.id.btn_nickname);
        etNombre = findViewById(R.id.et_nombre);
        etDirecciones = findViewById(R.id.et_direcciones);
        etCuentaBanco = findViewById(R.id.et_cuenta_banco);

        // Cargar datos del perfil
        cargarPerfil();
    }

    private void cargarPerfil() {
        db.collection("estudiantes")
                .document(estudianteId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Estudiante estudiante = documentSnapshot.toObject(Estudiante.class);
                        if (estudiante != null) {
                            mostrarDatos(estudiante);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al cargar perfil: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void mostrarDatos(Estudiante estudiante) {
        // Mostrar nickname en el botón
        btnNickname.setText(estudiante.getCorreo().split("@")[0]);

        // Llenar campos
        etNombre.setText(estudiante.getNombre());
        etDirecciones.setText(""); // Agregar campo en el modelo si lo necesitas
        etCuentaBanco.setText(""); // Agregar campo en el modelo si lo necesitas

        // Cargar foto de perfil
        if (estudiante.getFotoURL() != null && !estudiante.getFotoURL().isEmpty()) {
            Glide.with(this)
                    .load(estudiante.getFotoURL())
                    .circleCrop()
                    .into(imgProfile);
        }
    }
}