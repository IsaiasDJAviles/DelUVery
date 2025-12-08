package com.example.deluvery.models;

public class Articulo {

    private String id;
    private String nombre;
    private double precio;
    private String descripcion;
    private String imagenURL;
    private String localID;
    private boolean disponible;

    public Articulo() { }

    public Articulo(String id, String nombre, double precio, String descripcion,
                    String imagenURL, String localID, boolean disponible) {
        this.id = id;
        this.nombre = nombre;
        this.precio = precio;
        this.descripcion = descripcion;
        this.imagenURL = imagenURL;
        this.localID = localID;
        this.disponible = disponible;
    }

    // Getters / setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public double getPrecio() { return precio; }
    public void setPrecio(double precio) { this.precio = precio; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getImagenURL() { return imagenURL; }
    public void setImagenURL(String imagenURL) { this.imagenURL = imagenURL; }

    public String getLocalID() { return localID; }
    public void setLocalID(String localID) { this.localID = localID; }

    public boolean isDisponible() { return disponible; }
    public void setDisponible(boolean disponible) { this.disponible = disponible; }
}
