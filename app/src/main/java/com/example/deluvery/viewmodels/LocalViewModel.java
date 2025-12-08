package com.example.deluvery.viewmodels;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.deluvery.models.Local;
import com.example.deluvery.repositories.LocalRepository;

import java.util.List;

public class LocalViewModel extends ViewModel {

    private static final String TAG = "LocalViewModel";

    private final LocalRepository repository;

    private final MutableLiveData<List<Local>> localesLiveData;
    private final MutableLiveData<List<Local>> localesDisponiblesLiveData;
    private final MutableLiveData<Local> localSeleccionadoLiveData;
    private final MutableLiveData<Boolean> cargandoLiveData;
    private final MutableLiveData<String> errorLiveData;

    public LocalViewModel() {
        repository = new LocalRepository();
        localesLiveData = new MutableLiveData<>();
        localesDisponiblesLiveData = new MutableLiveData<>();
        localSeleccionadoLiveData = new MutableLiveData<>();
        cargandoLiveData = new MutableLiveData<>(false);
        errorLiveData = new MutableLiveData<>();
    }

    public LiveData<List<Local>> getLocales() {
        return localesLiveData;
    }

    public LiveData<List<Local>> getLocalesDisponibles() {
        return localesDisponiblesLiveData;
    }

    public LiveData<Local> getLocalSeleccionado() {
        return localSeleccionadoLiveData;
    }

    public LiveData<Boolean> getCargando() {
        return cargandoLiveData;
    }

    public LiveData<String> getError() {
        return errorLiveData;
    }

    public void cargarTodosLocales() {
        cargandoLiveData.setValue(true);

        repository.obtenerTodosLocales()
                .addOnSuccessListener(querySnapshot -> {
                    List<Local> locales =
                            LocalRepository.queryToLocalList(querySnapshot);
                    localesLiveData.setValue(locales);
                    cargandoLiveData.setValue(false);
                    Log.d(TAG, "Locales cargados: " + locales.size());
                })
                .addOnFailureListener(e -> {
                    errorLiveData.setValue("Error al cargar locales: " + e.getMessage());
                    cargandoLiveData.setValue(false);
                    Log.e(TAG, "Error al cargar locales", e);
                });
    }

    public void cargarLocalesDisponibles() {
        cargandoLiveData.setValue(true);

        repository.obtenerLocalesDisponibles()
                .addOnSuccessListener(querySnapshot -> {
                    List<Local> locales =
                            LocalRepository.queryToLocalList(querySnapshot);
                    localesDisponiblesLiveData.setValue(locales);
                    cargandoLiveData.setValue(false);
                    Log.d(TAG, "Locales disponibles: " + locales.size());
                })
                .addOnFailureListener(e -> {
                    errorLiveData.setValue("Error al cargar disponibles: " + e.getMessage());
                    cargandoLiveData.setValue(false);
                    Log.e(TAG, "Error al cargar disponibles", e);
                });
    }

    public void cargarLocalPorId(String id) {
        cargandoLiveData.setValue(true);

        repository.obtenerLocalPorId(id)
                .addOnSuccessListener(documentSnapshot -> {
                    Local local = LocalRepository.documentToLocal(documentSnapshot);
                    localSeleccionadoLiveData.setValue(local);
                    cargandoLiveData.setValue(false);
                })
                .addOnFailureListener(e -> {
                    errorLiveData.setValue("Error al cargar local: " + e.getMessage());
                    cargandoLiveData.setValue(false);
                    Log.e(TAG, "Error al cargar local", e);
                });
    }

    public void buscarPorNombre(String nombre) {
        cargandoLiveData.setValue(true);

        repository.buscarLocalesPorNombre(nombre)
                .addOnSuccessListener(querySnapshot -> {
                    List<Local> locales =
                            LocalRepository.queryToLocalList(querySnapshot);
                    localesLiveData.setValue(locales);
                    cargandoLiveData.setValue(false);
                })
                .addOnFailureListener(e -> {
                    errorLiveData.setValue("Error en búsqueda: " + e.getMessage());
                    cargandoLiveData.setValue(false);
                    Log.e(TAG, "Error en búsqueda", e);
                });
    }

    public void crearLocal(Local local) {
        cargandoLiveData.setValue(true);

        repository.crearLocal(local)
                .addOnSuccessListener(aVoid -> {
                    cargandoLiveData.setValue(false);
                    cargarTodosLocales();
                    Log.d(TAG, "Local creado");
                })
                .addOnFailureListener(e -> {
                    errorLiveData.setValue("Error al crear local: " + e.getMessage());
                    cargandoLiveData.setValue(false);
                    Log.e(TAG, "Error al crear local", e);
                });
    }

    public void actualizarLocal(Local local) {
        cargandoLiveData.setValue(true);

        repository.actualizarLocal(local)
                .addOnSuccessListener(aVoid -> {
                    cargandoLiveData.setValue(false);
                    cargarTodosLocales();
                    Log.d(TAG, "Local actualizado");
                })
                .addOnFailureListener(e -> {
                    errorLiveData.setValue("Error al actualizar: " + e.getMessage());
                    cargandoLiveData.setValue(false);
                    Log.e(TAG, "Error al actualizar", e);
                });
    }

    public void cambiarDisponibilidad(String id, boolean disponible) {
        repository.cambiarDisponibilidad(id, disponible)
                .addOnSuccessListener(aVoid -> {
                    cargarTodosLocales();
                    Log.d(TAG, "Disponibilidad actualizada");
                })
                .addOnFailureListener(e -> {
                    errorLiveData.setValue("Error al cambiar disponibilidad: " + e.getMessage());
                    Log.e(TAG, "Error al cambiar disponibilidad", e);
                });
    }

    public void limpiarError() {
        errorLiveData.setValue(null);
    }
}