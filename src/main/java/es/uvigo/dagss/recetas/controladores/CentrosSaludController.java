package es.uvigo.dagss.recetas.controladores;

import es.uvigo.dagss.recetas.entidades.CentroSalud;
import es.uvigo.dagss.recetas.servicios.CentroSaludService;
import java.net.URI;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import es.uvigo.dagss.recetas.controladores.dto.PrescripcionPatchRequest;

@RestController
@RequestMapping(path = "/api/centros-salud", produces = MediaType.APPLICATION_JSON_VALUE)
@CrossOrigin(origins = "*")
public class CentrosSaludController {

    private final CentroSaludService centroSaludService;

    public CentrosSaludController(CentroSaludService centroSaludService) {
        this.centroSaludService = centroSaludService;
    }

    /** HU-A3: listado + filtros por nombre y/o localidad (LIKE). */
    @GetMapping
    public List<CentroSalud> listar(
            @RequestParam(value = "nombre", required = false) String nombre,
            @RequestParam(value = "localidad", required = false) String localidad
    ) {
        if ((nombre == null || nombre.isBlank()) && (localidad == null || localidad.isBlank())) {
            return centroSaludService.listarActivos();
        }
        // El DAO/servicio original soporta un texto único; elegimos el que venga.
        String texto = (nombre != null && !nombre.isBlank()) ? nombre : localidad;
        return centroSaludService.buscarActivos(texto);
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CentroSalud> crear(@RequestBody CentroSalud cs) {
        CentroSalud creado = centroSaludService.crear(cs);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequestUri()
                .path("/{id}")
                .buildAndExpand(creado.getId())
                .toUri();
        return ResponseEntity.created(uri).body(creado);
    }

    @PutMapping(path = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public CentroSalud actualizar(@PathVariable Long id, @RequestBody CentroSalud cambios) {
        return centroSaludService.actualizar(id, cambios);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> baja(@PathVariable Long id) {
        centroSaludService.baja(id);
        return ResponseEntity.noContent().build();
    }
}
