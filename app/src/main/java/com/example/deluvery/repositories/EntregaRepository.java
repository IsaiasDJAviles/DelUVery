package com.example.deluvery.repositories;

import android.util.Log;

import com.example.deluvery.models.Entrega;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class EntregaRepository {

    private static final String TAG = "EntregaRepository";
    private static final String COLLECTION_NAME = "entregas";

    private final FirebaseFirestore db;

    public EntregaRepository() {
        this.db = FirebaseFirestore.getInstance();
    }

    // CREATE - Registrar nueva entrega
    public Task<Void> registrarEntrega(Entrega entrega) {
        return db.collection(COLLECTION_NAME)
                .document(entrega.getId())
                .set(entrega)
                .addOnSuccessListener(aVoid ->
                        Log.d(TAG, "Entrega registrada: " + entrega.getId()))
                .addOnFailureListener(e ->
                        Log.e(TAG, "Error al registrar entrega", e));
    }

    // READ - Obtener entrega por ID
    public Task<DocumentSnapshot> obtenerEntregaPorId(String id) {
        return db.collection(COLLECTION_NAME)
                .document(id)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        Log.d(TAG, "Entrega encontrada: " + id);
                    }
                })
                .addOnFailureListener(e ->
                        Log.e(TAG, "Error al obtener entrega", e));
    }

    // READ - Obtener entrega por pedido
    public Task<QuerySnapshot> obtenerEntregaPorPedido(String pedidoID) {
        return db.collection(COLLECTION_NAME)
                .whereEqualTo("pedidoID", pedidoID)
                .limit(1)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        Log.d(TAG, "Entrega del pedido encontrada");
                    }
                })
                .addOnFailureListener(e ->
                        Log.e(TAG, "Error al obtener entrega por pedido", e));
    }

    // READ - Obtener entregas por repartidor
    public Task<QuerySnapshot> obtenerEntregasPorRepartidor(String repartidorID) {
        return db.collection(COLLECTION_NAME)
                .whereEqualTo("repartidorID", repartidorID)
                .orderBy("horaSalida", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(querySnapshot ->
                        Log.d(TAG, "Entregas del repartidor: " + querySnapshot.size()))
                .addOnFailureListener(e ->
                        Log.e(TAG, "Error al obtener entregas por repartidor", e));
    }

    // READ - Obtener entregas completadas por repartidor
    public Task<QuerySnapshot> obtenerEntregasCompletadasRepartidor(String repartidorID) {
        return db.collection(COLLECTION_NAME)
                .whereEqualTo("repartidorID", repartidorID)
                .whereEqualTo("codigoQRValidado", true)
                .orderBy("horaLlegada", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(querySnapshot ->
                        Log.d(TAG, "Entregas completadas: " + querySnapshot.size()))
                .addOnFailureListener(e ->
                        Log.e(TAG, "Error al obtener entregas completadas", e));
    }

    // READ - Obtener entregas en curso por repartidor
    public Task<QuerySnapshot> obtenerEntregasEnCurso(String repartidorID) {
        return db.collection(COLLECTION_NAME)
                .whereEqualTo("repartidorID", repartidorID)
                .whereEqualTo("codigoQRValidado", false)
                .get()
                .addOnSuccessListener(querySnapshot ->
                        Log.d(TAG, "Entregas en curso: " + querySnapshot.size()))
                .addOnFailureListener(e ->
                        Log.e(TAG, "Error al obtener entregas en curso", e));
    }

    // READ - Obtener entregas por rango de fechas
    public Task<QuerySnapshot> obtenerEntregasPorFecha(Date inicio, Date fin) {
        return db.collection(COLLECTION_NAME)
                .whereGreaterThanOrEqualTo("horaSalida", inicio)
                .whereLessThanOrEqualTo("horaSalida", fin)
                .orderBy("horaSalida", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(querySnapshot ->
                        Log.d(TAG, "Entregas en rango: " + querySnapshot.size()))
                .addOnFailureListener(e ->
                        Log.e(TAG, "Error al filtrar por fecha", e));
    }

    // UPDATE - Actualizar ubicación de salida
    public Task<Void> actualizarUbicacionSalida(String id, double lat, double lng) {
        return db.collection(COLLECTION_NAME)
                .document(id)
                .update("inicioLat", lat, "inicioLng", lng)
                .addOnSuccessListener(aVoid ->
                        Log.d(TAG, "Ubicación de salida actualizada"))
                .addOnFailureListener(e ->
                        Log.e(TAG, "Error al actualizar ubicación de salida", e));
    }

    // UPDATE - Actualizar ubicación de llegada
    public Task<Void> actualizarUbicacionLlegada(String id, double lat, double lng) {
        return db.collection(COLLECTION_NAME)
                .document(id)
                .update("llegadaLat", lat, "llegadaLng", lng)
                .addOnSuccessListener(aVoid ->
                        Log.d(TAG, "Ubicación de llegada actualizada"))
                .addOnFailureListener(e ->
                        Log.e(TAG, "Error al actualizar ubicación de llegada", e));
    }

    // UPDATE - Registrar hora de llegada y validar QR
    public Task<Void> completarEntrega(String id, Date horaLlegada, double lat, double lng) {
        return db.collection(COLLECTION_NAME)
                .document(id)
                .update(
                        "horaLlegada", horaLlegada,
                        "llegadaLat", lat,
                        "llegadaLng", lng,
                        "codigoQRValidado", true
                )
                .addOnSuccessListener(aVoid ->
                        Log.d(TAG, "Entrega completada: " + id))
                .addOnFailureListener(e ->
                        Log.e(TAG, "Error al completar entrega", e));
    }

    // UPDATE - Validar código QR
    public Task<Void> validarCodigoQR(String id) {
        return db.collection(COLLECTION_NAME)
                .document(id)
                .update("codigoQRValidado", true, "horaLlegada", new Date())
                .addOnSuccessListener(aVoid ->
                        Log.d(TAG, "Código QR validado para: " + id))
                .addOnFailureListener(e ->
                        Log.e(TAG, "Error al validar QR", e));
    }

    // DELETE - Eliminar entrega
    public Task<Void> eliminarEntrega(String id) {
        return db.collection(COLLECTION_NAME)
                .document(id)
                .delete()
                .addOnSuccessListener(aVoid ->
                        Log.d(TAG, "Entrega eliminada: " + id))
                .addOnFailureListener(e ->
                        Log.e(TAG, "Error al eliminar entrega", e));
    }

    // UTILITY - Calcular tiempo de entrega en minutos
    public static long calcularTiempoEntrega(Entrega entrega) {
        if (entrega.getHoraSalida() != null && entrega.getHoraLlegada() != null) {
            long diff = entrega.getHoraLlegada().getTime() - entrega.getHoraSalida().getTime();
            return diff / (60 * 1000); // Convertir a minutos
        }
        return 0;
    }

    // UTILITY - Calcular distancia aproximada (fórmula haversine simplificada)
    public static double calcularDistancia(Entrega entrega) {
        double lat1 = Math.toRadians(entrega.getInicioLat());
        double lat2 = Math.toRadians(entrega.getLlegadaLat());
        double lng1 = Math.toRadians(entrega.getInicioLng());
        double lng2 = Math.toRadians(entrega.getLlegadaLng());

        double dLat = lat2 - lat1;
        double dLng = lng2 - lng1;

        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.cos(lat1) * Math.cos(lat2) *
                        Math.sin(dLng/2) * Math.sin(dLng/2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        double radius = 6371; // Radio de la Tierra en km

        return radius * c;
    }

    // UTILITY - Convertir DocumentSnapshot a Entrega
    public static Entrega documentToEntrega(DocumentSnapshot doc) {
        if (doc.exists()) {
            return doc.toObject(Entrega.class);
        }
        return null;
    }

    // UTILITY - Convertir QuerySnapshot a lista de Entregas
    public static List<Entrega> queryToEntregaList(QuerySnapshot querySnapshot) {
        List<Entrega> entregas = new ArrayList<>();
        for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
            Entrega entrega = doc.toObject(Entrega.class);
            if (entrega != null) {
                entregas.add(entrega);
            }
        }
        return entregas;
    }
}