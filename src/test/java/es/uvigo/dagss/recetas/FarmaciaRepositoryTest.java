package es.uvigo.dagss.recetas;

import es.uvigo.dagss.recetas.entidades.Farmacia;
import es.uvigo.dagss.recetas.repositorios.FarmaciaDAO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class FarmaciaRepositoryTest {

    @Autowired
    FarmaciaDAO farmaciaDAO;

    @Test
    void buscarActivasPorNombreEstablecimientoOLocalidadLike_funciona_y_excluye_inactivas() {
        Farmacia f1 = TestDataFactory.farmacia("f1", "c1", "Farmacia Centro");
        f1.setLocalidad("Vigo");
        farmaciaDAO.save(f1);

        Farmacia f2 = TestDataFactory.farmacia("f2", "c2", "Farmacia Norte");
        f2.setLocalidad("Ourense");
        farmaciaDAO.save(f2);

        Farmacia f3 = TestDataFactory.farmacia("f3", "c3", "Farmacia Sur");
        f3.setLocalidad("Vigo");
        f3.setActivo(false);
        farmaciaDAO.save(f3);

        var res = farmaciaDAO.buscarActivasPorNombreEstablecimientoOLocalidadLike("vIg");
        assertThat(res).hasSize(1);
        assertThat(res.get(0).getNombreEstablecimiento()).isEqualTo("Farmacia Centro");
        assertThat(res).allMatch(Farmacia::getActivo);
    }
}
