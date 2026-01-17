package es.uvigo.dagss.recetas.controladores;

import es.uvigo.dagss.recetas.entidades.Farmacia;
import es.uvigo.dagss.recetas.servicios.FarmaciaService;
import java.net.URI;
import java.util.List;
import java.util.Map;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping(path = "/api/farmacias", produces = MediaType.APPLICATION_JSON_VALUE)
@CrossOrigin(origins = "*")
public class FarmaciasController {

    private final FarmaciaService farmaciaService;

    public FarmaciasController(FarmaciaService farmaciaService) {
        this.farmaciaService = farmaciaService;
    }

    /** HU-A6: listado + filtros por nombreEstablecimiento y/o localidad */
    @GetMapping
    public List<Farmacia> listar(
            @RequestParam(value = "nombreEstablecimiento", required = false) String nombreEstablecimiento,
            @RequestParam(value = "localidad", required = false) String localidad
    ) {
        if ((nombreEstablecimiento == null || nombreEstablecimiento.isBlank())
                && (localidad == null || localidad.isBlank())) {
            return farmaciaService.listarActivas();
        }
        String texto = (nombreEstablecimiento != null && !nombreEstablecimiento.isBlank())
                ? nombreEstablecimiento
                : localidad;
        return farmaciaService.buscarActivas(texto);
    }

    /** HU-A6: alta (password inicial: numeroColegiadoFarmaceutico, lo gestiona el service) */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Farmacia> crear(@RequestBody Farmacia f) {
        Farmacia creada = farmaciaService.crear(
                f.getLogin(),
                f.getNombreEstablecimiento(),
                f.getNombreFarmaceutico(),
                f.getApellidosFarmaceutico(),
                f.getNif(),
                f.getNumeroColegiadoFarmaceutico(),
                f.getDomicilio(),
                f.getLocalidad(),
                f.getCodigoPostal(),
                f.getProvincia(),
                f.getTelefono(),
                f.getEmail()
        );

        URI location = ServletUriComponentsBuilder.fromCurrentRequestUri()
                .path("/{id}")
                .buildAndExpand(creada.getId())
                .toUri();

        return ResponseEntity.created(location).body(creada);
    }

    /** HU-A6: edición por administrador */
    @PutMapping(path = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Farmacia actualizar(@PathVariable Long id, @RequestBody Farmacia cambios) {
        return farmaciaService.actualizarPorAdmin(id, cambios);
    }

    /** HU-A6: baja lógica */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> baja(@PathVariable Long id) {
        farmaciaService.baja(id);
        return ResponseEntity.noContent().build();
    }

    /** HU-F4: perfil */
    @PutMapping(path = "/{id}/perfil", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Farmacia actualizarPerfil(@PathVariable Long id, @RequestBody Farmacia cambios) {
        return farmaciaService.actualizarPerfil(id, cambios);
    }

    /** HU-F4: cambiar solo password */
    @PutMapping(path = "/{id}/perfil/password", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> cambiarPassword(@PathVariable Long id, @RequestBody Map<String, String> body) {
        farmaciaService.cambiarPassword(id, body.get("password"));
        return ResponseEntity.noContent().build();
    }
}
