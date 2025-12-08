package com.example.deluvery.viewmodels;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.deluvery.models.Estudiante;
import com.example.deluvery.repositories.EstudianteRepository;

import java.util.List;

public class EstudianteViewModel extends ViewModel {

    private static final String TAG = "EstudianteViewModel";

    private final EstudianteRepository repository;

    private final MutableLiveData<List<Estudiante>> estudiantesLiveData;
    private final MutableLiveData<List<Estudiante>> repartidoresActivosLiveData;
    private final MutableLiveData<Estudiante> estudianteSeleccionadoLiveData;
    private final MutableLiveData<Boolean> cargandoLiveData;
    private final MutableLiveData<String> errorLiveData;

    public EstudianteViewModel() {
        repository = new EstudianteRepository();
        estudiantesLiveData = new MutableLiveData<>();
        repartidoresActivosLiveData = new MutableLiveData<>();
        estudianteSeleccionadoLiveData = new MutableLiveData<>();
        cargandoLiveData = new MutableLiveData<>(false);
        errorLiveData = new MutableLiveData<>();
    }

    // Getters para LiveData
    public LiveData<List<Estudiante>> getEstudiantes() {
        return estudiantesLiveData;
    }

    public LiveData<List<Estudiante>> getRepartidoresActivos() {
        return repartidoresActivosLiveData;
    }

    public LiveData<Estudiante> getEstudianteSeleccionado() {
        return estudianteSeleccionadoLiveData;
    }

    public LiveData<Boolean> getCargando() {
        return cargandoLiveData;
    }

    public LiveData<String> getError() {
        return errorLiveData;
    }

    // Cargar todos los estudiantes
    public void cargarEstudiantes() {
        cargandoLiveData.setValue(true);

        repository.obtenerTodosEstudiantes()
                .addOnSuccessListener(querySnapshot -> {
                    List<Estudiante> estudiantes =
                            EstudianteRepository.queryToEstudianteList(querySnapshot);
                    estudiantesLiveData.setValue(estudiantes);
                    cargandoLiveData.setValue(false);
                    Log.d(TAG, "Estudiantes cargados: " + estudiantes.size());
                })
                .addOnFailureListener(e -> {
                    errorLiveData.setValue("Error al cargar estudiantes: " + e.getMessage());
                    cargandoLiveData.setValue(false);
                    Log.e(TAG, "Error al cargar estudiantes", e);
                });
    }

    // Cargar estudiantes por rol
    public void cargarEstudiantesPorRol(String rol) {
        cargandoLiveData.setValue(true);

        repository.obtenerEstudiantesPorRol(rol)
                .addOnSuccessListener(querySnapshot -> {
                    List<Estudiante> estudiantes =
                            EstudianteRepository.queryToEstudianteList(querySnapshot);
                    estudiantesLiveData.setValue(estudiantes);
                    cargandoLiveData.setValue(false);
                })
                .addOnFailureListener(e -> {
                    errorLiveData.setValue("Error al filtrar por rol: " + e.getMessage());
                    cargandoLiveData.setValue(false);
                    Log.e(TAG, "Error al filtrar por rol", e);
                });
    }

    // Cargar repartidores activos
    public void cargarRepartidoresActivos() {
        cargandoLiveData.setValue(true);

        repository.obtenerRepartidoresActivos()
                .addOnSuccessListener(querySnapshot -> {
                    List<Estudiante> repartidores =
                            EstudianteRepository.queryToEstudianteList(querySnapshot);
                    repartidoresActivosLiveData.setValue(repartidores);
                    cargandoLiveData.setValue(false);
                    Log.d(TAG, "Repartidores activos: " + repartidores.size());
                })
                .addOnFailureListener(e -> {
                    errorLiveData.setValue("Error al cargar repartidores: " + e.getMessage());
                    cargandoLiveData.setValue(false);
                    Log.e(TAG, "Error al cargar repartidores", e);
                });
    }

