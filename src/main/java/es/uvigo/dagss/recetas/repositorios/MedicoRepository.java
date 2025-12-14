package es.uvigo.dagss.recetas.repositorios;

import es.uvigo.dagss.recetas.entidades.Medico;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MedicoRepository extends JpaRepository<Medico, Long> {

    Optional<Medico> findByLogin(String login);

    Optional<Medico> findByNumeroColegiado(String numeroColegiado);

    List<Medico> findByActivoTrueOrderByApellidosAscNombreAsc();

    @Query("""
           select m
           from Medico m
             left join m.centroSalud c
           where m.activo = true
             and (
                 :nombre is null
                 or lower(m.nombre) like lower(concat('%', :nombre, '%'))
                 or lower(m.apellidos) like lower(concat('%', :nombre, '%'))
             )
             and (
                 :localidad is null
                 or (c is not null and lower(c.localidad) like lower(concat('%', :localidad, '%')))
             )
             and (
                 :centroId is null
                 or (c is not null and c.id = :centroId)
             )
           order by m.apellidos, m.nombre
           """)
    List<Medico> buscarActivos(@Param("nombre") String nombre,
                              @Param("localidad") String localidad,
                              @Param("centroId") Long centroId);

    @Query("""
           select m
           from Medico m
           where m.activo = true
             and m.centroSalud.id = :centroId
           order by m.apellidos, m.nombre
           """)
    List<Medico> findActivosByCentro(@Param("centroId") Long centroId);
}
