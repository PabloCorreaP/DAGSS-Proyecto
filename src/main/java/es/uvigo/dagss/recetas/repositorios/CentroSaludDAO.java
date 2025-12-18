package es.uvigo.dagss.recetas.repositorios;

import es.uvigo.dagss.recetas.entidades.CentroSalud;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CentroSaludDAO extends JpaRepository<CentroSalud, Long> {

    List<CentroSalud> findByActivoTrueOrderByNombreAsc();

    List<CentroSalud> findByActivoTrueAndProvinciaIgnoreCaseOrderByLocalidadAscNombreAsc(String provincia);

    @Query("""
           select c
           from CentroSalud c
           where c.activo = true
             and (
                  :texto is null
                  or lower(c.nombre) like lower(concat('%', :texto, '%'))
                  or lower(c.localidad) like lower(concat('%', :texto, '%'))
             )
           order by c.nombre
           """)
    List<CentroSalud> buscarActivosPorNombreOLocalidadLike(@Param("texto") String texto);
}
