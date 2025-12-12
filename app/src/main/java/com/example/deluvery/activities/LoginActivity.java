package com.example.deluvery.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.deluvery.MainActivity;
import com.example.deluvery.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";

    private EditText etEmail;
    private EditText etPassword;
    private Button btnLogin;
    private TextView tvRegistro;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        mAuth = FirebaseAuth.getInstance();

        // Si ya hay sesion, ir a Main
        if (mAuth.getCurrentUser() != null) {
            irAMain();
            return;
        }

        inicializarVistas();
        configurarBotones();
    }

    private void inicializarVistas() {
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.btn_login);
        tvRegistro = findViewById(R.id.tv_registro);
    }

    private void configurarBotones() {
        btnLogin.setOnClickListener(v -> iniciarSesion());

        // CORREGIDO: Usar RegisterActivity en lugar de SignupActivity
        tvRegistro.setOnClickListener(v -> abrirRegistro());
    }

    private void iniciarSesion() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty()) {
            etEmail.setError("Ingresa tu correo");
            etEmail.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            etPassword.setError("Ingresa tu contrasena");
            etPassword.requestFocus();
            return;
        }

        btnLogin.setEnabled(false);

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    btnLogin.setEnabled(true);

                    if (task.isSuccessful()) {
                        Log.d(TAG, "signInWithEmail:success");
                        Toast.makeText(this, "Inicio de sesion exitoso", Toast.LENGTH_SHORT).show();
                        irAMain();
                    } else {
                        Log.w(TAG, "signInWithEmail:failure", task.getException());
                        String errorMsg = "Error al iniciar sesion";
                        if (task.getException() != null) {
                            String error = task.getException().getMessage();
                            if (error != null && error.contains("password")) {
                                errorMsg = "Contrasena incorrecta";
                            } else if (error != null && error.contains("user")) {
                                errorMsg = "Usuario no encontrado";
                            }
                        }
                        Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show();
                    }
                });
    }

    // CORREGIDO: Abrir RegisterActivity
    private void abrirRegistro() {
        Intent intent = new Intent(this, SignupActivity.class);
        startActivity(intent);
    }

    private void irAMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}