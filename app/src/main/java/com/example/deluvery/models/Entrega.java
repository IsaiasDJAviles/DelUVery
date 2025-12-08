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

    public Entrega() {}

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

    // getters/setters
}