    // Cargar estudiante por ID
    public void cargarEstudiantePorId(String id) {
        cargandoLiveData.setValue(true);

        repository.obtenerEstudiantePorId(id)
                .addOnSuccessListener(documentSnapshot -> {
                    Estudiante estudiante =
                            EstudianteRepository.documentToEstudiante(documentSnapshot);
                    estudianteSeleccionadoLiveData.setValue(estudiante);
                    cargandoLiveData.setValue(false);
                })
                .addOnFailureListener(e -> {
                    errorLiveData.setValue("Error al cargar estudiante: " + e.getMessage());
                    cargandoLiveData.setValue(false);
                    Log.e(TAG, "Error al cargar estudiante", e);
                });
    }

    // Buscar por correo
    public void buscarPorCorreo(String correo) {
        cargandoLiveData.setValue(true);

        repository.obtenerEstudiantePorCorreo(correo)
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        Estudiante estudiante =
                                EstudianteRepository.documentToEstudiante(
                                        querySnapshot.getDocuments().get(0));
                        estudianteSeleccionadoLiveData.setValue(estudiante);
                    } else {
                        errorLiveData.setValue("No se encontró estudiante con ese correo");
                    }
                    cargandoLiveData.setValue(false);
                })
                .addOnFailureListener(e -> {
                    errorLiveData.setValue("Error en búsqueda: " + e.getMessage());
                    cargandoLiveData.setValue(false);
                    Log.e(TAG, "Error al buscar por correo", e);
                });
    }

    // Crear estudiante
    public void crearEstudiante(Estudiante estudiante) {
        cargandoLiveData.setValue(true);

        repository.crearEstudiante(estudiante)
                .addOnSuccessListener(aVoid -> {
                    cargandoLiveData.setValue(false);
                    cargarEstudiantes(); // Recargar lista
                    Log.d(TAG, "Estudiante creado exitosamente");
                })
                .addOnFailureListener(e -> {
                    errorLiveData.setValue("Error al crear estudiante: " + e.getMessage());
                    cargandoLiveData.setValue(false);
                    Log.e(TAG, "Error al crear estudiante", e);
                });
    }

    // Actualizar estudiante
    public void actualizarEstudiante(Estudiante estudiante) {
        cargandoLiveData.setValue(true);

        repository.actualizarEstudiante(estudiante)
                .addOnSuccessListener(aVoid -> {
                    cargandoLiveData.setValue(false);
                    cargarEstudiantes(); // Recargar lista
                    Log.d(TAG, "Estudiante actualizado");
                })
                .addOnFailureListener(e -> {
                    errorLiveData.setValue("Error al actualizar: " + e.getMessage());
                    cargandoLiveData.setValue(false);
                    Log.e(TAG, "Error al actualizar estudiante", e);
                });
    }

    // Cambiar estado activo
    public void cambiarEstadoActivo(String id, boolean activo) {
        repository.actualizarEstadoActivo(id, activo)
                .addOnSuccessListener(aVoid -> {
                    cargarEstudiantes(); // Recargar lista
                    Log.d(TAG, "Estado actualizado");
                })
                .addOnFailureListener(e -> {
                    errorLiveData.setValue("Error al cambiar estado: " + e.getMessage());
                    Log.e(TAG, "Error al cambiar estado", e);
                });
    }

    // Eliminar estudiante
    public void eliminarEstudiante(String id) {
        cargandoLiveData.setValue(true);

        repository.eliminarEstudiante(id)
                .addOnSuccessListener(aVoid -> {
                    cargandoLiveData.setValue(false);
                    cargarEstudiantes(); // Recargar lista
                    Log.d(TAG, "Estudiante eliminado");
                })
                .addOnFailureListener(e -> {
                    errorLiveData.setValue("Error al eliminar: " + e.getMessage());
                    cargandoLiveData.setValue(false);
                    Log.e(TAG, "Error al eliminar estudiante", e);
                });
    }

    // Limpiar error
    public void limpiarError() {
        errorLiveData.setValue(null);
    }
}