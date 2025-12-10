package com.example.deluvery.repositories;

import android.util.Log;

import com.example.deluvery.models.ArticuloPedido;
import com.example.deluvery.models.Pedido;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PedidoRepository {

    private static final String TAG = "PedidoRepository";
    private static final String COLLECTION_NAME = "pedidos";
    private static final String SUBCOLLECTION_ARTICULOS = "articulos";

    private final FirebaseFirestore db;

    public PedidoRepository() {
        this.db = FirebaseFirestore.getInstance();
    }

    // CREATE - Crear nuevo pedido con artículos
    public Task<Void> crearPedidoConArticulos(Pedido pedido, List<ArticuloPedido> articulos) {
        // Primero crear el pedido
        return db.collection(COLLECTION_NAME)
                .document(pedido.getId())
                .set(pedido)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Pedido creado: " + pedido.getId());
                    // Luego agregar los artículos
                    agregarArticulosPedido(pedido.getId(), articulos);
                })
                .addOnFailureListener(e ->
                        Log.e(TAG, "Error al crear pedido", e));
    }

    // CREATE - Agregar artículos a un pedido
    public void agregarArticulosPedido(String pedidoID, List<ArticuloPedido> articulos) {
        for (ArticuloPedido art : articulos) {
            db.collection(COLLECTION_NAME)
                    .document(pedidoID)
                    .collection(SUBCOLLECTION_ARTICULOS)
                    .add(art)
                    .addOnSuccessListener(docRef ->
                            Log.d(TAG, "Artículo agregado al pedido: " + docRef.getId()))
                    .addOnFailureListener(e ->
                            Log.e(TAG, "Error al agregar artículo", e));
        }
    }

    // READ - Obtener pedido por ID
    public Task<DocumentSnapshot> obtenerPedidoPorId(String id) {
        return db.collection(COLLECTION_NAME)
                .document(id)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        Log.d(TAG, "Pedido encontrado: " + id);
                    }
                })
                .addOnFailureListener(e ->
                        Log.e(TAG, "Error al obtener pedido", e));
    }

    // READ - Obtener artículos de un pedido
    public Task<QuerySnapshot> obtenerArticulosDePedido(String pedidoID) {
        return db.collection(COLLECTION_NAME)
                .document(pedidoID)
                .collection(SUBCOLLECTION_ARTICULOS)
                .get()
                .addOnSuccessListener(querySnapshot ->
                        Log.d(TAG, "Artículos del pedido: " + querySnapshot.size()))
                .addOnFailureListener(e ->
                        Log.e(TAG, "Error al obtener artículos del pedido", e));
    }

    // READ - Obtener pedidos por cliente
    // READ - Obtener pedidos por cliente (versión sin índice compuesto)
    public Task<QuerySnapshot> obtenerPedidosPorCliente(String clienteID) {
        return db.collection(COLLECTION_NAME)
                .whereEqualTo("clienteID", clienteID)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    // Ordenar manualmente en el cliente si es necesario
                    Log.d(TAG, "Pedidos del cliente: " + querySnapshot.size());
                })
                .addOnFailureListener(e ->
                        Log.e(TAG, "Error al obtener pedidos por cliente", e));
    }

    // READ - Obtener pedidos por repartidor
    public Task<QuerySnapshot> obtenerPedidosPorRepartidor(String repartidorID) {
        return db.collection(COLLECTION_NAME)
                .whereEqualTo("repartidorID", repartidorID)
                .orderBy("fecha", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(querySnapshot ->
                        Log.d(TAG, "Pedidos del repartidor: " + querySnapshot.size()))
                .addOnFailureListener(e ->
                        Log.e(TAG, "Error al obtener pedidos por repartidor", e));
    }

    // READ - Obtener pedidos por estado
    public Task<QuerySnapshot> obtenerPedidosPorEstado(String estado) {
        return db.collection(COLLECTION_NAME)
                .whereEqualTo("estado", estado)
                .orderBy("fecha", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(querySnapshot ->
                        Log.d(TAG, "Pedidos con estado " + estado + ": " + querySnapshot.size()))
                .addOnFailureListener(e ->
                        Log.e(TAG, "Error al filtrar por estado", e));
    }

    // READ - Obtener pedidos pendientes de asignar
    public Task<QuerySnapshot> obtenerPedidosPendientes() {
        return db.collection(COLLECTION_NAME)
                .whereEqualTo("estado", "pendiente")
                .orderBy("fecha", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(querySnapshot ->
                        Log.d(TAG, "Pedidos pendientes: " + querySnapshot.size()))
                .addOnFailureListener(e ->
                        Log.e(TAG, "Error al obtener pedidos pendientes", e));
    }

    // READ - Obtener pedidos activos de un repartidor (en camino)
    public Task<QuerySnapshot> obtenerPedidosActivosRepartidor(String repartidorID) {
        return db.collection(COLLECTION_NAME)
                .whereEqualTo("repartidorID", repartidorID)
                .whereEqualTo("estado", "en_camino")
                .get()
                .addOnSuccessListener(querySnapshot ->
                        Log.d(TAG, "Pedidos activos del repartidor: " + querySnapshot.size()))
                .addOnFailureListener(e ->
                        Log.e(TAG, "Error al obtener pedidos activos", e));
    }

    // READ - Obtener pedidos por local
    public Task<QuerySnapshot> obtenerPedidosPorLocal(String localID) {
        return db.collection(COLLECTION_NAME)
                .whereEqualTo("localID", localID)
                .orderBy("fecha", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(querySnapshot ->
                        Log.d(TAG, "Pedidos del local: " + querySnapshot.size()))
                .addOnFailureListener(e ->
                        Log.e(TAG, "Error al obtener pedidos por local", e));
    }

    // READ - Obtener pedidos por rango de fechas
    public Task<QuerySnapshot> obtenerPedidosPorRangoFechas(Date inicio, Date fin) {
        return db.collection(COLLECTION_NAME)
                .whereGreaterThanOrEqualTo("fecha", inicio)
                .whereLessThanOrEqualTo("fecha", fin)
                .orderBy("fecha", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(querySnapshot ->
                        Log.d(TAG, "Pedidos en rango: " + querySnapshot.size()))
                .addOnFailureListener(e ->
                        Log.e(TAG, "Error al filtrar por rango de fechas", e));
    }

    // UPDATE - Actualizar estado del pedido
    public Task<Void> actualizarEstado(String id, String estado) {
        return db.collection(COLLECTION_NAME)
                .document(id)
                .update("estado", estado)
                .addOnSuccessListener(aVoid ->
                        Log.d(TAG, "Estado actualizado a: " + estado + " para pedido: " + id))
                .addOnFailureListener(e ->
                        Log.e(TAG, "Error al actualizar estado", e));
    }

    // UPDATE - Asignar repartidor
    public Task<Void> asignarRepartidor(String pedidoID, String repartidorID) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("repartidorID", repartidorID);
        updates.put("estado", "asignado");

        return db.collection(COLLECTION_NAME)
                .document(pedidoID)
                .update(updates)
                .addOnSuccessListener(aVoid ->
                        Log.d(TAG, "Repartidor asignado al pedido: " + pedidoID))
                .addOnFailureListener(e ->
                        Log.e(TAG, "Error al asignar repartidor", e));
    }

    // UPDATE - Actualizar ubicación
    public Task<Void> actualizarUbicacion(String id, double lat, double lng) {
        return db.collection(COLLECTION_NAME)
                .document(id)
                .update("lat", lat, "lng", lng)
                .addOnSuccessListener(aVoid ->
                        Log.d(TAG, "Ubicación actualizada para: " + id))
                .addOnFailureListener(e ->
                        Log.e(TAG, "Error al actualizar ubicación", e));
    }

    // UPDATE - Validar código QR
    public Task<Void> validarCodigoQR(String pedidoID, boolean validado) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("estado", "entregado");

        return db.collection(COLLECTION_NAME)
                .document(pedidoID)
                .update(updates)
                .addOnSuccessListener(aVoid ->
                        Log.d(TAG, "Pedido marcado como entregado: " + pedidoID))
                .addOnFailureListener(e ->
                        Log.e(TAG, "Error al validar entrega", e));
    }

    // DELETE - Cancelar pedido (soft delete - cambia estado)
    public Task<Void> cancelarPedido(String id) {
        return actualizarEstado(id, "cancelado");
    }

    // DELETE - Eliminar pedido (hard delete)
    public Task<Void> eliminarPedido(String id) {
        return db.collection(COLLECTION_NAME)
                .document(id)
                .delete()
                .addOnSuccessListener(aVoid ->
                        Log.d(TAG, "Pedido eliminado: " + id))
                .addOnFailureListener(e ->
                        Log.e(TAG, "Error al eliminar pedido", e));
    }

    // UTILITY - Convertir DocumentSnapshot a Pedido
    public static Pedido documentToPedido(DocumentSnapshot doc) {
        if (doc.exists()) {
            return doc.toObject(Pedido.class);
        }
        return null;
    }

    // UTILITY - Convertir QuerySnapshot a lista de Pedidos
    public static List<Pedido> queryToPedidoList(QuerySnapshot querySnapshot) {
        List<Pedido> pedidos = new ArrayList<>();
        for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
            Pedido pedido = doc.toObject(Pedido.class);
            if (pedido != null) {
                pedidos.add(pedido);
            }
        }
        return pedidos;
    }

    // UTILITY - Convertir QuerySnapshot a lista de ArticuloPedido
    public static List<ArticuloPedido> queryToArticuloPedidoList(QuerySnapshot querySnapshot) {
        List<ArticuloPedido> articulos = new ArrayList<>();
        for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
            ArticuloPedido articulo = doc.toObject(ArticuloPedido.class);
            if (articulo != null) {
                articulos.add(articulo);
            }
        }
        return articulos;
    }
}