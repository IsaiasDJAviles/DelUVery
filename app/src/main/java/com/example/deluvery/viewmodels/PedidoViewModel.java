package com.example.deluvery.viewmodels;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.deluvery.models.ArticuloPedido;
import com.example.deluvery.models.Pedido;
import com.example.deluvery.repositories.PedidoRepository;

import java.util.Date;
import java.util.List;

public class PedidoViewModel extends ViewModel {

    private static final String TAG = "PedidoViewModel";

    private final PedidoRepository repository;

    private final MutableLiveData<List<Pedido>> pedidosLiveData;
    private final MutableLiveData<List<Pedido>> pedidosPendientesLiveData;
    private final MutableLiveData<List<ArticuloPedido>> articulosPedidoLiveData;
    private final MutableLiveData<Pedido> pedidoSeleccionadoLiveData;
    private final MutableLiveData<Boolean> cargandoLiveData;
    private final MutableLiveData<String> errorLiveData;
    private final MutableLiveData<String> mensajeExitoLiveData;

    public PedidoViewModel() {
        repository = new PedidoRepository();
        pedidosLiveData = new MutableLiveData<>();
        pedidosPendientesLiveData = new MutableLiveData<>();
        articulosPedidoLiveData = new MutableLiveData<>();
        pedidoSeleccionadoLiveData = new MutableLiveData<>();
        cargandoLiveData = new MutableLiveData<>(false);
        errorLiveData = new MutableLiveData<>();
        mensajeExitoLiveData = new MutableLiveData<>();
    }

    // Getters para LiveData
    public LiveData<List<Pedido>> getPedidos() {
        return pedidosLiveData;
    }

    public LiveData<List<Pedido>> getPedidosPendientes() {
        return pedidosPendientesLiveData;
    }

    public LiveData<List<ArticuloPedido>> getArticulosPedido() {
        return articulosPedidoLiveData;
    }

    public LiveData<Pedido> getPedidoSeleccionado() {
        return pedidoSeleccionadoLiveData;
    }

    public LiveData<Boolean> getCargando() {
        return cargandoLiveData;
    }

    public LiveData<String> getError() {
        return errorLiveData;
    }

    public LiveData<String> getMensajeExito() {
        return mensajeExitoLiveData;
    }

    // Cargar pedidos por cliente
    public void cargarPedidosCliente(String clienteID) {
        cargandoLiveData.setValue(true);

        repository.obtenerPedidosPorCliente(clienteID)
                .addOnSuccessListener(querySnapshot -> {
                    List<Pedido> pedidos =
                            PedidoRepository.queryToPedidoList(querySnapshot);
                    pedidosLiveData.setValue(pedidos);
                    cargandoLiveData.setValue(false);
                    Log.d(TAG, "Pedidos del cliente cargados: " + pedidos.size());
                })
                .addOnFailureListener(e -> {
                    errorLiveData.setValue("Error al cargar pedidos: " + e.getMessage());
                    cargandoLiveData.setValue(false);
                    Log.e(TAG, "Error al cargar pedidos", e);
                });
    }

    // Cargar pedidos por repartidor
    public void cargarPedidosRepartidor(String repartidorID) {
        cargandoLiveData.setValue(true);

        repository.obtenerPedidosPorRepartidor(repartidorID)
                .addOnSuccessListener(querySnapshot -> {
                    List<Pedido> pedidos =
                            PedidoRepository.queryToPedidoList(querySnapshot);
                    pedidosLiveData.setValue(pedidos);
                    cargandoLiveData.setValue(false);
                    Log.d(TAG, "Pedidos del repartidor cargados: " + pedidos.size());
                })
                .addOnFailureListener(e -> {
                    errorLiveData.setValue("Error al cargar pedidos: " + e.getMessage());
                    cargandoLiveData.setValue(false);
                    Log.e(TAG, "Error al cargar pedidos", e);
                });
    }

