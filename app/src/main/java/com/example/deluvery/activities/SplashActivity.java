package com.example.deluvery.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.deluvery.MainActivity;
import com.example.deluvery.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DURATION = 2500; // 2.5 segundos

    private ImageView imgLogo;
    private TextView tvAppName;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Ocultar ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Inicializar Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Inicializar vistas
        imgLogo = findViewById(R.id.img_splash_logo);
        tvAppName = findViewById(R.id.tv_splash_name);

        // Animacion de fade in
        animarLogo();

        // Navegar despues del tiempo de splash
        new Handler(Looper.getMainLooper()).postDelayed(this::navegarSiguientePantalla, SPLASH_DURATION);
    }

    private void animarLogo() {
        // Animacion fade in para el logo
        AlphaAnimation fadeIn = new AlphaAnimation(0.0f, 1.0f);
        fadeIn.setDuration(1000);
        fadeIn.setFillAfter(true);

        // Animacion fade in para el texto (con delay)
        AlphaAnimation fadeInText = new AlphaAnimation(0.0f, 1.0f);
        fadeInText.setDuration(800);
        fadeInText.setStartOffset(500); // Empieza 500ms despues
        fadeInText.setFillAfter(true);

        imgLogo.startAnimation(fadeIn);
        tvAppName.startAnimation(fadeInText);
    }

    private void navegarSiguientePantalla() {
        // Verificar si el usuario ya esta logueado
        FirebaseUser currentUser = mAuth.getCurrentUser();

        Intent intent;
        if (currentUser != null) {
            // Usuario logueado, ir a MainActivity
            intent = new Intent(this, MainActivity.class);
        } else {
            // Usuario no logueado, ir a LoginActivity
            intent = new Intent(this, LoginActivity.class);
        }

        startActivity(intent);

        // Animacion de transicion
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

        finish(); // Cerrar SplashActivity para que no se pueda volver
    }
}