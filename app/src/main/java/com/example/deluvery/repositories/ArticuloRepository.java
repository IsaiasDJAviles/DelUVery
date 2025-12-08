package com.example.deluvery.repositories;

import android.util.Log;

import com.example.deluvery.models.Articulo;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class ArticuloRepository {

    private static final String TAG = "ArticuloRepository";
    private static final String COLLECTION_NAME = "articulos";

    private final FirebaseFirestore db;

    public ArticuloRepository() {
        this.db = FirebaseFirestore.getInstance();
    }

    // CREATE - Crear nuevo artículo
    public Task<Void> crearArticulo(Articulo articulo) {
        return db.collection(COLLECTION_NAME)
                .document(articulo.getId())
                .set(articulo)
                .addOnSuccessListener(aVoid ->
                        Log.d(TAG, "Artículo creado: " + articulo.getId()))
                .addOnFailureListener(e ->
                        Log.e(TAG, "Error al crear artículo", e));
    }

    // READ - Obtener artículo por ID
    public Task<DocumentSnapshot> obtenerArticuloPorId(String id) {
        return db.collection(COLLECTION_NAME)
                .document(id)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        Log.d(TAG, "Artículo encontrado: " + id);
                    }
                })
                .addOnFailureListener(e ->
                        Log.e(TAG, "Error al obtener artículo", e));
    }

    // READ - Obtener todos los artículos
    public Task<QuerySnapshot> obtenerTodosArticulos() {
        return db.collection(COLLECTION_NAME)
                .get()
                .addOnSuccessListener(querySnapshot ->
                        Log.d(TAG, "Total artículos: " + querySnapshot.size()))
                .addOnFailureListener(e ->
                        Log.e(TAG, "Error al obtener artículos", e));
    }

    // READ - Obtener artículos por local
    public Task<QuerySnapshot> obtenerArticulosPorLocal(String localID) {
        return db.collection(COLLECTION_NAME)
                .whereEqualTo("localID", localID)
                .get()
                .addOnSuccessListener(querySnapshot ->
                        Log.d(TAG, "Artículos del local " + localID + ": " + querySnapshot.size()))
                .addOnFailureListener(e ->
                        Log.e(TAG, "Error al obtener artículos por local", e));
    }

    // READ - Obtener artículos disponibles por local
    public Task<QuerySnapshot> obtenerArticulosDisponiblesPorLocal(String localID) {
        return db.collection(COLLECTION_NAME)
                .whereEqualTo("localID", localID)
                .whereEqualTo("disponible", true)
                .get()
                .addOnSuccessListener(querySnapshot ->
                        Log.d(TAG, "Artículos disponibles del local: " + querySnapshot.size()))
                .addOnFailureListener(e ->
                        Log.e(TAG, "Error al obtener artículos disponibles", e));
    }

    // READ - Buscar artículos por nombre
    public Task<QuerySnapshot> buscarArticulosPorNombre(String nombre) {
        return db.collection(COLLECTION_NAME)
                .whereGreaterThanOrEqualTo("nombre", nombre)
                .whereLessThanOrEqualTo("nombre", nombre + '\uf8ff')
                .get()
                .addOnSuccessListener(querySnapshot ->
                        Log.d(TAG, "Artículos encontrados: " + querySnapshot.size()))
                .addOnFailureListener(e ->
                        Log.e(TAG, "Error al buscar artículos", e));
    }

    // READ - Obtener artículos por rango de precio
    public Task<QuerySnapshot> obtenerArticulosPorRangoPrecio(double min, double max) {
        return db.collection(COLLECTION_NAME)
                .whereGreaterThanOrEqualTo("precio", min)
                .whereLessThanOrEqualTo("precio", max)
                .get()
                .addOnSuccessListener(querySnapshot ->
                        Log.d(TAG, "Artículos en rango de precio: " + querySnapshot.size()))
                .addOnFailureListener(e ->
                        Log.e(TAG, "Error al filtrar por precio", e));
    }

    // UPDATE - Actualizar artículo completo
    public Task<Void> actualizarArticulo(Articulo articulo) {
        return db.collection(COLLECTION_NAME)
                .document(articulo.getId())
                .set(articulo)
                .addOnSuccessListener(aVoid ->
                        Log.d(TAG, "Artículo actualizado: " + articulo.getId()))
                .addOnFailureListener(e ->
                        Log.e(TAG, "Error al actualizar artículo", e));
    }

    // UPDATE - Cambiar disponibilidad
    public Task<Void> cambiarDisponibilidad(String id, boolean disponible) {
        return db.collection(COLLECTION_NAME)
                .document(id)
                .update("disponible", disponible)
                .addOnSuccessListener(aVoid ->
                        Log.d(TAG, "Disponibilidad actualizada para: " + id))
                .addOnFailureListener(e ->
                        Log.e(TAG, "Error al actualizar disponibilidad", e));
    }

    // UPDATE - Actualizar precio
    public Task<Void> actualizarPrecio(String id, double precio) {
        return db.collection(COLLECTION_NAME)
                .document(id)
                .update("precio", precio)
                .addOnSuccessListener(aVoid ->
                        Log.d(TAG, "Precio actualizado para: " + id))
                .addOnFailureListener(e ->
                        Log.e(TAG, "Error al actualizar precio", e));
    }

    // UPDATE - Actualizar imagen
    public Task<Void> actualizarImagen(String id, String imagenURL) {
        return db.collection(COLLECTION_NAME)
                .document(id)
                .update("imagenURL", imagenURL)
                .addOnSuccessListener(aVoid ->
                        Log.d(TAG, "Imagen actualizada para: " + id))
                .addOnFailureListener(e ->
                        Log.e(TAG, "Error al actualizar imagen", e));
    }

    // DELETE - Eliminar artículo
    public Task<Void> eliminarArticulo(String id) {
        return db.collection(COLLECTION_NAME)
                .document(id)
                .delete()
                .addOnSuccessListener(aVoid ->
                        Log.d(TAG, "Artículo eliminado: " + id))
                .addOnFailureListener(e ->
                        Log.e(TAG, "Error al eliminar artículo", e));
    }

    // UTILITY - Convertir DocumentSnapshot a Articulo
    public static Articulo documentToArticulo(DocumentSnapshot doc) {
        if (doc.exists()) {
            return doc.toObject(Articulo.class);
        }
        return null;
    }

    // UTILITY - Convertir QuerySnapshot a lista de Articulos
    public static List<Articulo> queryToArticuloList(QuerySnapshot querySnapshot) {
        List<Articulo> articulos = new ArrayList<>();
        for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
            Articulo articulo = doc.toObject(Articulo.class);
            if (articulo != null) {
                articulos.add(articulo);
            }
        }
        return articulos;
    }
}