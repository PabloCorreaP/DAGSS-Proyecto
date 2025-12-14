package es.uvigo.dagss.recetas.repositorios;

import es.uvigo.dagss.recetas.entidades.Prescripcion;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PrescripcionRepository extends JpaRepository<Prescripcion, Long> {

    @Query("""
           select p
           from Prescripcion p
             join fetch p.medicamento m
             join fetch p.medico md
           where p.paciente.id = :pacienteId
             and p.activa = true
             and p.fechaFin >= :hoy
           order by p.fechaInicio
           """)
    List<Prescripcion> findEnVigorDePaciente(@Param("pacienteId") Long pacienteId,
                                            @Param("hoy") LocalDate hoy);
}
