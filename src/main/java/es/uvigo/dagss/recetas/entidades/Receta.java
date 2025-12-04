package es.uvigo.dagss.recetas.entidades;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

@Entity
public class Receta implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "prescripcion_id", nullable = false)
    private Prescripcion prescripcion;

    private LocalDate fechaValidezInicial;
    private LocalDate fechaValidezFinal;
    private Integer numeroUnidades = 1;

    @Enumerated(EnumType.STRING)
    private EstadoReceta estado = EstadoReceta.PLANIFICADA;

    @ManyToOne
    @JoinColumn(name = "farmacia_id")
    private Farmacia farmacia;

    public Receta() {
    }

    public Receta(Prescripcion prescripcion, LocalDate fechaValidezInicial, LocalDate fechaValidezFinal) {
        this.prescripcion = prescripcion;
        this.fechaValidezInicial = fechaValidezInicial;
        this.fechaValidezFinal = fechaValidezFinal;
        this.numeroUnidades = 1;
        this.estado = EstadoReceta.PLANIFICADA;
    }

    public Receta(Prescripcion prescripcion, LocalDate fechaValidezInicial, LocalDate fechaValidezFinal, Integer numeroUnidades) {
        this.prescripcion = prescripcion;
        this.fechaValidezInicial = fechaValidezInicial;
        this.fechaValidezFinal = fechaValidezFinal;
        this.numeroUnidades = numeroUnidades;
        this.estado = EstadoReceta.PLANIFICADA;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Prescripcion getPrescripcion() {
        return prescripcion;
    }

    public void setPrescripcion(Prescripcion prescripcion) {
        this.prescripcion = prescripcion;
    }

    public LocalDate getFechaValidezInicial() {
        return fechaValidezInicial;
    }

    public void setFechaValidezInicial(LocalDate fechaValidezInicial) {
        this.fechaValidezInicial = fechaValidezInicial;
    }

    public LocalDate getFechaValidezFinal() {
        return fechaValidezFinal;
    }

    public void setFechaValidezFinal(LocalDate fechaValidezFinal) {
        this.fechaValidezFinal = fechaValidezFinal;
    }

    public Integer getNumeroUnidades() {
        return numeroUnidades;
    }

    public void setNumeroUnidades(Integer numeroUnidades) {
        this.numeroUnidades = numeroUnidades;
    }

    public EstadoReceta getEstado() {
        return estado;
    }

    public void setEstado(EstadoReceta estado) {
        this.estado = estado;
    }

    public Farmacia getFarmacia() {
        return farmacia;
    }

    public void setFarmacia(Farmacia farmacia) {
        this.farmacia = farmacia;
    }

    public void marcarComoServida(Farmacia farmacia) {
        this.estado = EstadoReceta.SERVIDA;
        this.farmacia = farmacia;
    }

    public void marcarComoAnulada() {
        this.estado = EstadoReceta.ANULADA;
    }

    public boolean puedeSerServida(LocalDate fechaActual) {
        return estado == EstadoReceta.PLANIFICADA 
            && !fechaActual.isBefore(fechaValidezInicial) 
            && !fechaActual.isAfter(fechaValidezFinal);
    }
}

