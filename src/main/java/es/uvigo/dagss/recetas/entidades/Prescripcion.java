package es.uvigo.dagss.recetas.entidades;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
public class Prescripcion implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "medicamento_id", nullable = false)
    private Medicamento medicamento;

    @ManyToOne
    @JoinColumn(name = "paciente_id", nullable = false)
    private Paciente paciente;

    @ManyToOne
    @JoinColumn(name = "medico_id", nullable = false)
    private Medico medico;

    private Double dosisDiaria;
    
    @Column(length = 1000)
    private String indicaciones;

    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private Boolean activa = true;

    @OneToMany(mappedBy = "prescripcion", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Receta> recetas = new ArrayList<>();

    public Prescripcion() {
    }

    public Prescripcion(Medicamento medicamento, Paciente paciente, Medico medico, 
                        Double dosisDiaria, String indicaciones, LocalDate fechaInicio, LocalDate fechaFin) {
        this.medicamento = medicamento;
        this.paciente = paciente;
        this.medico = medico;
        this.dosisDiaria = dosisDiaria;
        this.indicaciones = indicaciones;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.activa = true;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Medicamento getMedicamento() {
        return medicamento;
    }

    public void setMedicamento(Medicamento medicamento) {
        this.medicamento = medicamento;
    }

    public Paciente getPaciente() {
        return paciente;
    }

    public void setPaciente(Paciente paciente) {
        this.paciente = paciente;
    }

    public Medico getMedico() {
        return medico;
    }

    public void setMedico(Medico medico) {
        this.medico = medico;
    }

    public Double getDosisDiaria() {
        return dosisDiaria;
    }

    public void setDosisDiaria(Double dosisDiaria) {
        this.dosisDiaria = dosisDiaria;
    }

    public String getIndicaciones() {
        return indicaciones;
    }

    public void setIndicaciones(String indicaciones) {
        this.indicaciones = indicaciones;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDate fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public LocalDate getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(LocalDate fechaFin) {
        this.fechaFin = fechaFin;
    }

    public Boolean getActiva() {
        return activa;
    }

    public void setActiva(Boolean activa) {
        this.activa = activa;
    }

    public void activar() {
        this.activa = true;
    }

    public void desactivar() {
        this.activa = false;
    }

    public List<Receta> getRecetas() {
        return recetas;
    }

    public void setRecetas(List<Receta> recetas) {
        this.recetas = recetas;
    }

    public void agregarReceta(Receta receta) {
        recetas.add(receta);
        receta.setPrescripcion(this);
    }

    public void eliminarReceta(Receta receta) {
        recetas.remove(receta);
        receta.setPrescripcion(null);
    }
}

