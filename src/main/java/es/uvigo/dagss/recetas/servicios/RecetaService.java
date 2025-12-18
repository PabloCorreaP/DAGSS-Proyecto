package es.uvigo.dagss.recetas.servicios;

import es.uvigo.dagss.recetas.entidades.EstadoReceta;
import es.uvigo.dagss.recetas.entidades.Farmacia;
import es.uvigo.dagss.recetas.entidades.Receta;
import es.uvigo.dagss.recetas.repositorios.FarmaciaDAO;
import es.uvigo.dagss.recetas.repositorios.RecetaDAO;
import es.uvigo.dagss.recetas.servicios.excepciones.OperacionNoPermitidaException;
import es.uvigo.dagss.recetas.servicios.excepciones.RecursoNoEncontradoException;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RecetaService {

    private final RecetaDAO recetaDAO;
    private final FarmaciaDAO farmaciaDAO;

    public RecetaService(RecetaDAO recetaDAO,
                         FarmaciaDAO farmaciaDAO) {
        this.recetaDAO = recetaDAO;
        this.farmaciaDAO = farmaciaDAO;
    }

    /** HU-P4: recetas pendientes de recoger del paciente */
    @Transactional(readOnly = true)
    public List<Receta> recetasPendientesPaciente(Long pacienteId, LocalDate hoy) {
        return recetaDAO.findPendientesDePaciente(pacienteId, hoy);
    }

    /** HU-F2: recetas en vigor  */
    @Transactional(readOnly = true)
    public List<Receta> recetasEnVigorPorTarjetaSanitaria(String numeroTarjetaSanitaria, LocalDate hoy) {
        return recetaDAO.findPlanificadasEnVigorPorTarjetaSanitaria(numeroTarjetaSanitaria, hoy);
    }

    /** HU-F3: servir receta*/
    @Transactional
    public Receta servirReceta(Long recetaId, Long farmaciaId, LocalDate hoy) {
        Receta r = recetaDAO.findById(recetaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Receta no encontrada: " + recetaId));

        if (r.getEstado() != EstadoReceta.PLANIFICADA) {
            throw new OperacionNoPermitidaException("Solo se pueden servir recetas PLANIFICADAS");
        }

        if (hoy.isBefore(r.getFechaValidezInicial()) || hoy.isAfter(r.getFechaValidezFinal())) {
            throw new OperacionNoPermitidaException("No se puede servir la receta fuera de su periodo de validez");
        }

        Farmacia f = farmaciaDAO.findById(farmaciaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Farmacia no encontrada: " + farmaciaId));

        r.setEstado(EstadoReceta.SERVIDA);
        r.setFarmacia(f);

        return recetaDAO.save(r);
    }

    public boolean esServible(Receta r, LocalDate hoy) {
        return r.getEstado() == EstadoReceta.PLANIFICADA
                && !hoy.isBefore(r.getFechaValidezInicial())
                && !hoy.isAfter(r.getFechaValidezFinal());
    }
}
