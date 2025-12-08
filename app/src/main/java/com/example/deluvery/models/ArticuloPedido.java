package com.example.deluvery.models;

public class ArticuloPedido {

    private String articuloID;
    private int cantidad;
    private double subtotal;

    public ArticuloPedido() { }

    public ArticuloPedido(String articuloID, int cantidad, double subtotal) {
        this.articuloID = articuloID;
        this.cantidad = cantidad;
        this.subtotal = subtotal;
    }

    // Getters y setters
    public String getArticuloID() { return articuloID; }
    public void setArticuloID(String articuloID) { this.articuloID = articuloID; }

    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }

    public double getSubtotal() { return subtotal; }
    public void setSubtotal(double subtotal) { this.subtotal = subtotal; }
}
