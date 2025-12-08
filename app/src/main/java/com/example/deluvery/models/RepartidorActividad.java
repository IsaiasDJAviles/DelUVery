package com.example.deluvery.models;

public class RepartidorActividad {

    private String id;
    private String repartidorID;
    private String pedidoID;
    private long timestamp;
    private boolean movimientoDetectado;
    private double x;
    private double y;
    private double z;

    public RepartidorActividad() {}

    public RepartidorActividad(String id, String repartidorID, String pedidoID, long timestamp,
                               boolean movimientoDetectado, double x, double y, double z) {
        this.id = id;
        this.repartidorID = repartidorID;
        this.pedidoID = pedidoID;
        this.timestamp = timestamp;
        this.movimientoDetectado = movimientoDetectado;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    // getters/setters
}
