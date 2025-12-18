package es.uvigo.dagss.recetas.servicios;

import es.uvigo.dagss.recetas.entidades.Cita;
import es.uvigo.dagss.recetas.entidades.EstadoCita;
import es.uvigo.dagss.recetas.entidades.Medico;
import es.uvigo.dagss.recetas.entidades.Paciente;
import es.uvigo.dagss.recetas.repositorios.CitaDAO;
import es.uvigo.dagss.recetas.repositorios.MedicoDAO;
import es.uvigo.dagss.recetas.repositorios.PacienteDAO;
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

    private final CitaDAO citaDAO;
    private final MedicoDAO medicoDAO;
    private final PacienteDAO pacienteDAO;

    public CitaService(CitaDAO citaDAO,
                       MedicoDAO medicoDAO,
                       PacienteDAO pacienteDAO) {
        this.citaDAO = citaDAO;
        this.medicoDAO = medicoDAO;
        this.pacienteDAO = pacienteDAO;
    }

    /** HU-A7: listado por día + filtros opcionales */
    @Transactional(readOnly = true)
    public List<Cita> listarPorDia(LocalDate fecha, Long medicoId, Long pacienteId) {
        return citaDAO.buscarPorFechaConFiltros(fecha, medicoId, pacienteId);
    }

    /** HU-A7: anular cita (admin) */
    @Transactional
    public void anularComoAdmin(Long citaId) {
        Cita c = citaDAO.findById(citaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Cita no encontrada: " + citaId));
        c.setEstado(EstadoCita.ANULADA);
        citaDAO.save(c);
    }

    /** HU-M2: agenda de hoy */
    @Transactional(readOnly = true)
    public List<Cita> agendaMedico(Long medicoId, LocalDate fecha) {
        return citaDAO.findByMedicoIdAndFechaOrderByHoraInicio(medicoId, fecha);
    }

    /** HU-M2: marcar ausente, tiene que ser PLANIFICADA) */
    @Transactional
    public void marcarAusente(Long citaId, Long medicoId) {
        Cita c = citaDAO.findById(citaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Cita no encontrada: " + citaId));

        if (!c.getMedico().getId().equals(medicoId)) {
            throw new OperacionNoPermitidaException("La cita no pertenece al médico");
        }
        if (c.getEstado() != EstadoCita.PLANIFICADA) {
            throw new OperacionNoPermitidaException("Solo se puede marcar AUSENTE si la cita está PLANIFICADA");
        }

        c.setEstado(EstadoCita.AUSENTE);
        citaDAO.save(c);
    }

    /** HU-M3: marcar completada */
    @Transactional
    public void marcarCompletada(Long citaId, Long medicoId) {
        Cita c = citaDAO.findById(citaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Cita no encontrada: " + citaId));

        if (!c.getMedico().getId().equals(medicoId)) {
            throw new OperacionNoPermitidaException("La cita no pertenece al médico");
        }
        c.setEstado(EstadoCita.COMPLETADA);
        citaDAO.save(c);
    }

    /** HU-P2: citas futuras planificadas del paciente  */
    @Transactional(readOnly = true)
    public List<Cita> citasFuturasPlanificadas(Long pacienteId, LocalDate hoy, LocalTime ahora) {
        return citaDAO.findFuturasPlanificadasDePaciente(pacienteId, hoy, ahora);
    }

    /** HU-P2: anular cita (paciente) */
    @Transactional
    public void anularComoPaciente(Long citaId, Long pacienteId) {
        Cita c = citaDAO.findById(citaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Cita no encontrada: " + citaId));

        if (!c.getPaciente().getId().equals(pacienteId)) {
            throw new OperacionNoPermitidaException("La cita no pertenece al paciente");
        }
        if (c.getEstado() != EstadoCita.PLANIFICADA) {
            throw new OperacionNoPermitidaException("Solo se pueden anular citas PLANIFICADAS");
        }

        c.setEstado(EstadoCita.ANULADA);
        citaDAO.save(c);
    }

    /** HU-P3: huecos disponibles, 15 min, para el médico en un dia concreto */
    @Transactional(readOnly = true)
    public List<LocalTime> huecosDisponibles(Long medicoId, LocalDate fecha) {
        LocalTime inicio = LocalTime.of(8, 30);
        LocalTime fin = LocalTime.of(15, 30);

        List<Cita> existentes = citaDAO.findByMedicoIdAndFechaAndEstadoOrderByHoraInicio(
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

    /** HU-P3: crear cita del paciente con su médico asignado  */
    @Transactional
    public Cita crearCitaPaciente(Long pacienteId, LocalDate fecha, LocalTime horaInicio) {
        if (fecha == null || horaInicio == null) throw new ValidacionException("Fecha y hora son obligatorias");

        Paciente p = pacienteDAO.findById(pacienteId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Paciente no encontrado: " + pacienteId));

        Medico m = p.getMedicoAsignado();
        if (m == null) throw new OperacionNoPermitidaException("El paciente no tiene médico asignado");

        // comprobar rango horario
        LocalTime inicio = LocalTime.of(8, 30);
        LocalTime fin = LocalTime.of(15, 30);
        if (horaInicio.isBefore(inicio) || !horaInicio.isBefore(fin) || (horaInicio.getMinute() % 15 != 0)) {
            throw new ValidacionException("Hora fuera de rango (8:30-15:30) o no alineada a 15 min");
        }

        // comprobar que el hueco está libre
        boolean ocupado = citaDAO
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

        return citaDAO.save(c);
    }
}
