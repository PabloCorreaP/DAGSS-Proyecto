package es.uvigo.dagss.recetas.controladores.dto;

public class PrescripcionPatchRequest {
     public Boolean activa;   // esperamos {"activa": false}
    public Long medicoId;    // opcional para validar pertenencia
}
