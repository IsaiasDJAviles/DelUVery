package com.example.deluvery;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.deluvery.models.Estudiante;
import com.example.deluvery.models.Local;
import com.example.deluvery.models.Articulo;
import com.example.deluvery.models.Pedido;
import com.example.deluvery.repositories.EstudianteRepository;
import com.example.deluvery.repositories.LocalRepository;
import com.example.deluvery.repositories.ArticuloRepository;
import com.example.deluvery.repositories.PedidoRepository;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    // Repositorios
    private EstudianteRepository estudianteRepo;
    private LocalRepository localRepo;
    private ArticuloRepository articuloRepo;
    private PedidoRepository pedidoRepo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Firebase solo se inicializa UNA VEZ
        FirebaseApp.initializeApp(this);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Ajustar insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // ---------------------------------
        // 🔥 TEST BÁSICO DE FIREBASE
        // ---------------------------------
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("test")
                .add(new TestData("Firebase conectado correctamente!"))
                .addOnSuccessListener(docRef ->
                        Log.d(TAG, "TEST FIREBASE OK → ID = " + docRef.getId())
                )
                .addOnFailureListener(e ->
                        Log.e(TAG, "TEST FIREBASE ERROR", e)
                );

        // ---------------------------------
        // 🔥 Inicialización de repositorios
        // ---------------------------------
        estudianteRepo = new EstudianteRepository();
        localRepo = new LocalRepository();
        articuloRepo = new ArticuloRepository();
        pedidoRepo = new PedidoRepository();

        // ---------------------------------
        // 🔥 Ejemplos de pruebas CRUD
        // ---------------------------------
        ejemploCrearEstudiante();
        ejemploObtenerLocalesDisponibles();
        ejemploObtenerArticulosPorLocal();
        ejemploBuscarPedidosCliente();
    }

    // Clase usada para el test inicial
    public static class TestData {
        public String mensaje;

        public TestData() {}

        public TestData(String mensaje) {
            this.mensaje = mensaje;
        }
    }

    // ==============================================================
    //                 EJEMPLO 1 → Crear estudiante
    // ==============================================================
    private void ejemploCrearEstudiante() {
        Estudiante estudiante = new Estudiante(
                "EST001",
                "Juan Pérez",
                "juan.perez@universidad.edu",
                "cliente",
                "2291234567",
                "https://ejemplo.com/foto.jpg",
                true
        );

        estudianteRepo.crearEstudiante(estudiante)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Estudiante creado exitosamente");
                    Toast.makeText(this, "Estudiante registrado", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e ->
                        Log.e(TAG, "Error al crear estudiante", e)
                );
    }

    // ==============================================================
    //                EJEMPLO 2 → Obtener locales disponibles
    // ==============================================================
    private void ejemploObtenerLocalesDisponibles() {
        localRepo.obtenerLocalesDisponibles()
                .addOnSuccessListener(querySnapshot -> {
                    List<Local> locales = LocalRepository.queryToLocalList(querySnapshot);

                    Log.d(TAG, "Locales disponibles: " + locales.size());

                    for (Local local : locales) {
                        Log.d(TAG, "- " + local.getNombre() +
                                " (Abre: " + local.getHorarioApertura() + ")");
                    }
                })
                .addOnFailureListener(e ->
                        Log.e(TAG, "Error obteniendo locales", e)
                );
    }

    // ==============================================================
    //             EJEMPLO 3 → Artículos por local
    // ==============================================================
    private void ejemploObtenerArticulosPorLocal() {
        String localID = "LOCAL001";

        articuloRepo.obtenerArticulosDisponiblesPorLocal(localID)
                .addOnSuccessListener(querySnapshot -> {
                    List<Articulo> articulos = ArticuloRepository.queryToArticuloList(querySnapshot);

                    Log.d(TAG, "Artículos: " + articulos.size());

                    for (Articulo art : articulos) {
                        Log.d(TAG, "- " + art.getNombre() + ": $" + art.getPrecio());
                    }
                })
                .addOnFailureListener(e ->
                        Log.e(TAG, "Error al obtener artículos", e)
                );
    }

    // ==============================================================
    //             EJEMPLO 4 → Pedidos por cliente
    // ==============================================================
    private void ejemploBuscarPedidosCliente() {
        String clienteID = "EST001";

        pedidoRepo.obtenerPedidosPorCliente(clienteID)
                .addOnSuccessListener(querySnapshot -> {
                    List<Pedido> pedidos = PedidoRepository.queryToPedidoList(querySnapshot);

                    Log.d(TAG, "Pedidos: " + pedidos.size());

                    for (Pedido p : pedidos) {
                        Log.d(TAG, "- Pedido " + p.getId() +
                                " | Estado: " + p.getEstado() +
                                " | Total: $" + p.getTotal());
                    }
                })
                .addOnFailureListener(e ->
                        Log.e(TAG, "Error obteniendo pedidos", e)
                );
    }
}
