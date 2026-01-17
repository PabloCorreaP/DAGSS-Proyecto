package es.uvigo.dagss.recetas.controladores;

import es.uvigo.dagss.recetas.entidades.Medico;
import es.uvigo.dagss.recetas.servicios.MedicoService;
import java.net.URI;
import java.util.List;
import java.util.Map;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping(path = "/api/medicos", produces = MediaType.APPLICATION_JSON_VALUE)
@CrossOrigin(origins = "*")
public class MedicosController {

    private final MedicoService medicoService;

    public MedicosController(MedicoService medicoService) {
        this.medicoService = medicoService;
    }

    /** HU-A4: listado + filtros */
    @GetMapping
    public List<Medico> listar(
            @RequestParam(value = "nombre", required = false) String nombre,
            @RequestParam(value = "localidad", required = false) String localidad,
            @RequestParam(value = "centroId", required = false) Long centroId
    ) {
        return medicoService.buscarActivos(nombre, localidad, centroId);
    }

    /** HU-A4: alta (password inicial: numeroColegiado, lo gestiona el servicio). */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Medico> crear(
            @RequestBody Medico m,
            @RequestParam(value = "centroId", required = false) Long centroId
    ) {
        Long csId = (centroId != null) ? centroId : (m.getCentroSalud() != null ? m.getCentroSalud().getId() : null);

        // Wrapper: tu servicio ya expone crear(Medico, csId)
        Medico creado = medicoService.crear(m, csId);

        URI uri = ServletUriComponentsBuilder.fromCurrentRequestUri()
                .path("/{id}")
                .buildAndExpand(creado.getId())
                .toUri();

        return ResponseEntity.created(uri).body(creado);
    }

    @PutMapping(path = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Medico actualizar(
            @PathVariable Long id,
            @RequestBody Medico cambios,
            @RequestParam(value = "centroId", required = false) Long centroId
    ) {
        Long csId = (centroId != null) ? centroId : (cambios.getCentroSalud() != null ? cambios.getCentroSalud().getId() : null);
        return medicoService.actualizarPorAdmin(id, cambios, csId);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> baja(@PathVariable Long id) {
        medicoService.baja(id);
        return ResponseEntity.noContent().build();
    }

    /** HU-M6: perfil */
    @PutMapping(path = "/{id}/perfil", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Medico actualizarPerfil(@PathVariable Long id, @RequestBody Medico cambios) {
        return medicoService.actualizarPerfil(id, cambios);
    }

    @PutMapping(path = "/{id}/perfil/password", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> cambiarPassword(@PathVariable Long id, @RequestBody Map<String, String> body) {
        medicoService.cambiarPassword(id, body.get("password"));
        return ResponseEntity.noContent().build();
    }
}