    // Cargar pedidos pendientes
    public void cargarPedidosPendientes() {
        cargandoLiveData.setValue(true);

        repository.obtenerPedidosPendientes()
                .addOnSuccessListener(querySnapshot -> {
                    List<Pedido> pedidos =
                            PedidoRepository.queryToPedidoList(querySnapshot);
                    pedidosPendientesLiveData.setValue(pedidos);
                    cargandoLiveData.setValue(false);
                    Log.d(TAG, "Pedidos pendientes: " + pedidos.size());
                })
                .addOnFailureListener(e -> {
                    errorLiveData.setValue("Error al cargar pendientes: " + e.getMessage());
                    cargandoLiveData.setValue(false);
                    Log.e(TAG, "Error al cargar pendientes", e);
                });
    }

    // Cargar pedidos por estado
    public void cargarPedidosPorEstado(String estado) {
        cargandoLiveData.setValue(true);

        repository.obtenerPedidosPorEstado(estado)
                .addOnSuccessListener(querySnapshot -> {
                    List<Pedido> pedidos =
                            PedidoRepository.queryToPedidoList(querySnapshot);
                    pedidosLiveData.setValue(pedidos);
                    cargandoLiveData.setValue(false);
                })
                .addOnFailureListener(e -> {
                    errorLiveData.setValue("Error al filtrar por estado: " + e.getMessage());
                    cargandoLiveData.setValue(false);
                    Log.e(TAG, "Error al filtrar por estado", e);
                });
    }

    // Cargar pedidos activos de repartidor
    public void cargarPedidosActivos(String repartidorID) {
        cargandoLiveData.setValue(true);

        repository.obtenerPedidosActivosRepartidor(repartidorID)
                .addOnSuccessListener(querySnapshot -> {
                    List<Pedido> pedidos =
                            PedidoRepository.queryToPedidoList(querySnapshot);
                    pedidosLiveData.setValue(pedidos);
                    cargandoLiveData.setValue(false);
                })
                .addOnFailureListener(e -> {
                    errorLiveData.setValue("Error al cargar activos: " + e.getMessage());
                    cargandoLiveData.setValue(false);
                    Log.e(TAG, "Error al cargar activos", e);
                });
    }

    // Cargar pedidos por local
    public void cargarPedidosPorLocal(String localID) {
        cargandoLiveData.setValue(true);

        repository.obtenerPedidosPorLocal(localID)
                .addOnSuccessListener(querySnapshot -> {
                    List<Pedido> pedidos =
                            PedidoRepository.queryToPedidoList(querySnapshot);
                    pedidosLiveData.setValue(pedidos);
                    cargandoLiveData.setValue(false);
                })
                .addOnFailureListener(e -> {
                    errorLiveData.setValue("Error al cargar por local: " + e.getMessage());
                    cargandoLiveData.setValue(false);
                    Log.e(TAG, "Error al cargar por local", e);
                });
    }

    // Cargar pedido por ID
    public void cargarPedidoPorId(String id) {
        cargandoLiveData.setValue(true);

        repository.obtenerPedidoPorId(id)
                .addOnSuccessListener(documentSnapshot -> {
                    Pedido pedido = PedidoRepository.documentToPedido(documentSnapshot);
                    pedidoSeleccionadoLiveData.setValue(pedido);
                    cargandoLiveData.setValue(false);
                })
                .addOnFailureListener(e -> {
                    errorLiveData.setValue("Error al cargar pedido: " + e.getMessage());
                    cargandoLiveData.setValue(false);
                    Log.e(TAG, "Error al cargar pedido", e);
                });
    }

    // Cargar artículos de un pedido
    public void cargarArticulosPedido(String pedidoID) {
        cargandoLiveData.setValue(true);

        repository.obtenerArticulosDePedido(pedidoID)
                .addOnSuccessListener(querySnapshot -> {
                    List<ArticuloPedido> articulos =
                            PedidoRepository.queryToArticuloPedidoList(querySnapshot);
                    articulosPedidoLiveData.setValue(articulos);
                    cargandoLiveData.setValue(false);
                    Log.d(TAG, "Artículos del pedido: " + articulos.size());
                })
                .addOnFailureListener(e -> {
                    errorLiveData.setValue("Error al cargar artículos: " + e.getMessage());
                    cargandoLiveData.setValue(false);
                    Log.e(TAG, "Error al cargar artículos", e);
                });
    }

