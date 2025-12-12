package com.example.deluvery.models;

import java.io.Serializable;
import java.util.Date;

public class Pedido implements Serializable {

    private String id;
    private String clienteID;
    private String repartidorID;
    private String localID;
    private String estado; // pendiente, asignado, en_camino, entregado, cancelado
    private double total;
    private Date fecha;
    private String salonEntrega;
    private double lat;
    private double lng;
    private String codigoQR;
    private String anotaciones;

    public Pedido() {
        // Constructor vacío requerido por Firestore
    }

    public Pedido(String id, String clienteID, String localID, String estado,
                  double total, Date fecha, String salonEntrega) {
        this.id = id;
        this.clienteID = clienteID;
        this.localID = localID;
        this.estado = estado;
        this.total = total;
        this.fecha = fecha;
        this.salonEntrega = salonEntrega;
        this.lat = 0.0;
        this.lng = 0.0;
        this.codigoQR = "";
        this.anotaciones = "";
    }

    // Getters y Setters
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

    public String getAnotaciones() { return anotaciones; }
    public void setAnotaciones(String anotaciones) { this.anotaciones = anotaciones; }
}