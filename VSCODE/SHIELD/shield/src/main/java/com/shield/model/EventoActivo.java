package com.shield.model;

import java.time.LocalDateTime;

public class EventoActivo {
    private final LocalDateTime fecha;
    private final String evento;
    private final String detalle;
    private final String usuario;

    public EventoActivo(LocalDateTime fecha, String evento, String detalle, String usuario) {
        this.fecha = fecha;
        this.evento = evento;
        this.detalle = detalle;
        this.usuario = usuario;
    }

    public LocalDateTime getFecha() { return fecha; }
    public String getEvento() { return evento; }
    public String getDetalle() { return detalle; }
    public String getUsuario() { return usuario; }
}
