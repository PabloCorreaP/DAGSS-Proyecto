package es.uvigo.dagss.recetas.repositorios;

import es.uvigo.dagss.recetas.entidades.Medicamento;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MedicamentoRepository extends JpaRepository<Medicamento, Long> {

    List<Medicamento> findByActivoTrueOrderByNombreComercialAsc();

    @Query("""
           select m
           from Medicamento m
           where m.activo = true
             and (
                :texto is null
                or lower(m.nombreComercial) like lower(concat('%', :texto, '%'))
                or lower(m.principioActivo) like lower(concat('%', :texto, '%'))
                or lower(m.fabricante) like lower(concat('%', :texto, '%'))
                or lower(m.familia) like lower(concat('%', :texto, '%'))
             )
           order by m.nombreComercial
           """)
    List<Medicamento> buscarActivosLike(@Param("texto") String texto);
}
