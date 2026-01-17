package es.uvigo.dagss.recetas.controladores;

import es.uvigo.dagss.recetas.controladores.dto.AtencionCitaResponse;
import es.uvigo.dagss.recetas.controladores.dto.CitaCreateRequest;
import es.uvigo.dagss.recetas.controladores.dto.CitaPatchRequest;
import es.uvigo.dagss.recetas.entidades.Cita;
import es.uvigo.dagss.recetas.entidades.Paciente;
import es.uvigo.dagss.recetas.entidades.Prescripcion;
import es.uvigo.dagss.recetas.servicios.CitaService;
import es.uvigo.dagss.recetas.servicios.PrescripcionService;
import es.uvigo.dagss.recetas.servicios.excepciones.ValidacionException;
import java.net.URI;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping(path = "/api/citas", produces = MediaType.APPLICATION_JSON_VALUE)
@CrossOrigin(origins = "*")
public class CitasController {

    private final CitaService citaService;
    private final PrescripcionService prescripcionService;

    public CitasController(CitaService citaService, PrescripcionService prescripcionService) {
        this.citaService = citaService;
        this.prescripcionService = prescripcionService;
    }

    /**
     * HU-A7 / HU-M2 / HU-P2: listado/filtrado por fecha (+ opcional medicoId/pacienteId).
     */
    @GetMapping
    public List<Cita> listar(
            @RequestParam(value = "fecha", required = false) String fecha,
            @RequestParam(value = "medicoId", required = false) Long medicoId,
            @RequestParam(value = "pacienteId", required = false) Long pacienteId
    ) {
        // Caso "mis citas" (HU-P2): si no se da fecha pero sí pacienteId, devolvemos futuras planificadas.
        if ((fecha == null || fecha.isBlank()) && pacienteId != null) {
            return citaService.citasFuturasPlanificadas(pacienteId, LocalDate.now(), LocalTime.now());
        }
        if (fecha == null || fecha.isBlank()) {
            throw new ValidacionException("Debe indicar 'fecha' (o 'pacienteId' para ver futuras)");
        }
        LocalDate f = LocalDate.parse(fecha);
        return citaService.listarPorDia(f, medicoId, pacienteId);
    }

    /** HU-P3: huecos libres del médico para una fecha */
    @GetMapping("/huecos")
    public List<LocalTime> huecos(
            @RequestParam("fecha") String fecha,
            @RequestParam("medicoId") Long medicoId
    ) {
        LocalDate f = LocalDate.parse(fecha);
        return citaService.huecosDisponibles(medicoId, f);
    }

    /** HU-P3: crear cita (se crea para el médico asignado al paciente). */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Cita> crear(@RequestBody CitaCreateRequest req) {
        if (req == null) throw new ValidacionException("Body obligatorio");
        LocalDate f = LocalDate.parse(req.fecha);
        LocalTime h = LocalTime.parse(req.horaInicio);
        Cita creada = citaService.crearCitaPaciente(req.pacienteId, f, h);

        URI uri = ServletUriComponentsBuilder.fromCurrentRequestUri()
                .path("/{id}")
                .buildAndExpand(creada.getId())
                .toUri();

        return ResponseEntity.created(uri).body(creada);
    }

    /** HU-A7 / HU-M2 / HU-M3 / HU-P2: cambio de estado (ANULADA / AUSENTE / COMPLETADA...). */
    @PatchMapping(path = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> cambiarEstado(@PathVariable Long id, @RequestBody CitaPatchRequest req) {
        if (req == null || req.estado == null || req.estado.isBlank()) {
            throw new ValidacionException("estado obligatorio");
        }
        String e = req.estado.trim().toUpperCase();

        switch (e) {
            case "ANULADA" -> {
                if (req.pacienteId != null) citaService.anularComoPaciente(id, req.pacienteId);
                else citaService.anularComoAdmin(id);
            }
            case "AUSENTE" -> {
                if (req.medicoId == null) throw new ValidacionException("medicoId obligatorio para AUSENTE");
                citaService.marcarAusente(id, req.medicoId);
            }
            case "COMPLETADA" -> {
                if (req.medicoId == null) throw new ValidacionException("medicoId obligatorio para COMPLETADA");
                citaService.marcarCompletada(id, req.medicoId);
            }
            default -> throw new ValidacionException("estado no soportado: " + req.estado);
        }

        return ResponseEntity.noContent().build();
    }

    /** HU-M3: atención de cita (paciente + prescripciones vigentes). */
    @GetMapping("/{id}/atencion")
    public AtencionCitaResponse atencion(@PathVariable Long id) {
        Cita c = citaService.getOrThrow(id);
        Paciente p = c.getPaciente();
        List<Prescripcion> vigentes = prescripcionService.prescripcionesEnVigor(p.getId(), LocalDate.now());
        return new AtencionCitaResponse(c, p, vigentes);
    }
}
