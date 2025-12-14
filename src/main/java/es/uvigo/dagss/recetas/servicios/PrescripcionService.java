package es.uvigo.dagss.recetas.servicios;

import es.uvigo.dagss.recetas.entidades.EstadoReceta;
import es.uvigo.dagss.recetas.entidades.Medico;
import es.uvigo.dagss.recetas.entidades.Medicamento;
import es.uvigo.dagss.recetas.entidades.Paciente;
import es.uvigo.dagss.recetas.entidades.Prescripcion;
import es.uvigo.dagss.recetas.entidades.Receta;
import es.uvigo.dagss.recetas.repositorios.MedicoRepository;
import es.uvigo.dagss.recetas.repositorios.MedicamentoRepository;
import es.uvigo.dagss.recetas.repositorios.PacienteRepository;
import es.uvigo.dagss.recetas.repositorios.PrescripcionRepository;
import es.uvigo.dagss.recetas.servicios.excepciones.OperacionNoPermitidaException;
import es.uvigo.dagss.recetas.servicios.excepciones.RecursoNoEncontradoException;
import es.uvigo.dagss.recetas.servicios.excepciones.ValidacionException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PrescripcionService {

    private final PrescripcionRepository prescripcionRepository;
    private final MedicamentoRepository medicamentoRepository;
    private final PacienteRepository pacienteRepository;
    private final MedicoRepository medicoRepository;

    public PrescripcionService(PrescripcionRepository prescripcionRepository,
                               MedicamentoRepository medicamentoRepository,
                               PacienteRepository pacienteRepository,
                               MedicoRepository medicoRepository) {
        this.prescripcionRepository = prescripcionRepository;
        this.medicamentoRepository = medicamentoRepository;
        this.pacienteRepository = pacienteRepository;
        this.medicoRepository = medicoRepository;
    }

    /** HU-M3: prescripciones en vigor del paciente (fechaFin >= hoy y activa=true) */
    @Transactional(readOnly = true)
    public List<Prescripcion> prescripcionesEnVigor(Long pacienteId, LocalDate hoy) {
        return prescripcionRepository.findEnVigorDePaciente(pacienteId, hoy);
    }

    /** HU-M4/HU-M5: crear prescripción + generar plan de recetas */
    @Transactional
    public Prescripcion crearPrescripcion(Long medicoId,
                                          Long pacienteId,
                                          Long medicamentoId,
                                          Double dosisDiaria,
                                          String indicaciones,
                                          LocalDate fechaFin) {

        if (dosisDiaria == null || dosisDiaria <= 0) throw new ValidacionException("dosisDiaria debe ser > 0");
        if (fechaFin == null) throw new ValidacionException("fechaFin obligatoria");

        LocalDate hoy = LocalDate.now();
        if (fechaFin.isBefore(hoy)) throw new ValidacionException("fechaFin no puede ser anterior a hoy");

        Medico medico = medicoRepository.findById(medicoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Médico no encontrado: " + medicoId));

        Paciente paciente = pacienteRepository.findById(pacienteId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Paciente no encontrado: " + pacienteId));

        Medicamento medicamento = medicamentoRepository.findById(medicamentoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Medicamento no encontrado: " + medicamentoId));

        Prescripcion p = new Prescripcion();
        p.setMedico(medico);
        p.setPaciente(paciente);
        p.setMedicamento(medicamento);
        p.setDosisDiaria(dosisDiaria);
        p.setIndicaciones(indicaciones);
        p.setFechaInicio(hoy);
        p.setFechaFin(fechaFin);
        p.setActiva(true);

        // plan de recetas (1 caja por receta)
        List<Receta> plan = generarPlanRecetas(p);
        p.getRecetas().addAll(plan);

        return prescripcionRepository.save(p);
    }

    /** HU-M3: anular prescripción (activa=false) + anular todas sus recetas */
    @Transactional
    public void anularPrescripcion(Long prescripcionId, Long medicoId) {
        Prescripcion p = prescripcionRepository.findById(prescripcionId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Prescripción no encontrada: " + prescripcionId));

        if (!p.getMedico().getId().equals(medicoId)) {
            throw new OperacionNoPermitidaException("La prescripción no pertenece al médico");
        }

        p.setActiva(false);

        for (Receta r : p.getRecetas()) {
            r.setEstado(EstadoReceta.ANULADA);
            r.setFarmacia(null);
        }

        prescripcionRepository.save(p);
    }

    /**
     * HU-M5: generación semanal de recetas con margen de 1 semana antes (excepto 1ª) y 1 semana después.
     * - 1 caja por receta.
     * - cajas necesarias = ceil((diasTotales * dosisDiaria) / numeroDosis)
     */
    private List<Receta> generarPlanRecetas(Prescripcion p) {
        Medicamento med = p.getMedicamento();
        if (med.getNumeroDosis() == null || med.getNumeroDosis() <= 0) {
            throw new ValidacionException("El medicamento debe tener numeroDosis > 0");
        }

        LocalDate inicio = p.getFechaInicio();
        LocalDate fin = p.getFechaFin();

        long dias = java.time.temporal.ChronoUnit.DAYS.between(inicio, fin) + 1; // inclusivo
        BigDecimal bdDias = new BigDecimal(dias);
        BigDecimal bdDosis = BigDecimal.valueOf(p.getDosisDiaria());
        BigDecimal bdDosisEnvase = BigDecimal.valueOf(med.getNumeroDosis());

        // total unidades necesarias / unidades por caja => cajas
        BigDecimal totalUnidades = bdDias.multiply(bdDosis);
        int cajas = totalUnidades.divide(bdDosisEnvase, 0, RoundingMode.CEILING).intValueExact();
        if (cajas < 1) cajas = 1;

        // días que dura una caja (puede no ser entero)
        BigDecimal diasPorCaja = bdDosisEnvase.divide(bdDosis, 8, RoundingMode.HALF_UP);

        List<Receta> recetas = new ArrayList<>();

        for (int i = 0; i < cajas; i++) {
            BigDecimal offset = diasPorCaja.multiply(BigDecimal.valueOf(i));
            long offsetDias = offset.setScale(0, RoundingMode.FLOOR).longValue(); // recoger un poco antes, mejor que tarde
            LocalDate fechaExacta = inicio.plusDays(offsetDias);

            LocalDate valIni = (i == 0) ? fechaExacta : fechaExacta.minusWeeks(1);
            LocalDate valFin = fechaExacta.plusWeeks(1);

            if (valIni.isBefore(inicio)) valIni = inicio;

            Receta r = new Receta();
            r.setPrescripcion(p);
            r.setFechaValidezInicial(valIni);
            r.setFechaValidezFinal(valFin);
            r.setNumeroUnidades(1);
            r.setEstado(EstadoReceta.PLANIFICADA);
            r.setFarmacia(null);

            recetas.add(r);
        }

        return recetas;
    }
}
