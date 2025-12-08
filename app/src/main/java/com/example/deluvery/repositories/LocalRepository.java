package com.example.deluvery.repositories;

import android.util.Log;

import com.example.deluvery.models.Local;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class LocalRepository {

    private static final String TAG = "LocalRepository";
    private static final String COLLECTION_NAME = "locales";

    private final FirebaseFirestore db;

    public LocalRepository() {
        this.db = FirebaseFirestore.getInstance();
    }

    // CREATE - Crear nuevo local
    public Task<Void> crearLocal(Local local) {
        return db.collection(COLLECTION_NAME)
                .document(local.getId())
                .set(local)
                .addOnSuccessListener(aVoid ->
                        Log.d(TAG, "Local creado: " + local.getId()))
                .addOnFailureListener(e ->
                        Log.e(TAG, "Error al crear local", e));
    }

    // READ - Obtener local por ID
    public Task<DocumentSnapshot> obtenerLocalPorId(String id) {
        return db.collection(COLLECTION_NAME)
                .document(id)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        Log.d(TAG, "Local encontrado: " + id);
                    }
                })
                .addOnFailureListener(e ->
                        Log.e(TAG, "Error al obtener local", e));
    }

    // READ - Obtener todos los locales
    public Task<QuerySnapshot> obtenerTodosLocales() {
        return db.collection(COLLECTION_NAME)
                .get()
                .addOnSuccessListener(querySnapshot ->
                        Log.d(TAG, "Total locales: " + querySnapshot.size()))
                .addOnFailureListener(e ->
                        Log.e(TAG, "Error al obtener locales", e));
    }

    // READ - Obtener locales disponibles
    public Task<QuerySnapshot> obtenerLocalesDisponibles() {
        return db.collection(COLLECTION_NAME)
                .whereEqualTo("disponible", true)
                .get()
                .addOnSuccessListener(querySnapshot ->
                        Log.d(TAG, "Locales disponibles: " + querySnapshot.size()))
                .addOnFailureListener(e ->
                        Log.e(TAG, "Error al obtener locales disponibles", e));
    }

    // READ - Buscar locales por nombre
    public Task<QuerySnapshot> buscarLocalesPorNombre(String nombre) {
        return db.collection(COLLECTION_NAME)
                .whereGreaterThanOrEqualTo("nombre", nombre)
                .whereLessThanOrEqualTo("nombre", nombre + '\uf8ff')
                .get()
                .addOnSuccessListener(querySnapshot ->
                        Log.d(TAG, "Locales encontrados: " + querySnapshot.size()))
                .addOnFailureListener(e ->
                        Log.e(TAG, "Error al buscar locales", e));
    }

    // UPDATE - Actualizar local completo
    public Task<Void> actualizarLocal(Local local) {
        return db.collection(COLLECTION_NAME)
                .document(local.getId())
                .set(local)
                .addOnSuccessListener(aVoid ->
                        Log.d(TAG, "Local actualizado: " + local.getId()))
                .addOnFailureListener(e ->
                        Log.e(TAG, "Error al actualizar local", e));
    }

    // UPDATE - Cambiar disponibilidad del local
    public Task<Void> cambiarDisponibilidad(String id, boolean disponible) {
        return db.collection(COLLECTION_NAME)
                .document(id)
                .update("disponible", disponible)
                .addOnSuccessListener(aVoid ->
                        Log.d(TAG, "Disponibilidad actualizada para: " + id))
                .addOnFailureListener(e ->
                        Log.e(TAG, "Error al actualizar disponibilidad", e));
    }

    // UPDATE - Actualizar horarios
    public Task<Void> actualizarHorarios(String id, String apertura, String cierre) {
        return db.collection(COLLECTION_NAME)
                .document(id)
                .update("horarioApertura", apertura, "horarioCierre", cierre)
                .addOnSuccessListener(aVoid ->
                        Log.d(TAG, "Horarios actualizados para: " + id))
                .addOnFailureListener(e ->
                        Log.e(TAG, "Error al actualizar horarios", e));
    }

    // DELETE - Eliminar local
    public Task<Void> eliminarLocal(String id) {
        return db.collection(COLLECTION_NAME)
                .document(id)
                .delete()
                .addOnSuccessListener(aVoid ->
                        Log.d(TAG, "Local eliminado: " + id))
                .addOnFailureListener(e ->
                        Log.e(TAG, "Error al eliminar local", e));
    }

    // UTILITY - Convertir DocumentSnapshot a Local
    public static Local documentToLocal(DocumentSnapshot doc) {
        if (doc.exists()) {
            return doc.toObject(Local.class);
        }
        return null;
    }

    // UTILITY - Convertir QuerySnapshot a lista de Locales
    public static List<Local> queryToLocalList(QuerySnapshot querySnapshot) {
        List<Local> locales = new ArrayList<>();
        for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
            Local local = doc.toObject(Local.class);
            if (local != null) {
                locales.add(local);
            }
        }
        return locales;
    }
}