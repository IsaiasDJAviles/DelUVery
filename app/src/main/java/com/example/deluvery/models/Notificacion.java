package com.example.deluvery.models;

import java.util.Date;

public class Notificacion {

    private String id;
    private String usuarioID;
    private String pedidoID;
    private String tipo;
    private String titulo;
    private String mensaje;
    private Date fecha;
    private boolean leida;

    public Notificacion() {}

    public Notificacion(String id, String usuarioID, String pedidoID, String tipo,
                        String titulo, String mensaje, Date fecha, boolean leida) {
        this.id = id;
        this.usuarioID = usuarioID;
        this.pedidoID = pedidoID;
        this.tipo = tipo;
        this.titulo = titulo;
        this.mensaje = mensaje;
        this.fecha = fecha;
        this.leida = leida;
    }

    // Getters y setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUsuarioID() { return usuarioID; }
    public void setUsuarioID(String usuarioID) { this.usuarioID = usuarioID; }

    public String getPedidoID() { return pedidoID; }
    public void setPedidoID(String pedidoID) { this.pedidoID = pedidoID; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }

    public Date getFecha() { return fecha; }
    public void setFecha(Date fecha) { this.fecha = fecha; }

    public boolean isLeida() { return leida; }
    public void setLeida(boolean leida) { this.leida = leida; }
}