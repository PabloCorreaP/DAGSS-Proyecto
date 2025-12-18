package es.uvigo.dagss.recetas.repositorios;

import es.uvigo.dagss.recetas.entidades.Paciente;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PacienteDAO extends JpaRepository<Paciente, Long> {

    Optional<Paciente> findByLogin(String login);

    Optional<Paciente> findByNumeroTarjetaSanitaria(String numeroTarjetaSanitaria);

    List<Paciente> findByActivoTrueOrderByApellidosAscNombreAsc();

    @Query("""
           select p
           from Paciente p
             left join p.centroSalud c
             left join p.medicoAsignado m
           where p.activo = true
             and (
                :nombre is null
                or lower(p.nombre) like lower(concat('%', :nombre, '%'))
                or lower(p.apellidos) like lower(concat('%', :nombre, '%'))
             )
             and (
                :localidad is null
                or lower(p.localidad) like lower(concat('%', :localidad, '%'))
             )
             and (
                :centroId is null
                or (c is not null and c.id = :centroId)
             )
             and (
                :medicoId is null
                or (m is not null and m.id = :medicoId)
             )
           order by p.apellidos, p.nombre
           """)
    List<Paciente> buscarActivos(@Param("nombre") String nombre,
                                @Param("localidad") String localidad,
                                @Param("centroId") Long centroId,
                                @Param("medicoId") Long medicoId);
}
