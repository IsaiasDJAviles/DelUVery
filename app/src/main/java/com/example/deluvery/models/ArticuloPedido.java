package com.example.deluvery.models;

import java.io.Serializable;

public class ArticuloPedido implements Serializable {

    private String articuloID;
    private int cantidad;
    private double subtotal;

    public ArticuloPedido() {
        // Constructor vacío requerido por Firestore
    }

    public ArticuloPedido(String articuloID, int cantidad, double subtotal) {
        this.articuloID = articuloID;
        this.cantidad = cantidad;
        this.subtotal = subtotal;
    }

    // Getters y Setters
    public String getArticuloID() { return articuloID; }
    public void setArticuloID(String articuloID) { this.articuloID = articuloID; }

    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }

    public double getSubtotal() { return subtotal; }
    public void setSubtotal(double subtotal) { this.subtotal = subtotal; }
}