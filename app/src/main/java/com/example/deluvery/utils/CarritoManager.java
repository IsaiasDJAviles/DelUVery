package com.example.deluvery.utils;

import com.example.deluvery.models.Articulo;
import com.example.deluvery.models.CarritoItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CarritoManager {

    private static CarritoManager instance;
    private Map<String, CarritoItem> items;
    private static final double COSTO_SERVICIO = 5.0;

    private CarritoManager() {
        items = new HashMap<>();
    }

    public static synchronized CarritoManager getInstance() {
        if (instance == null) {
            instance = new CarritoManager();
        }
        return instance;
    }

    public void agregarArticulo(Articulo articulo, String localNombre) {
        String id = articulo.getId();

        if (items.containsKey(id)) {
            CarritoItem item = items.get(id);
            item.setCantidad(item.getCantidad() + 1);
        } else {
            CarritoItem nuevoItem = new CarritoItem(articulo, 1);
            nuevoItem.setLocalNombre(localNombre);
            items.put(id, nuevoItem);
        }
    }

    public void eliminarArticulo(String articuloID) {
        items.remove(articuloID);
    }

    public void actualizarCantidad(String articuloID, int cantidad) {
        if (cantidad <= 0) {
            eliminarArticulo(articuloID);
        } else {
            CarritoItem item = items.get(articuloID);
            if (item != null) {
                item.setCantidad(cantidad);
            }
        }
    }

    public void incrementarCantidad(String articuloID) {
        CarritoItem item = items.get(articuloID);
        if (item != null) {
            item.setCantidad(item.getCantidad() + 1);
        }
    }

    public void decrementarCantidad(String articuloID) {
        CarritoItem item = items.get(articuloID);
        if (item != null) {
            int nuevaCantidad = item.getCantidad() - 1;
            if (nuevaCantidad <= 0) {
                eliminarArticulo(articuloID);
            } else {
                item.setCantidad(nuevaCantidad);
            }
        }
    }

    public List<CarritoItem> getItems() {
        return new ArrayList<>(items.values());
    }

    public int getCantidadTotal() {
        int total = 0;
        for (CarritoItem item : items.values()) {
            total += item.getCantidad();
        }
        return total;
    }

    public double getSubtotal() {
        double subtotal = 0;
        for (CarritoItem item : items.values()) {
            subtotal += item.getSubtotal();
        }
        return subtotal;
    }

    public double getCostoServicio() {
        return COSTO_SERVICIO;
    }

    public double getTotal() {
        return getSubtotal() + COSTO_SERVICIO;
    }

    public boolean estaVacio() {
        return items.isEmpty();
    }

    public void limpiar() {
        items.clear();
    }

    public String getLocalID() {
        if (items.isEmpty()) return null;
        return items.values().iterator().next().getLocalID();
    }

    public String getLocalNombre() {
        if (items.isEmpty()) return null;
        return items.values().iterator().next().getLocalNombre();
    }
}