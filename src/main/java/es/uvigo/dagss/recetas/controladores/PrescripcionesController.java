package es.uvigo.dagss.recetas.controladores;

import es.uvigo.dagss.recetas.controladores.dto.PrescripcionCreateRequest;
import es.uvigo.dagss.recetas.entidades.Prescripcion;
import es.uvigo.dagss.recetas.servicios.PrescripcionService;
import es.uvigo.dagss.recetas.servicios.excepciones.ValidacionException;
import java.net.URI;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import es.uvigo.dagss.recetas.controladores.dto.PrescripcionPatchRequest;

@RestController
@RequestMapping(path = "/api/prescripciones", produces = MediaType.APPLICATION_JSON_VALUE)
@CrossOrigin(origins = "*")
public class PrescripcionesController {

    private final PrescripcionService prescripcionService;

    public PrescripcionesController(PrescripcionService prescripcionService) {
        this.prescripcionService = prescripcionService;
    }

    /** HU-M3..M5: crea prescripción + genera plan de recetas */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Prescripcion> crear(@RequestBody PrescripcionCreateRequest req) {
        if (req == null)
            throw new ValidacionException("Body obligatorio");
        // Mapeo del DTO al servicio (evitamos que el service dependa del paquete de
        // controladores)
        if (req.medicoId == null)
            throw new ValidacionException("medicoId obligatorio");
        if (req.pacienteId == null)
            throw new ValidacionException("pacienteId obligatorio");
        if (req.medicamentoId == null)
            throw new ValidacionException("medicamentoId obligatorio");
        if (req.dosisDiaria == null)
            throw new ValidacionException("dosisDiaria obligatoria");
        if (req.fechaFin == null || req.fechaFin.isBlank())
            throw new ValidacionException("fechaFin obligatoria");
        
       java.time.LocalDate fechaFin;

        try {
            fechaFin = java.time.LocalDate.parse(req.fechaFin); // yyyy-MM-dd
        } catch (java.time.format.DateTimeParseException e) {
            throw new ValidacionException("fechaFin debe tener formato yyyy-MM-dd");
        }
        Prescripcion p = prescripcionService.crearPrescripcion(
                req.medicoId,
                req.pacienteId,
                req.medicamentoId,
                req.dosisDiaria,
                req.indicaciones,
                fechaFin);

        URI uri = ServletUriComponentsBuilder.fromCurrentRequestUri()
                .path("/{id}")
                .buildAndExpand(p.getId())
                .toUri();

        return ResponseEntity.created(uri).body(p);
    }

    /** HU-M5: anular prescripción (activa=false) + recetas ANULADA */
    @PatchMapping(path = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> patch(@PathVariable Long id, @RequestBody PrescripcionPatchRequest req) {
        if (req == null || req.activa == null)
            throw new ValidacionException("activa obligatorio");
        if (Boolean.FALSE.equals(req.activa)) {
            // Si el DTO trae medicoId lo pasamos para validar que la prescripción pertenece
            // al médico
            prescripcionService.anularPrescripcion(id, req.medicoId);
        } else {
            throw new ValidacionException("Solo se soporta activa=false");
        }
        return ResponseEntity.noContent().build();
    }
}