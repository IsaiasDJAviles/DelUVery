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

    public RepartidorActividad() { }

    public RepartidorActividad(String id, String repartidorID, String pedidoID,
                               long timestamp, boolean movimientoDetectado,
                               double x, double y, double z) {
        this.id = id;
        this.repartidorID = repartidorID;
        this.pedidoID = pedidoID;
        this.timestamp = timestamp;
        this.movimientoDetectado = movimientoDetectado;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    // Getters y setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getRepartidorID() { return repartidorID; }
    public void setRepartidorID(String repartidorID) { this.repartidorID = repartidorID; }

    public String getPedidoID() { return pedidoID; }
    public void setPedidoID(String pedidoID) { this.pedidoID = pedidoID; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public boolean isMovimientoDetectado() { return movimientoDetectado; }
    public void setMovimientoDetectado(boolean movimientoDetectado) { this.movimientoDetectado = movimientoDetectado; }

    public double getX() { return x; }
    public void setX(double x) { this.x = x; }

    public double getY() { return y; }
    public void setY(double y) { this.y = y; }

    public double getZ() { return z; }
    public void setZ(double z) { this.z = z; }
}
