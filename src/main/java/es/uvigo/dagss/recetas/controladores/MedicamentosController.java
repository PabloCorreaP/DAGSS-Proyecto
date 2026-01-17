package es.uvigo.dagss.recetas.controladores;

import es.uvigo.dagss.recetas.entidades.Medicamento;
import es.uvigo.dagss.recetas.servicios.MedicamentoService;
import java.net.URI;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping(path = "/api/medicamentos", produces = MediaType.APPLICATION_JSON_VALUE)
@CrossOrigin(origins = "*")
public class MedicamentosController {

    private final MedicamentoService medicamentoService;

    public MedicamentosController(MedicamentoService medicamentoService) {
        this.medicamentoService = medicamentoService;
    }

    /** HU-A8 / HU-M4: listado + filtros por nombre/principioActivo/fabricante/familia */
    @GetMapping
    public List<Medicamento> listar(
            @RequestParam(value = "nombre", required = false) String nombre,
            @RequestParam(value = "principioActivo", required = false) String principioActivo,
            @RequestParam(value = "fabricante", required = false) String fabricante,
            @RequestParam(value = "familia", required = false) String familia
    ) {
        return medicamentoService.buscar(nombre, principioActivo, fabricante, familia);
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Medicamento> crear(@RequestBody Medicamento m) {
        Medicamento creado = medicamentoService.crear(m);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequestUri()
                .path("/{id}")
                .buildAndExpand(creado.getId())
                .toUri();
        return ResponseEntity.created(uri).body(creado);
    }

    @PutMapping(path = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Medicamento actualizar(@PathVariable Long id, @RequestBody Medicamento cambios) {
        return medicamentoService.actualizar(id, cambios);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> baja(@PathVariable Long id) {
        medicamentoService.baja(id);
        return ResponseEntity.noContent().build();
    }
}
