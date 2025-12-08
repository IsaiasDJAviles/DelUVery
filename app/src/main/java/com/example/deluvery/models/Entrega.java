package com.example.deluvery.models;

import java.util.Date;

public class Entrega {

    private String id;
    private String pedidoID;
    private String repartidorID;
    private Date horaSalida;
    private Date horaLlegada;
    private double inicioLat;
    private double inicioLng;
    private double llegadaLat;
    private double llegadaLng;
    private boolean codigoQRValidado;

    public Entrega() { }

    public Entrega(String id, String pedidoID, String repartidorID, Date horaSalida,
                   Date horaLlegada, double inicioLat, double inicioLng,
                   double llegadaLat, double llegadaLng, boolean codigoQRValidado) {

        this.id = id;
        this.pedidoID = pedidoID;
        this.repartidorID = repartidorID;
        this.horaSalida = horaSalida;
        this.horaLlegada = horaLlegada;
        this.inicioLat = inicioLat;
        this.inicioLng = inicioLng;
        this.llegadaLat = llegadaLat;
        this.llegadaLng = llegadaLng;
        this.codigoQRValidado = codigoQRValidado;
    }

    // Getters y setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getPedidoID() { return pedidoID; }
    public void setPedidoID(String pedidoID) { this.pedidoID = pedidoID; }

    public String getRepartidorID() { return repartidorID; }
    public void setRepartidorID(String repartidorID) { this.repartidorID = repartidorID; }

    public Date getHoraSalida() { return horaSalida; }
    public void setHoraSalida(Date horaSalida) { this.horaSalida = horaSalida; }

    public Date getHoraLlegada() { return horaLlegada; }
    public void setHoraLlegada(Date horaLlegada) { this.horaLlegada = horaLlegada; }

    public double getInicioLat() { return inicioLat; }
    public void setInicioLat(double inicioLat) { this.inicioLat = inicioLat; }

    public double getInicioLng() { return inicioLng; }
    public void setInicioLng(double inicioLng) { this.inicioLng = inicioLng; }

    public double getLlegadaLat() { return llegadaLat; }
    public void setLlegadaLat(double llegadaLat) { this.llegadaLat = llegadaLat; }

    public double getLlegadaLng() { return llegadaLng; }
    public void setLlegadaLng(double llegadaLng) { this.llegadaLng = llegadaLng; }

    public boolean isCodigoQRValidado() { return codigoQRValidado; }
    public void setCodigoQRValidado(boolean codigoQRValidado) { this.codigoQRValidado = codigoQRValidado; }
}
