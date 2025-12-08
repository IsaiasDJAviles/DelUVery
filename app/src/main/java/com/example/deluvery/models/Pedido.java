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

    public Pedido() {}

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

    // getters/setters
}
