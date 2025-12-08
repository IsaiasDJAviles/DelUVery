package com.example.deluvery.repositories;

import android.util.Log;

import com.example.deluvery.models.Estudiante;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class EstudianteRepository {

    private static final String TAG = "EstudianteRepository";
    private static final String COLLECTION_NAME = "estudiantes";

    private final FirebaseFirestore db;

    public EstudianteRepository() {
        this.db = FirebaseFirestore.getInstance();
    }

    // CREATE - Crear nuevo estudiante
    public Task<Void> crearEstudiante(Estudiante estudiante) {
        return db.collection(COLLECTION_NAME)
                .document(estudiante.getId())
                .set(estudiante)
                .addOnSuccessListener(aVoid ->
                        Log.d(TAG, "Estudiante creado: " + estudiante.getId()))
                .addOnFailureListener(e ->
                        Log.e(TAG, "Error al crear estudiante", e));
    }

    // READ - Obtener estudiante por ID
    public Task<DocumentSnapshot> obtenerEstudiantePorId(String id) {
        return db.collection(COLLECTION_NAME)
                .document(id)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        Log.d(TAG, "Estudiante encontrado: " + id);
                    } else {
                        Log.d(TAG, "Estudiante no existe: " + id);
                    }
                })
                .addOnFailureListener(e ->
                        Log.e(TAG, "Error al obtener estudiante", e));
    }

    // READ - Obtener todos los estudiantes
    public Task<QuerySnapshot> obtenerTodosEstudiantes() {
        return db.collection(COLLECTION_NAME)
                .get()
                .addOnSuccessListener(querySnapshot ->
                        Log.d(TAG, "Total estudiantes: " + querySnapshot.size()))
                .addOnFailureListener(e ->
                        Log.e(TAG, "Error al obtener estudiantes", e));
    }

    // READ - Obtener estudiantes por rol (cliente/repartidor)
    public Task<QuerySnapshot> obtenerEstudiantesPorRol(String rol) {
        return db.collection(COLLECTION_NAME)
                .whereEqualTo("rol", rol)
                .get()
                .addOnSuccessListener(querySnapshot ->
                        Log.d(TAG, "Estudiantes con rol " + rol + ": " + querySnapshot.size()))
                .addOnFailureListener(e ->
                        Log.e(TAG, "Error al filtrar por rol", e));
    }

    // READ - Obtener estudiante por correo
    public Task<QuerySnapshot> obtenerEstudiantePorCorreo(String correo) {
        return db.collection(COLLECTION_NAME)
                .whereEqualTo("correo", correo)
                .limit(1)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        Log.d(TAG, "Estudiante encontrado con correo: " + correo);
                    }
                })
                .addOnFailureListener(e ->
                        Log.e(TAG, "Error al buscar por correo", e));
    }

    // READ - Obtener repartidores activos
    public Task<QuerySnapshot> obtenerRepartidoresActivos() {
        return db.collection(COLLECTION_NAME)
                .whereEqualTo("rol", "repartidor")
                .whereEqualTo("activo", true)
                .get()
                .addOnSuccessListener(querySnapshot ->
                        Log.d(TAG, "Repartidores activos: " + querySnapshot.size()))
                .addOnFailureListener(e ->
                        Log.e(TAG, "Error al obtener repartidores activos", e));
    }

    // UPDATE - Actualizar estudiante completo
    public Task<Void> actualizarEstudiante(Estudiante estudiante) {
        return db.collection(COLLECTION_NAME)
                .document(estudiante.getId())
                .set(estudiante)
                .addOnSuccessListener(aVoid ->
                        Log.d(TAG, "Estudiante actualizado: " + estudiante.getId()))
                .addOnFailureListener(e ->
                        Log.e(TAG, "Error al actualizar estudiante", e));
    }

    // UPDATE - Actualizar solo el estado activo
    public Task<Void> actualizarEstadoActivo(String id, boolean activo) {
        return db.collection(COLLECTION_NAME)
                .document(id)
                .update("activo", activo)
                .addOnSuccessListener(aVoid ->
                        Log.d(TAG, "Estado activo actualizado para: " + id))
                .addOnFailureListener(e ->
                        Log.e(TAG, "Error al actualizar estado", e));
    }

    // UPDATE - Actualizar teléfono
    public Task<Void> actualizarTelefono(String id, String telefono) {
        return db.collection(COLLECTION_NAME)
                .document(id)
                .update("telefono", telefono)
                .addOnSuccessListener(aVoid ->
                        Log.d(TAG, "Teléfono actualizado para: " + id))
                .addOnFailureListener(e ->
                        Log.e(TAG, "Error al actualizar teléfono", e));
    }

    // UPDATE - Actualizar foto de perfil
    public Task<Void> actualizarFotoURL(String id, String fotoURL) {
        return db.collection(COLLECTION_NAME)
                .document(id)
                .update("fotoURL", fotoURL)
                .addOnSuccessListener(aVoid ->
                        Log.d(TAG, "Foto actualizada para: " + id))
                .addOnFailureListener(e ->
                        Log.e(TAG, "Error al actualizar foto", e));
    }

    // DELETE - Eliminar estudiante
    public Task<Void> eliminarEstudiante(String id) {
        return db.collection(COLLECTION_NAME)
                .document(id)
                .delete()
                .addOnSuccessListener(aVoid ->
                        Log.d(TAG, "Estudiante eliminado: " + id))
                .addOnFailureListener(e ->
                        Log.e(TAG, "Error al eliminar estudiante", e));
    }

    // UTILITY - Verificar si existe estudiante
    public void existeEstudiante(String id, OnExisteCallback callback) {
        db.collection(COLLECTION_NAME)
                .document(id)
                .get()
                .addOnSuccessListener(doc -> callback.onResult(doc.exists()))
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al verificar existencia", e);
                    callback.onResult(false);
                });
    }

    // Callback interface para verificación de existencia
    public interface OnExisteCallback {
        void onResult(boolean existe);
    }

    // UTILITY - Convertir DocumentSnapshot a Estudiante
    public static Estudiante documentToEstudiante(DocumentSnapshot doc) {
        if (doc.exists()) {
            return doc.toObject(Estudiante.class);
        }
        return null;
    }

    // UTILITY - Convertir QuerySnapshot a lista de Estudiantes
    public static List<Estudiante> queryToEstudianteList(QuerySnapshot querySnapshot) {
        List<Estudiante> estudiantes = new ArrayList<>();
        for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
            Estudiante estudiante = doc.toObject(Estudiante.class);
            if (estudiante != null) {
                estudiantes.add(estudiante);
            }
        }
        return estudiantes;
    }
}