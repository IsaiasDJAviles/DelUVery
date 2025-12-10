package com.example.deluvery.models;

import java.io.Serializable;

public class CarritoItem implements Serializable {

    private String articuloID;
    private String nombre;
    private String descripcion;
    private String imagenURL;
    private double precio;
    private int cantidad;
    private String localID;
    private String localNombre;

    public CarritoItem() { }

    public CarritoItem(Articulo articulo, int cantidad) {
        this.articuloID = articulo.getId();
        this.nombre = articulo.getNombre();
        this.descripcion = articulo.getDescripcion();
        this.imagenURL = articulo.getImagenURL();
        this.precio = articulo.getPrecio();
        this.cantidad = cantidad;
        this.localID = articulo.getLocalID();
    }

    public double getSubtotal() {
        return precio * cantidad;
    }

    // Getters y setters
    public String getArticuloID() { return articuloID; }
    public void setArticuloID(String articuloID) { this.articuloID = articuloID; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getImagenURL() { return imagenURL; }
    public void setImagenURL(String imagenURL) { this.imagenURL = imagenURL; }

    public double getPrecio() { return precio; }
    public void setPrecio(double precio) { this.precio = precio; }

    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }

    public String getLocalID() { return localID; }
    public void setLocalID(String localID) { this.localID = localID; }

    public String getLocalNombre() { return localNombre; }
    public void setLocalNombre(String localNombre) { this.localNombre = localNombre; }
}