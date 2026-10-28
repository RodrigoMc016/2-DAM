package com.shield.model;

import java.time.LocalDateTime;

public class Activo {
    private int id;
    private String codigo;
    private TipoActivo tipo;
    private String fabricante;
    private String modelo;
    private String numeroSerie;
    private EstadoActivo estado;
    private LocalDateTime fechaAlta;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }

    public TipoActivo getTipo() { return tipo; }
    public void setTipo(TipoActivo tipo) { this.tipo = tipo; }

    public String getFabricante() { return fabricante; }
    public void setFabricante(String fabricante) { this.fabricante = fabricante; }

    public String getModelo() { return modelo; }
    public void setModelo(String modelo) { this.modelo = modelo; }

    public String getNumeroSerie() { return numeroSerie; }
    public void setNumeroSerie(String numeroSerie) { this.numeroSerie = numeroSerie; }

    public EstadoActivo getEstado() { return estado; }
    public void setEstado(EstadoActivo estado) { this.estado = estado; }

    public LocalDateTime getFechaAlta() { return fechaAlta; }
    public void setFechaAlta(LocalDateTime fechaAlta) { this.fechaAlta = fechaAlta; }
}
