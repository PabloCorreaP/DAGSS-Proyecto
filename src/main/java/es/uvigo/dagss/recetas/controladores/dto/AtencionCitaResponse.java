package es.uvigo.dagss.recetas.controladores.dto;

import es.uvigo.dagss.recetas.entidades.Cita;
import es.uvigo.dagss.recetas.entidades.Paciente;
import es.uvigo.dagss.recetas.entidades.Prescripcion;
import java.util.List;

public class AtencionCitaResponse {
    public Cita cita;
    public Paciente paciente;
    public List<Prescripcion> prescripcionesVigentes;

    public AtencionCitaResponse(Cita cita, Paciente paciente, List<Prescripcion> prescripcionesVigentes) {
        this.cita = cita;
        this.paciente = paciente;
        this.prescripcionesVigentes = prescripcionesVigentes;
    }
}
