//package com.example.deluvery.utils;
//
//import android.util.Log;
//
//import com.example.deluvery.models.Articulo;
//import com.example.deluvery.models.Estudiante;
//import com.example.deluvery.models.Local;
//import com.google.firebase.firestore.FirebaseFirestore;
//
//import java.util.ArrayList;
//import java.util.List;
//
//public class DataSeeder {
//
//    private static final String TAG = "DataSeeder";
//    private final FirebaseFirestore db;
//
//    public DataSeeder() {
//        this.db = FirebaseFirestore.getInstance();
//    }
//
//    // Método principal para poblar todos los datos
//    public void poblarDatosDePrueba() {
//        poblarLocales();
//        poblarEstudiantes();
//        poblarArticulos();
//    }
//
//    // Poblar locales de prueba
//    public void poblarLocales() {
//        List<Local> locales = new ArrayList<>();
//
//        locales.add(new Local(
//                "LOC001",
//                "Cafetería Central",
//                "07:00",
//                "20:00",
//                true
//        ));
//
//        locales.add(new Local(
//                "LOC002",
//                "Comedor Universitario",
//                "08:00",
//                "18:00",
//                true
//        ));
//
//        locales.add(new Local(
//                "LOC003",
//                "Snack UV",
//                "09:00",
//                "17:00",
//                true
//        ));
//
//        locales.add(new Local(
//                "LOC004",
//                "Papelería La Torre",
//                "08:00",
//                "16:00",
//                false
//        ));
//
//        locales.add(new Local(
//                "LOC005",
//                "Tienda de Electrónica",
//                "10:00",
//                "19:00",
//                true
//        ));
//
//        locales.add(new Local(
//                "LOC006",
//                "Jugos y Licuados",
//                "07:30",
//                "15:00",
//                true
//        ));
//
//        // Guardar en Firestore
//        for (Local local : locales) {
//            db.collection("locales")
//                    .document(local.getId())
//                    .set(local)
//                    .addOnSuccessListener(aVoid ->
//                            Log.d(TAG, "Local creado: " + local.getNombre()))
//                    .addOnFailureListener(e ->
//                            Log.e(TAG, "Error al crear local", e));
//        }
//    }
//
//    // Poblar estudiantes de prueba
//    public void poblarEstudiantes() {
//        List<Estudiante> estudiantes = new ArrayList<>();
//
//        // Clientes
//        estudiantes.add(new Estudiante(
//                "EST001",
//                "Juan Pérez",
//                "juan.perez@uv.mx",
//                "cliente",
//                "2291234567",
//                "",
//                true
//        ));
//
//        estudiantes.add(new Estudiante(
//                "EST002",
//                "María González",
//                "maria.gonzalez@uv.mx",
//                "cliente",
//                "2291234568",
//                "",
//                true
//        ));
//
//        // Repartidores
//        estudiantes.add(new Estudiante(
//                "REP001",
//                "Carlos Ramírez",
//                "carlos.ramirez@uv.mx",
//                "repartidor",
//                "2291234569",
//                "",
//                true
//        ));
//
//        estudiantes.add(new Estudiante(
//                "REP002",
//                "Ana López",
//                "ana.lopez@uv.mx",
//                "repartidor",
//                "2291234570",
//                "",
//                true
//        ));
//
//        estudiantes.add(new Estudiante(
//                "REP003",
//                "Pedro Martínez",
//                "pedro.martinez@uv.mx",
//                "repartidor",
//                "2291234571",
//                "",
//                false
//        ));
//
//        // Guardar en Firestore
//        for (Estudiante estudiante : estudiantes) {
//            db.collection("estudiantes")
//                    .document(estudiante.getId())
//                    .set(estudiante)
//                    .addOnSuccessListener(aVoid ->
//                            Log.d(TAG, "Estudiante creado: " + estudiante.getNombre()))
//                    .addOnFailureListener(e ->
//                            Log.e(TAG, "Error al crear estudiante", e));
//        }
//    }
//
//    // Poblar artículos de prueba
//    public void poblarArticulos() {
//        List<Articulo> articulos = new ArrayList<>();
//
//        // Artículos para Cafetería Central (LOC001)
//        articulos.add(new Articulo(
//                "ART001",
//                "Café Americano",
//                25.0,
//                "Café recién hecho, 250ml",
//                "",
//                "LOC001",
//                true
//        ));
//
//        articulos.add(new Articulo(
//                "ART002",
//                "Cappuccino",
//                35.0,
//                "Café con leche espumosa",
//                "",
//                "LOC001",
//                true
//        ));
//
//        articulos.add(new Articulo(
//                "ART003",
//                "Sandwich de Jamón",
//                45.0,
//                "Pan integral con jamón y queso",
//                "",
//                "LOC001",
//                true
//        ));
//
//        // Artículos para Comedor Universitario (LOC002)
//        articulos.add(new Articulo(
//                "ART004",
//                "Menú del Día",
//                60.0,
//                "Comida completa: sopa, guisado, arroz y postre",
//                "",
//                "LOC002",
//                true
//        ));
//
//        articulos.add(new Articulo(
//                "ART005",
//                "Enchiladas",
//                50.0,
//                "3 enchiladas con crema y queso",
//                "",
//                "LOC002",
//                true
//        ));
//
//        // Artículos para Snack UV (LOC003)
//        articulos.add(new Articulo(
//                "ART006",
//                "Papas Fritas",
//                20.0,
//                "Bolsa de papas grandes",
//                "",
//                "LOC003",
//                true
//        ));
//
//        articulos.add(new Articulo(
//                "ART007",
//                "Refresco",
//                15.0,
//                "Refresco de 355ml",
//                "",
//                "LOC003",
//                true
//        ));
//
//        articulos.add(new Articulo(
//                "ART008",
//                "Hot Dog",
//                30.0,
//                "Hot dog con papas",
//                "",
//                "LOC003",
//                true
//        ));
//
//        // Artículos para Papelería (LOC004)
//        articulos.add(new Articulo(
//                "ART009",
//                "Cuaderno 100 hojas",
//                35.0,
//                "Cuaderno profesional",
//                "",
//                "LOC004",
//                false
//        ));
//
//        articulos.add(new Articulo(
//                "ART010",
//                "Bolígrafos (3 piezas)",
//                15.0,
//                "Pack de 3 bolígrafos azules",
//                "",
//                "LOC004",
//                false
//        ));
//
//        // Artículos para Tienda de Electrónica (LOC005)
//        articulos.add(new Articulo(
//                "ART011",
//                "Cable USB-C",
//                80.0,
//                "Cable de 1 metro",
//                "",
//                "LOC005",
//                true
//        ));
//
//        articulos.add(new Articulo(
//                "ART012",
//                "Audífonos Básicos",
//                120.0,
//                "Audífonos con cable jack 3.5mm",
//                "",
//                "LOC005",
//                true
//        ));
//
//        // Artículos para Jugos y Licuados (LOC006)
//        articulos.add(new Articulo(
//                "ART013",
//                "Jugo de Naranja",
//                25.0,
//                "Jugo natural de naranja 500ml",
//                "",
//                "LOC006",
//                true
//        ));
//
//        articulos.add(new Articulo(
//                "ART014",
//                "Licuado de Plátano",
//                30.0,
//                "Licuado con leche y plátano",
//                "",
//                "LOC006",
//                true
//        ));
//
//        articulos.add(new Articulo(
//                "ART015",
//                "Smoothie de Fresa",
//                35.0,
//                "Smoothie con yogurt y fresa",
//                "",
//                "LOC006",
//                true
//        ));
//
//        // Guardar en Firestore
//        for (Articulo articulo : articulos) {
//            db.collection("articulos")
//                    .document(articulo.getId())
//                    .set(articulo)
//                    .addOnSuccessListener(aVoid ->
//                            Log.d(TAG, "Artículo creado: " + articulo.getNombre()))
//                    .addOnFailureListener(e ->
//                            Log.e(TAG, "Error al crear artículo", e));
//        }
//    }
//
//    // Método para limpiar todas las colecciones
//    public void limpiarDatos() {
//        limpiarColeccion("locales");
//        limpiarColeccion("estudiantes");
//        limpiarColeccion("articulos");
//    }
//
//    private void limpiarColeccion(String coleccion) {
//        db.collection(coleccion)
//                .get()
//                .addOnSuccessListener(querySnapshot -> {
//                    querySnapshot.getDocuments().forEach(document ->
//                            document.getReference().delete()
//                    );
//                    Log.d(TAG, "Colección limpiada: " + coleccion);
//                })
//                .addOnFailureListener(e ->
//                        Log.e(TAG, "Error al limpiar colección: " + coleccion, e));
//    }
//}