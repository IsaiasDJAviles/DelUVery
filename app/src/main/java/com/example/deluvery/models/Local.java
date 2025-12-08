package com.example.deluvery.models;

public class Local {

    private String id;
    private String nombre;
    private String horarioApertura;
    private String horarioCierre;
    private boolean disponible;

    public Local() {}

    public Local(String id, String nombre, String horarioApertura, String horarioCierre, boolean disponible) {
        this.id = id;
        this.nombre = nombre;
        this.horarioApertura = horarioApertura;
        this.horarioCierre = horarioCierre;
        this.disponible = disponible;
    }

    // getters/setters
}
