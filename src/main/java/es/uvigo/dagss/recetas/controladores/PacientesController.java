package es.uvigo.dagss.recetas.controladores;

import es.uvigo.dagss.recetas.entidades.Paciente;
import es.uvigo.dagss.recetas.servicios.PacienteService;
import java.net.URI;
import java.util.List;
import java.util.Map;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping(path = "/api/pacientes", produces = MediaType.APPLICATION_JSON_VALUE)
@CrossOrigin(origins = "*")
public class PacientesController {

    private final PacienteService pacienteService;

    public PacientesController(PacienteService pacienteService) {
        this.pacienteService = pacienteService;
    }

    /** HU-A5: listado + filtros */
    @GetMapping
    public List<Paciente> listar(
            @RequestParam(value = "nombre", required = false) String nombre,
            @RequestParam(value = "localidad", required = false) String localidad,
            @RequestParam(value = "centroId", required = false) Long centroId,
            @RequestParam(value = "medicoId", required = false) Long medicoId
    ) {
        return pacienteService.buscarActivos(nombre, localidad, centroId, medicoId);
    }

    /** HU-A5: alta (password inicial: DNI, lo gestiona el servicio) */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Paciente> crear(
            @RequestBody Paciente p,
            @RequestParam(value = "centroId", required = false) Long centroId,
            @RequestParam(value = "medicoId", required = false) Long medicoId
    ) {
        Long csId = (centroId != null) ? centroId : (p.getCentroSalud() != null ? p.getCentroSalud().getId() : null);
        Long mId = (medicoId != null) ? medicoId : (p.getMedicoAsignado() != null ? p.getMedicoAsignado().getId() : null);

        // Wrapper: tu servicio ya expone crear(Paciente, csId, mId)
        Paciente creado = pacienteService.crear(p, csId, mId);

        URI uri = ServletUriComponentsBuilder.fromCurrentRequestUri()
                .path("/{id}")
                .buildAndExpand(creado.getId())
                .toUri();

        return ResponseEntity.created(uri).body(creado);
    }

    @PutMapping(path = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Paciente actualizar(
            @PathVariable Long id,
            @RequestBody Paciente cambios,
            @RequestParam(value = "centroId", required = false) Long centroId,
            @RequestParam(value = "medicoId", required = false) Long medicoId
    ) {
        Long csId = (centroId != null) ? centroId : (cambios.getCentroSalud() != null ? cambios.getCentroSalud().getId() : null);
        Long mId = (medicoId != null) ? medicoId : (cambios.getMedicoAsignado() != null ? cambios.getMedicoAsignado().getId() : null);
        return pacienteService.actualizarPorAdmin(id, cambios, csId, mId);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> baja(@PathVariable Long id) {
        pacienteService.baja(id);
        return ResponseEntity.noContent().build();
    }

    /** HU-P5: perfil */
    @PutMapping(path = "/{id}/perfil", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Paciente actualizarPerfil(@PathVariable Long id, @RequestBody Paciente cambios) {
        return pacienteService.actualizarPerfil(id, cambios);
    }

    @PutMapping(path = "/{id}/perfil/password", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> cambiarPassword(@PathVariable Long id, @RequestBody Map<String, String> body) {
        pacienteService.cambiarPassword(id, body.get("password"));
        return ResponseEntity.noContent().build();
    }
}
