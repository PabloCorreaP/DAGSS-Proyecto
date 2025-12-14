package es.uvigo.dagss.recetas.repositorios;

import es.uvigo.dagss.recetas.entidades.EstadoReceta;
import es.uvigo.dagss.recetas.entidades.Receta;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RecetaRepository extends JpaRepository<Receta, Long> {

  @Query("""
      select r
      from Receta r
        join fetch r.prescripcion p
        join fetch p.medicamento m
        join fetch p.medico md
      where p.paciente.id = :pacienteId
        and r.estado = es.uvigo.dagss.recetas.entidades.EstadoReceta.PLANIFICADA
        and r.fechaValidezFinal >= :hoy
      order by r.fechaValidezInicial asc
      """)
  List<Receta> findPendientesDePaciente(@Param("pacienteId") Long pacienteId,
      @Param("hoy") LocalDate hoy);

  @Query("""
      select r
      from Receta r
        join fetch r.prescripcion p
        join p.paciente pa
        join fetch p.medicamento m
        join fetch p.medico mdc
      where pa.numeroTarjetaSanitaria = :nts
        and p.activa = true
        and r.estado = es.uvigo.dagss.recetas.entidades.EstadoReceta.PLANIFICADA
        and r.fechaValidezFinal >= :hoy
      order by r.fechaValidezInicial asc
      """)
  List<Receta> findPlanificadasEnVigorPorTarjetaSanitaria(@Param("nts") String numeroTarjetaSanitaria,
      @Param("hoy") LocalDate hoy);

  List<Receta> findByPrescripcionIdAndEstado(Long prescripcionId, EstadoReceta estado);
}
