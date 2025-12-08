package com.example.deluvery.models;

import java.util.Date;

public class Pedido {

    private String id;
    private String clienteID;
    private String repartidorID;
    private String localID;
    private String estado;
    private double total;
    private Date fecha;
    private String salonEntrega;
    private double lat;
    private double lng;
    private String codigoQR;

    public Pedido() { }

    public Pedido(String id, String clienteID, String repartidorID, String localID,
                  String estado, double total, Date fecha, String salonEntrega,
                  double lat, double lng, String codigoQR) {
        this.id = id;
        this.clienteID = clienteID;
        this.repartidorID = repartidorID;
        this.localID = localID;
        this.estado = estado;
        this.total = total;
        this.fecha = fecha;
        this.salonEntrega = salonEntrega;
        this.lat = lat;
        this.lng = lng;
        this.codigoQR = codigoQR;
    }

    // Getters y setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getClienteID() { return clienteID; }
    public void setClienteID(String clienteID) { this.clienteID = clienteID; }

    public String getRepartidorID() { return repartidorID; }
    public void setRepartidorID(String repartidorID) { this.repartidorID = repartidorID; }

    public String getLocalID() { return localID; }
    public void setLocalID(String localID) { this.localID = localID; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }

    public Date getFecha() { return fecha; }
    public void setFecha(Date fecha) { this.fecha = fecha; }

    public String getSalonEntrega() { return salonEntrega; }
    public void setSalonEntrega(String salonEntrega) { this.salonEntrega = salonEntrega; }

    public double getLat() { return lat; }
    public void setLat(double lat) { this.lat = lat; }

    public double getLng() { return lng; }
    public void setLng(double lng) { this.lng = lng; }

    public String getCodigoQR() { return codigoQR; }
    public void setCodigoQR(String codigoQR) { this.codigoQR = codigoQR; }
}
