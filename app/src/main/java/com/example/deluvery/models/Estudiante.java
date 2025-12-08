package com.example.deluvery.models;

public class Estudiante {

    private String id;
    private String nombre;
    private String correo;
    private String rol; // cliente / repartidor
    private String telefono;
    private String fotoURL;
    private boolean activo;

    public Estudiante() {} // Necesario para Firestore

    public Estudiante(String id, String nombre, String correo, String rol, String telefono, String fotoURL, boolean activo) {
        this.id = id;
        this.nombre = nombre;
        this.correo = correo;
        this.rol = rol;
        this.telefono = telefono;
        this.fotoURL = fotoURL;
        this.activo = activo;
    }

    // Getters y setters
    // ...
}
