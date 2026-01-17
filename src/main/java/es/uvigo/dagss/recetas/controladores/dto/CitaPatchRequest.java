package es.uvigo.dagss.recetas.controladores.dto;

/**
 * PATCH /api/citas/{id}
 *  - estado: ANULADA | AUSENTE | COMPLETADA
 *  - medicoId: requerido para AUSENTE/COMPLETADA
 *  - pacienteId: opcional para ANULADA (si anula el paciente)
 */
public class CitaPatchRequest {
    public String estado;
    public Long medicoId;
    public Long pacienteId;
}
