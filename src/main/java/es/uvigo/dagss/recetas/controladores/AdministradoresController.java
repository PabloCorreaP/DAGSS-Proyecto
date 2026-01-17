package es.uvigo.dagss.recetas.controladores;

import es.uvigo.dagss.recetas.entidades.Administrador;
import es.uvigo.dagss.recetas.servicios.AdministradorService;
import java.net.URI;
import java.util.List;
import java.util.Locale;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping(path = "/api/administradores", produces = MediaType.APPLICATION_JSON_VALUE)
@CrossOrigin(origins = "*")
public class AdministradoresController {

    private final AdministradorService administradorService;

    public AdministradoresController(AdministradorService administradorService) {
        this.administradorService = administradorService;
    }

    /** HU-A2: listado (opcionalmente filtrado por q en login/nombre/email). */
    @GetMapping
    public List<Administrador> listar(@RequestParam(value = "q", required = false) String q) {
        List<Administrador> base = administradorService.listarActivos();
        if (q == null || q.isBlank()) return base;
        String needle = q.toLowerCase(Locale.ROOT);
        return base.stream().filter(a ->
                (a.getLogin() != null && a.getLogin().toLowerCase(Locale.ROOT).contains(needle))
                        || (a.getNombre() != null && a.getNombre().toLowerCase(Locale.ROOT).contains(needle))
                        || (a.getEmail() != null && a.getEmail().toLowerCase(Locale.ROOT).contains(needle))
        ).toList();
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Administrador> crear(@RequestBody Administrador a) {
        Administrador creado = administradorService.crear(a.getLogin(), a.getPassword(), a.getNombre(), a.getEmail());
        URI uri = ServletUriComponentsBuilder.fromCurrentRequestUri()
                .path("/{id}")
                .buildAndExpand(creado.getId())
                .toUri();
        return ResponseEntity.created(uri).body(creado);
    }

    @PutMapping(path = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Administrador actualizar(@PathVariable Long id, @RequestBody Administrador cambios) {
        return administradorService.actualizar(id, cambios.getNombre(), cambios.getEmail(), cambios.getActivo());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> baja(@PathVariable Long id) {
        administradorService.baja(id);
        return ResponseEntity.noContent().build();
    }
}