    // Crear pedido con artículos
    public void crearPedido(Pedido pedido, List<ArticuloPedido> articulos) {
        cargandoLiveData.setValue(true);

        repository.crearPedidoConArticulos(pedido, articulos)
                .addOnSuccessListener(aVoid -> {
                    mensajeExitoLiveData.setValue("Pedido creado exitosamente");
                    cargandoLiveData.setValue(false);
                    Log.d(TAG, "Pedido creado");
                })
                .addOnFailureListener(e -> {
                    errorLiveData.setValue("Error al crear pedido: " + e.getMessage());
                    cargandoLiveData.setValue(false);
                    Log.e(TAG, "Error al crear pedido", e);
                });
    }

    // Actualizar estado del pedido
    public void actualizarEstado(String pedidoID, String estado) {
        repository.actualizarEstado(pedidoID, estado)
                .addOnSuccessListener(aVoid -> {
                    mensajeExitoLiveData.setValue("Estado actualizado a: " + estado);
                    cargarPedidoPorId(pedidoID); // Recargar pedido
                    Log.d(TAG, "Estado actualizado");
                })
                .addOnFailureListener(e -> {
                    errorLiveData.setValue("Error al actualizar estado: " + e.getMessage());
                    Log.e(TAG, "Error al actualizar estado", e);
                });
    }

    // Asignar repartidor
    public void asignarRepartidor(String pedidoID, String repartidorID) {
        cargandoLiveData.setValue(true);

        repository.asignarRepartidor(pedidoID, repartidorID)
                .addOnSuccessListener(aVoid -> {
                    mensajeExitoLiveData.setValue("Repartidor asignado");
                    cargandoLiveData.setValue(false);
                    cargarPedidoPorId(pedidoID); // Recargar pedido
                    Log.d(TAG, "Repartidor asignado");
                })
                .addOnFailureListener(e -> {
                    errorLiveData.setValue("Error al asignar repartidor: " + e.getMessage());
                    cargandoLiveData.setValue(false);
                    Log.e(TAG, "Error al asignar repartidor", e);
                });
    }

    // Actualizar ubicación
    public void actualizarUbicacion(String pedidoID, double lat, double lng) {
        repository.actualizarUbicacion(pedidoID, lat, lng)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Ubicación actualizada");
                })
                .addOnFailureListener(e -> {
                    errorLiveData.setValue("Error al actualizar ubicación: " + e.getMessage());
                    Log.e(TAG, "Error al actualizar ubicación", e);
                });
    }

    // Validar código QR y marcar como entregado
    public void validarEntrega(String pedidoID) {
        cargandoLiveData.setValue(true);

        repository.validarCodigoQR(pedidoID, true)
                .addOnSuccessListener(aVoid -> {
                    mensajeExitoLiveData.setValue("Pedido entregado exitosamente");
                    cargandoLiveData.setValue(false);
                    cargarPedidoPorId(pedidoID);
                    Log.d(TAG, "Pedido entregado");
                })
                .addOnFailureListener(e -> {
                    errorLiveData.setValue("Error al validar entrega: " + e.getMessage());
                    cargandoLiveData.setValue(false);
                    Log.e(TAG, "Error al validar entrega", e);
                });
    }

    // Cancelar pedido
    public void cancelarPedido(String pedidoID) {
        cargandoLiveData.setValue(true);

        repository.cancelarPedido(pedidoID)
                .addOnSuccessListener(aVoid -> {
                    mensajeExitoLiveData.setValue("Pedido cancelado");
                    cargandoLiveData.setValue(false);
                    cargarPedidoPorId(pedidoID);
                    Log.d(TAG, "Pedido cancelado");
                })
                .addOnFailureListener(e -> {
                    errorLiveData.setValue("Error al cancelar: " + e.getMessage());
                    cargandoLiveData.setValue(false);
                    Log.e(TAG, "Error al cancelar", e);
                });
    }

    // Limpiar mensajes
    public void limpiarMensajes() {
        errorLiveData.setValue(null);
        mensajeExitoLiveData.setValue(null);
    }
}