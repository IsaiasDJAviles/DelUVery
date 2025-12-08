package com.example.deluvery.repositories;

import android.util.Log;

import com.example.deluvery.models.RepartidorActividad;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class RepartidorActividadRepository {

    private static final String TAG = "RepartidorActividadRepo";
    private static final String COLLECTION_NAME = "repartidor_actividad";

    private final FirebaseFirestore db;

    public RepartidorActividadRepository() {
        this.db = FirebaseFirestore.getInstance();
    }

    // CREATE - Registrar actividad del repartidor
    public Task<Void> registrarActividad(RepartidorActividad actividad) {
        return db.collection(COLLECTION_NAME)
                .document(actividad.getId())
                .set(actividad)
                .addOnSuccessListener(aVoid ->
                        Log.d(TAG, "Actividad registrada: " + actividad.getId()))
                .addOnFailureListener(e ->
                        Log.e(TAG, "Error al registrar actividad", e));
    }

    // CREATE - Registrar actividad en lote (batch)
    public void registrarActividadBatch(List<RepartidorActividad> actividades) {
        for (RepartidorActividad act : actividades) {
            db.collection(COLLECTION_NAME)
                    .add(act)
                    .addOnSuccessListener(docRef ->
                            Log.d(TAG, "Actividad batch agregada"))
                    .addOnFailureListener(e ->
                            Log.e(TAG, "Error en batch", e));
        }
    }

    // READ - Obtener actividad por ID
    public Task<DocumentSnapshot> obtenerActividadPorId(String id) {
        return db.collection(COLLECTION_NAME)
                .document(id)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        Log.d(TAG, "Actividad encontrada: " + id);
                    }
                })
                .addOnFailureListener(e ->
                        Log.e(TAG, "Error al obtener actividad", e));
    }

    // READ - Obtener actividades por repartidor
    public Task<QuerySnapshot> obtenerActividadesPorRepartidor(String repartidorID) {
        return db.collection(COLLECTION_NAME)
                .whereEqualTo("repartidorID", repartidorID)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(querySnapshot ->
                        Log.d(TAG, "Actividades del repartidor: " + querySnapshot.size()))
                .addOnFailureListener(e ->
                        Log.e(TAG, "Error al obtener actividades", e));
    }

    // READ - Obtener actividades por pedido
    public Task<QuerySnapshot> obtenerActividadesPorPedido(String pedidoID) {
        return db.collection(COLLECTION_NAME)
                .whereEqualTo("pedidoID", pedidoID)
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(querySnapshot ->
                        Log.d(TAG, "Actividades del pedido: " + querySnapshot.size()))
                .addOnFailureListener(e ->
                        Log.e(TAG, "Error al obtener actividades por pedido", e));
    }

    // READ - Obtener últimas actividades del repartidor (últimas N)
    public Task<QuerySnapshot> obtenerUltimasActividades(String repartidorID, int limite) {
        return db.collection(COLLECTION_NAME)
                .whereEqualTo("repartidorID", repartidorID)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(limite)
                .get()
                .addOnSuccessListener(querySnapshot ->
                        Log.d(TAG, "Últimas " + limite + " actividades obtenidas"))
                .addOnFailureListener(e ->
                        Log.e(TAG, "Error al obtener últimas actividades", e));
    }

    // READ - Obtener actividades con movimiento detectado
    public Task<QuerySnapshot> obtenerActividadesConMovimiento(String repartidorID) {
        return db.collection(COLLECTION_NAME)
                .whereEqualTo("repartidorID", repartidorID)
                .whereEqualTo("movimientoDetectado", true)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(querySnapshot ->
                        Log.d(TAG, "Actividades con movimiento: " + querySnapshot.size()))
                .addOnFailureListener(e ->
                        Log.e(TAG, "Error al filtrar movimientos", e));
    }

    // READ - Obtener actividades por rango de tiempo
    public Task<QuerySnapshot> obtenerActividadesPorRango(String repartidorID, long inicio, long fin) {
        return db.collection(COLLECTION_NAME)
                .whereEqualTo("repartidorID", repartidorID)
                .whereGreaterThanOrEqualTo("timestamp", inicio)
                .whereLessThanOrEqualTo("timestamp", fin)
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(querySnapshot ->
                        Log.d(TAG, "Actividades en rango: " + querySnapshot.size()))
                .addOnFailureListener(e ->
                        Log.e(TAG, "Error al filtrar por rango", e));
    }

    // READ - Obtener actividad más reciente del repartidor
    public Task<QuerySnapshot> obtenerActividadMasReciente(String repartidorID) {
        return db.collection(COLLECTION_NAME)
                .whereEqualTo("repartidorID", repartidorID)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        Log.d(TAG, "Actividad más reciente encontrada");
                    }
                })
                .addOnFailureListener(e ->
                        Log.e(TAG, "Error al obtener actividad reciente", e));
    }

    // DELETE - Eliminar actividad
    public Task<Void> eliminarActividad(String id) {
        return db.collection(COLLECTION_NAME)
                .document(id)
                .delete()
                .addOnSuccessListener(aVoid ->
                        Log.d(TAG, "Actividad eliminada: " + id))
                .addOnFailureListener(e ->
                        Log.e(TAG, "Error al eliminar actividad", e));
    }

    // DELETE - Eliminar actividades antiguas (más de N días)
    public Task<QuerySnapshot> eliminarActividadesAntiguas(long timestampLimite) {
        return db.collection(COLLECTION_NAME)
                .whereLessThan("timestamp", timestampLimite)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        doc.getReference().delete()
                                .addOnSuccessListener(aVoid ->
                                        Log.d(TAG, "Actividad antigua eliminada"))
                                .addOnFailureListener(e ->
                                        Log.e(TAG, "Error al eliminar actividad antigua", e));
                    }
                })
                .addOnFailureListener(e ->
                        Log.e(TAG, "Error al buscar actividades antiguas", e));
    }

    // UTILITY - Calcular magnitud del acelerómetro
    public static double calcularMagnitud(RepartidorActividad actividad) {
        return Math.sqrt(
                Math.pow(actividad.getX(), 2) +
                        Math.pow(actividad.getY(), 2) +
                        Math.pow(actividad.getZ(), 2)
        );
    }

    // UTILITY - Determinar si hay movimiento significativo
    public static boolean esMovimientoSignificativo(RepartidorActividad actividad, double umbral) {
        double magnitud = calcularMagnitud(actividad);
        return magnitud > umbral;
    }

    // UTILITY - Calcular promedio de movimiento en lista
    public static double calcularPromedioMovimiento(List<RepartidorActividad> actividades) {
        if (actividades.isEmpty()) return 0.0;

        double suma = 0.0;
        for (RepartidorActividad act : actividades) {
            suma += calcularMagnitud(act);
        }
        return suma / actividades.size();
    }

    // UTILITY - Contar actividades con movimiento
    public static int contarMovimientos(List<RepartidorActividad> actividades) {
        int count = 0;
        for (RepartidorActividad act : actividades) {
            if (act.isMovimientoDetectado()) {
                count++;
            }
        }
        return count;
    }

    // UTILITY - Convertir DocumentSnapshot a RepartidorActividad
    public static RepartidorActividad documentToActividad(DocumentSnapshot doc) {
        if (doc.exists()) {
            return doc.toObject(RepartidorActividad.class);
        }
        return null;
    }

    // UTILITY - Convertir QuerySnapshot a lista de RepartidorActividad
    public static List<RepartidorActividad> queryToActividadList(QuerySnapshot querySnapshot) {
        List<RepartidorActividad> actividades = new ArrayList<>();
        for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
            RepartidorActividad actividad = doc.toObject(RepartidorActividad.class);
            if (actividad != null) {
                actividades.add(actividad);
            }
        }
        return actividades;
    }
}