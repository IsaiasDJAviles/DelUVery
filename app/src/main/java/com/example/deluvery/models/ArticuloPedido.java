package com.example.deluvery.models;

public class ArticuloPedido {

    private String articuloID;
    private int cantidad;
    private double subtotal;

    public ArticuloPedido() {}

    public ArticuloPedido(String articuloID, int cantidad, double subtotal) {
        this.articuloID = articuloID;
        this.cantidad = cantidad;
        this.subtotal = subtotal;
    }

    // getters/setters
}
