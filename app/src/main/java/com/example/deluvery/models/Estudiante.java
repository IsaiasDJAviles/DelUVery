package com.example.deluvery.models;

public class Estudiante {

    private String id;
    private String nombre;
    private String correo;
    private String rol; // cliente | repartidor
    private String telefono;
    private String fotoURL;
    private boolean activo;

    public Estudiante() { }

    public Estudiante(String id, String nombre, String correo, String rol,
                      String telefono, String fotoURL, boolean activo) {
        this.id = id;
        this.nombre = nombre;
        this.correo = correo;
        this.rol = rol;
        this.telefono = telefono;
        this.fotoURL = fotoURL;
        this.activo = activo;
    }

    // Getters y setters

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }

    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getFotoURL() { return fotoURL; }
    public void setFotoURL(String fotoURL) { this.fotoURL = fotoURL; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
}
