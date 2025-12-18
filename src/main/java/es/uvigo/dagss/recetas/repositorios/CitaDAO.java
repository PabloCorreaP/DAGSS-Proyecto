package es.uvigo.dagss.recetas.repositorios;

import es.uvigo.dagss.recetas.entidades.Cita;
import es.uvigo.dagss.recetas.entidades.EstadoCita;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CitaDAO extends JpaRepository<Cita, Long> {

    @Query("""
           select c
           from Cita c
           where c.fecha = :fecha
             and (:medicoId is null or c.medico.id = :medicoId)
             and (:pacienteId is null or c.paciente.id = :pacienteId)
           order by c.horaInicio
           """)
    List<Cita> buscarPorFechaConFiltros(@Param("fecha") LocalDate fecha,
                                        @Param("medicoId") Long medicoId,
                                        @Param("pacienteId") Long pacienteId);

    List<Cita> findByMedicoIdAndFechaOrderByHoraInicio(Long medicoId, LocalDate fecha);

    List<Cita> findByMedicoIdAndFechaAndEstadoOrderByHoraInicio(Long medicoId, LocalDate fecha, EstadoCita estado);

    @Query("""
           select c
           from Cita c
           where c.paciente.id = :pacienteId
             and c.estado = es.uvigo.dagss.recetas.entidades.EstadoCita.PLANIFICADA
             and (c.fecha > :hoy or (c.fecha = :hoy and c.horaInicio >= :ahora))
           order by c.fecha, c.horaInicio
           """)
    List<Cita> findFuturasPlanificadasDePaciente(@Param("pacienteId") Long pacienteId,
                                                 @Param("hoy") LocalDate hoy,
                                                 @Param("ahora") LocalTime ahora);

    Optional<Cita> findByMedicoIdAndFechaAndHoraInicioAndEstado(Long medicoId, LocalDate fecha, LocalTime horaInicio, EstadoCita estado);
}
