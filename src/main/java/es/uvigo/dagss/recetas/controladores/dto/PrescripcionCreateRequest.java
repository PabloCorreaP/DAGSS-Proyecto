package es.uvigo.dagss.recetas.controladores.dto;

public class PrescripcionCreateRequest {
    public Long pacienteId;
    public Long medicoId;
    public Long medicamentoId;
    public Double dosisDiaria;
    public String indicaciones;
    /** yyyy-MM-dd */
    public String fechaFin;
}
