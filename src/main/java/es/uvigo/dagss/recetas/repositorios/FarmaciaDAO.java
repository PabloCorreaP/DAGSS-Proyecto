package es.uvigo.dagss.recetas.repositorios;

import es.uvigo.dagss.recetas.entidades.Farmacia;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FarmaciaDAO extends JpaRepository<Farmacia, Long> {

    Optional<Farmacia> findByLogin(String login);

    Optional<Farmacia> findByNumeroColegiadoFarmaceutico(String numeroColegiadoFarmaceutico);

    List<Farmacia> findByActivoTrueOrderByNombreEstablecimientoAsc();

    @Query("""
           select f
           from Farmacia f
           where f.activo = true
             and (
                 :texto is null
                 or lower(f.nombreEstablecimiento) like lower(concat('%', :texto, '%'))
                 or lower(f.localidad) like lower(concat('%', :texto, '%'))
             )
           order by f.nombreEstablecimiento
           """)
    List<Farmacia> buscarActivasPorNombreEstablecimientoOLocalidadLike(@Param("texto") String texto);
}
