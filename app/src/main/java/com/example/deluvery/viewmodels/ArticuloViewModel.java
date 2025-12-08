package com.example.deluvery.viewmodels;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.deluvery.models.Articulo;
import com.example.deluvery.repositories.ArticuloRepository;

import java.util.List;

public class ArticuloViewModel extends ViewModel {

    private static final String TAG = "ArticuloViewModel";

    private final ArticuloRepository repository;

    private final MutableLiveData<List<Articulo>> articulosLiveData;
    private final MutableLiveData<Articulo> articuloSeleccionadoLiveData;
    private final MutableLiveData<Boolean> cargandoLiveData;
    private final MutableLiveData<String> errorLiveData;

    public ArticuloViewModel() {
        repository = new ArticuloRepository();
        articulosLiveData = new MutableLiveData<>();
        articuloSeleccionadoLiveData = new MutableLiveData<>();
        cargandoLiveData = new MutableLiveData<>(false);
        errorLiveData = new MutableLiveData<>();
    }

    public LiveData<List<Articulo>> getArticulos() {
        return articulosLiveData;
    }

    public LiveData<Articulo> getArticuloSeleccionado() {
        return articuloSeleccionadoLiveData;
    }

    public LiveData<Boolean> getCargando() {
        return cargandoLiveData;
    }

    public LiveData<String> getError() {
        return errorLiveData;
    }

    public void cargarTodosArticulos() {
        cargandoLiveData.setValue(true);

        repository.obtenerTodosArticulos()
                .addOnSuccessListener(querySnapshot -> {
                    List<Articulo> articulos =
                            ArticuloRepository.queryToArticuloList(querySnapshot);
                    articulosLiveData.setValue(articulos);
                    cargandoLiveData.setValue(false);
                    Log.d(TAG, "Artículos cargados: " + articulos.size());
                })
                .addOnFailureListener(e -> {
                    errorLiveData.setValue("Error al cargar artículos: " + e.getMessage());
                    cargandoLiveData.setValue(false);
                    Log.e(TAG, "Error al cargar artículos", e);
                });
    }

    public void cargarArticulosPorLocal(String localID) {
        cargandoLiveData.setValue(true);

        repository.obtenerArticulosPorLocal(localID)
                .addOnSuccessListener(querySnapshot -> {
                    List<Articulo> articulos =
                            ArticuloRepository.queryToArticuloList(querySnapshot);
                    articulosLiveData.setValue(articulos);
                    cargandoLiveData.setValue(false);
                    Log.d(TAG, "Artículos del local: " + articulos.size());
                })
                .addOnFailureListener(e -> {
                    errorLiveData.setValue("Error al cargar menú: " + e.getMessage());
                    cargandoLiveData.setValue(false);
                    Log.e(TAG, "Error al cargar menú", e);
                });
    }

    public void cargarArticulosDisponibles(String localID) {
        cargandoLiveData.setValue(true);

        repository.obtenerArticulosDisponiblesPorLocal(localID)
                .addOnSuccessListener(querySnapshot -> {
                    List<Articulo> articulos =
                            ArticuloRepository.queryToArticuloList(querySnapshot);
                    articulosLiveData.setValue(articulos);
                    cargandoLiveData.setValue(false);
                    Log.d(TAG, "Artículos disponibles: " + articulos.size());
                })
                .addOnFailureListener(e -> {
                    errorLiveData.setValue("Error al cargar disponibles: " + e.getMessage());
                    cargandoLiveData.setValue(false);
                    Log.e(TAG, "Error al cargar disponibles", e);
                });
    }

    public void cargarArticuloPorId(String id) {
        cargandoLiveData.setValue(true);

        repository.obtenerArticuloPorId(id)
                .addOnSuccessListener(documentSnapshot -> {
                    Articulo articulo =
                            ArticuloRepository.documentToArticulo(documentSnapshot);
                    articuloSeleccionadoLiveData.setValue(articulo);
                    cargandoLiveData.setValue(false);
                })
                .addOnFailureListener(e -> {
                    errorLiveData.setValue("Error al cargar artículo: " + e.getMessage());
                    cargandoLiveData.setValue(false);
                    Log.e(TAG, "Error al cargar artículo", e);
                });
    }

    public void buscarPorNombre(String nombre) {
        cargandoLiveData.setValue(true);

        repository.buscarArticulosPorNombre(nombre)
                .addOnSuccessListener(querySnapshot -> {
                    List<Articulo> articulos =
                            ArticuloRepository.queryToArticuloList(querySnapshot);
                    articulosLiveData.setValue(articulos);
                    cargandoLiveData.setValue(false);
                })
                .addOnFailureListener(e -> {
                    errorLiveData.setValue("Error en búsqueda: " + e.getMessage());
                    cargandoLiveData.setValue(false);
                    Log.e(TAG, "Error en búsqueda", e);
                });
    }

    public void buscarPorRangoPrecio(double min, double max) {
        cargandoLiveData.setValue(true);

        repository.obtenerArticulosPorRangoPrecio(min, max)
                .addOnSuccessListener(querySnapshot -> {
                    List<Articulo> articulos =
                            ArticuloRepository.queryToArticuloList(querySnapshot);
                    articulosLiveData.setValue(articulos);
                    cargandoLiveData.setValue(false);
                })
                .addOnFailureListener(e -> {
                    errorLiveData.setValue("Error al filtrar por precio: " + e.getMessage());
                    cargandoLiveData.setValue(false);
                    Log.e(TAG, "Error al filtrar por precio", e);
                });
    }

    public void crearArticulo(Articulo articulo) {
        cargandoLiveData.setValue(true);

        repository.crearArticulo(articulo)
                .addOnSuccessListener(aVoid -> {
                    cargandoLiveData.setValue(false);
                    Log.d(TAG, "Artículo creado");
                })
                .addOnFailureListener(e -> {
                    errorLiveData.setValue("Error al crear artículo: " + e.getMessage());
                    cargandoLiveData.setValue(false);
                    Log.e(TAG, "Error al crear artículo", e);
                });
    }

    public void actualizarArticulo(Articulo articulo) {
        cargandoLiveData.setValue(true);

        repository.actualizarArticulo(articulo)
                .addOnSuccessListener(aVoid -> {
                    cargandoLiveData.setValue(false);
                    Log.d(TAG, "Artículo actualizado");
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
                    Log.d(TAG, "Disponibilidad actualizada");
                })
                .addOnFailureListener(e -> {
                    errorLiveData.setValue("Error al cambiar disponibilidad: " + e.getMessage());
                    Log.e(TAG, "Error al cambiar disponibilidad", e);
                });
    }

    public void actualizarPrecio(String id, double precio) {
        repository.actualizarPrecio(id, precio)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Precio actualizado");
                })
                .addOnFailureListener(e -> {
                    errorLiveData.setValue("Error al actualizar precio: " + e.getMessage());
                    Log.e(TAG, "Error al actualizar precio", e);
                });
    }

    public void limpiarError() {
        errorLiveData.setValue(null);
    }
}