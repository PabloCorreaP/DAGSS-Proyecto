package es.uvigo.dagss.recetas.controladores;

import es.uvigo.dagss.recetas.controladores.dto.ServirRecetaRequest;
import es.uvigo.dagss.recetas.entidades.Receta;
import es.uvigo.dagss.recetas.servicios.RecetaService;
import es.uvigo.dagss.recetas.servicios.excepciones.ValidacionException;
import java.time.LocalDate;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/api/recetas", produces = MediaType.APPLICATION_JSON_VALUE)
@CrossOrigin(origins = "*")
public class RecetasController {

    private final RecetaService recetaService;

    public RecetasController(RecetaService recetaService) {
        this.recetaService = recetaService;
    }

    /**
     * HU-P4: mis recetas pendientes (pacienteId)
     * HU-F2: consulta por tarjetaSanitaria
     */
    @GetMapping
    public List<Receta> listar(
            @RequestParam(value = "pacienteId", required = false) Long pacienteId,
            @RequestParam(value = "tarjetaSanitaria", required = false) String tarjetaSanitaria
    ) {
        LocalDate hoy = LocalDate.now();
        if (pacienteId != null) {
            return recetaService.recetasPendientesPaciente(pacienteId, hoy);
        }
        if (tarjetaSanitaria != null && !tarjetaSanitaria.isBlank()) {
            return recetaService.recetasEnVigorPorTarjetaSanitaria(tarjetaSanitaria.trim(), hoy);
        }
        throw new ValidacionException("Debe indicar 'pacienteId' o 'tarjetaSanitaria'");
    }

    /** HU-F3: servir receta */
    @PatchMapping(path = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> servir(@PathVariable Long id, @RequestBody ServirRecetaRequest req) {
        if (req == null || req.farmaciaId == null) throw new ValidacionException("farmaciaId obligatorio");
        recetaService.servirReceta(id, req.farmaciaId, LocalDate.now());
        return ResponseEntity.noContent().build();
    }
}
