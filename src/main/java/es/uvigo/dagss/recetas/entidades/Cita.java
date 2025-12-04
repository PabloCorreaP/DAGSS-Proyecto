package es.uvigo.dagss.recetas.entidades;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;

@Entity
public class Cita implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "paciente_id", nullable = false)
    private Paciente paciente;

    @ManyToOne
    @JoinColumn(name = "medico_id", nullable = false)
    private Medico medico;

    private LocalDate fecha;
    private LocalTime horaInicio;
    private Integer duracion = 15;

    @Enumerated(EnumType.STRING)
    private EstadoCita estado = EstadoCita.PLANIFICADA;

    public Cita() {
    }

    public Cita(Paciente paciente, Medico medico, LocalDate fecha, LocalTime horaInicio) {
        this.paciente = paciente;
        this.medico = medico;
        this.fecha = fecha;
        this.horaInicio = horaInicio;
        this.duracion = 15;
        this.estado = EstadoCita.PLANIFICADA;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public LocalTime getHoraInicio() {
        return horaInicio;
    }

    public void setHoraInicio(LocalTime horaInicio) {
        this.horaInicio = horaInicio;
    }

    public Integer getDuracion() {
        return duracion;
    }

    public void setDuracion(Integer duracion) {
        this.duracion = duracion;
    }

    public EstadoCita getEstado() {
        return estado;
    }

    public void setEstado(EstadoCita estado) {
        this.estado = estado;
    }

    public void marcarComoAnulada() {
        this.estado = EstadoCita.ANULADA;
    }

    public void marcarComoCompletada() {
        this.estado = EstadoCita.COMPLETADA;
    }

    public void marcarComoAusente() {
        this.estado = EstadoCita.AUSENTE;
    }
}

