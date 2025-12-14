package es.uvigo.dagss.recetas.servicios;

import es.uvigo.dagss.recetas.entidades.Cita;
import es.uvigo.dagss.recetas.entidades.EstadoCita;
import es.uvigo.dagss.recetas.entidades.Medico;
import es.uvigo.dagss.recetas.entidades.Paciente;
import es.uvigo.dagss.recetas.repositorios.CitaRepository;
import es.uvigo.dagss.recetas.repositorios.MedicoRepository;
import es.uvigo.dagss.recetas.repositorios.PacienteRepository;
import es.uvigo.dagss.recetas.servicios.excepciones.OperacionNoPermitidaException;
import es.uvigo.dagss.recetas.servicios.excepciones.RecursoNoEncontradoException;
import es.uvigo.dagss.recetas.servicios.excepciones.ValidacionException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CitaService {

    private final CitaRepository citaRepository;
    private final MedicoRepository medicoRepository;
    private final PacienteRepository pacienteRepository;

    public CitaService(CitaRepository citaRepository,
                       MedicoRepository medicoRepository,
                       PacienteRepository pacienteRepository) {
        this.citaRepository = citaRepository;
        this.medicoRepository = medicoRepository;
        this.pacienteRepository = pacienteRepository;
    }

    /** HU-A7: listado por día + filtros opcionales */
    @Transactional(readOnly = true)
    public List<Cita> listarPorDia(LocalDate fecha, Long medicoId, Long pacienteId) {
        return citaRepository.buscarPorFechaConFiltros(fecha, medicoId, pacienteId);
    }

    /** HU-A7: anular cita (admin) */
    @Transactional
    public void anularComoAdmin(Long citaId) {
        Cita c = citaRepository.findById(citaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Cita no encontrada: " + citaId));
        c.setEstado(EstadoCita.ANULADA);
        citaRepository.save(c);
    }

    /** HU-M2: agenda de hoy */
    @Transactional(readOnly = true)
    public List<Cita> agendaMedico(Long medicoId, LocalDate fecha) {
        return citaRepository.findByMedicoIdAndFechaOrderByHoraInicio(medicoId, fecha);
    }

    /** HU-M2: marcar ausente (solo si PLANIFICADA) */
    @Transactional
    public void marcarAusente(Long citaId, Long medicoId) {
        Cita c = citaRepository.findById(citaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Cita no encontrada: " + citaId));

        if (!c.getMedico().getId().equals(medicoId)) {
            throw new OperacionNoPermitidaException("La cita no pertenece al médico");
        }
        if (c.getEstado() != EstadoCita.PLANIFICADA) {
            throw new OperacionNoPermitidaException("Solo se puede marcar AUSENTE si la cita está PLANIFICADA");
        }

        c.setEstado(EstadoCita.AUSENTE);
        citaRepository.save(c);
    }

    /** HU-M3: marcar completada */
    @Transactional
    public void marcarCompletada(Long citaId, Long medicoId) {
        Cita c = citaRepository.findById(citaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Cita no encontrada: " + citaId));

        if (!c.getMedico().getId().equals(medicoId)) {
            throw new OperacionNoPermitidaException("La cita no pertenece al médico");
        }
        c.setEstado(EstadoCita.COMPLETADA);
        citaRepository.save(c);
    }

    /** HU-P2: citas futuras planificadas del paciente (a partir de ahora) */
    @Transactional(readOnly = true)
    public List<Cita> citasFuturasPlanificadas(Long pacienteId, LocalDate hoy, LocalTime ahora) {
        return citaRepository.findFuturasPlanificadasDePaciente(pacienteId, hoy, ahora);
    }

    /** HU-P2: anular cita (paciente) */
    @Transactional
    public void anularComoPaciente(Long citaId, Long pacienteId) {
        Cita c = citaRepository.findById(citaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Cita no encontrada: " + citaId));

        if (!c.getPaciente().getId().equals(pacienteId)) {
            throw new OperacionNoPermitidaException("La cita no pertenece al paciente");
        }
        if (c.getEstado() != EstadoCita.PLANIFICADA) {
            throw new OperacionNoPermitidaException("Solo se pueden anular citas PLANIFICADAS");
        }

        c.setEstado(EstadoCita.ANULADA);
        citaRepository.save(c);
    }

    /** HU-P3: huecos disponibles (15 min) para el médico en un día: 8:30-15:30 */
    @Transactional(readOnly = true)
    public List<LocalTime> huecosDisponibles(Long medicoId, LocalDate fecha) {
        LocalTime inicio = LocalTime.of(8, 30);
        LocalTime fin = LocalTime.of(15, 30);

        List<Cita> existentes = citaRepository.findByMedicoIdAndFechaAndEstadoOrderByHoraInicio(
                medicoId, fecha, EstadoCita.PLANIFICADA);

        Set<LocalTime> ocupadas = new HashSet<>();
        for (Cita c : existentes) {
            ocupadas.add(c.getHoraInicio());
        }

        java.util.ArrayList<LocalTime> libres = new java.util.ArrayList<>();
        LocalTime t = inicio;
        while (t.isBefore(fin)) {
            if (!ocupadas.contains(t)) {
                libres.add(t);
            }
            t = t.plusMinutes(15);
        }
        return libres;
    }

    /** HU-P3: crear cita del paciente con su médico asignado (verifica hueco libre) */
    @Transactional
    public Cita crearCitaPaciente(Long pacienteId, LocalDate fecha, LocalTime horaInicio) {
        if (fecha == null || horaInicio == null) throw new ValidacionException("Fecha y hora son obligatorias");

        Paciente p = pacienteRepository.findById(pacienteId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Paciente no encontrado: " + pacienteId));

        Medico m = p.getMedicoAsignado();
        if (m == null) throw new OperacionNoPermitidaException("El paciente no tiene médico asignado");

        // comprobar rango horario
        LocalTime inicio = LocalTime.of(8, 30);
        LocalTime fin = LocalTime.of(15, 30);
        if (horaInicio.isBefore(inicio) || !horaInicio.isBefore(fin) || (horaInicio.getMinute() % 15 != 0)) {
            throw new ValidacionException("Hora fuera de rango (8:30-15:30) o no alineada a 15 min");
        }

        // comprobar que el hueco está libre (solo conflictos con PLANIFICADA)
        boolean ocupado = citaRepository
                .findByMedicoIdAndFechaAndHoraInicioAndEstado(m.getId(), fecha, horaInicio, EstadoCita.PLANIFICADA)
                .isPresent();

        if (ocupado) {
            throw new OperacionNoPermitidaException("Ese hueco ya está ocupado");
        }

        Cita c = new Cita();
        c.setPaciente(p);
        c.setMedico(m);
        c.setFecha(fecha);
        c.setHoraInicio(horaInicio);
        c.setDuracion(15);
        c.setEstado(EstadoCita.PLANIFICADA);

        return citaRepository.save(c);
    }
}
