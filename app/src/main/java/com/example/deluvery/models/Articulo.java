package com.example.deluvery.models;

public class Articulo {

    private String id;
    private String nombre;
    private double precio;
    private String descripcion;
    private String imagenURL;
    private String localID;
    private boolean disponible;

    public Articulo() {}

    public Articulo(String id, String nombre, double precio, String descripcion, String imagenURL, String localID, boolean disponible) {
        this.id = id;
        this.nombre = nombre;
        this.precio = precio;
        this.descripcion = descripcion;
        this.imagenURL = imagenURL;
        this.localID = localID;
        this.disponible = disponible;
    }

    // getters/setters
}
