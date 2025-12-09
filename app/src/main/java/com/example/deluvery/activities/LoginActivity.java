package com.example.deluvery.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.deluvery.MainActivity;
import com.example.deluvery.R;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {

    private EditText etUsername;
    private EditText etPassword;
    private Button btnLogin;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Ocultar ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Inicializar Firestore
        db = FirebaseFirestore.getInstance();

        // Inicializar vistas
        etUsername = findViewById(R.id.et_username);
        etPassword = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.btn_login);

        // Configurar botón
        btnLogin.setOnClickListener(v -> iniciarSesion());
    }

    private void iniciarSesion() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Validaciones
        if (TextUtils.isEmpty(username)) {
            etUsername.setError("Ingresa tu usuario");
            etUsername.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Ingresa tu contraseña");
            etPassword.requestFocus();
            return;
        }

        // Aquí deberías implementar autenticación real con Firebase Auth
        // Por ahora, hacemos login simple buscando en estudiantes
        buscarEstudiante(username, password);
    }

    private void buscarEstudiante(String username, String password) {
        db.collection("estudiantes")
                .whereEqualTo("correo", username)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        // Usuario encontrado
                        Toast.makeText(this, "Bienvenido!", Toast.LENGTH_SHORT).show();

                        // Ir a MainActivity
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        intent.putExtra("userId", querySnapshot.getDocuments().get(0).getId());
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(this, "Usuario no encontrado", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}