package es.uvigo.dagss.recetas;

import es.uvigo.dagss.recetas.entidades.Medicamento;
import es.uvigo.dagss.recetas.repositorios.MedicamentoDAO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class MedicamentoRepositoryTest {

    @Autowired
    MedicamentoDAO medicamentoDAO;

    @Test
    void buscarActivosLike_filtraPorCampos_y_ignora_inactivos() {
        Medicamento m1 = medicamentoDAO.save(TestDataFactory.medicamento(
                "Aspirina 500", "acido acetilsalicilico", "Bayer", "analgesicos", 20));
        Medicamento m2 = medicamentoDAO.save(TestDataFactory.medicamento(
                "Ibuprofeno 600", "ibuprofeno", "Generic", "antiinflamatorios", 30));
        Medicamento m3 = medicamentoDAO.save(TestDataFactory.medicamento(
                "Aspirina Forte", "acido acetilsalicilico", "Bayer", "analgesicos", 20));
        m3.setActivo(false);
        medicamentoDAO.save(m3);

        var res = medicamentoDAO.buscarActivosLike("asp");

        assertThat(res).hasSize(1);
        assertThat(res.get(0).getNombreComercial()).isEqualTo(m1.getNombreComercial());
        assertThat(res).allMatch(Medicamento::getActivo);
    }

    @Test
    void buscarActivosLike_conTextoNull_devuelveTodosLosActivos_ordenados() {
        medicamentoDAO.save(TestDataFactory.medicamento("B Medicamento", "p", "f", "fam", 10));
        medicamentoDAO.save(TestDataFactory.medicamento("A Medicamento", "p", "f", "fam", 10));

        var res = medicamentoDAO.buscarActivosLike(null);

        assertThat(res).hasSize(2);
        assertThat(res.get(0).getNombreComercial()).isEqualTo("A Medicamento");
        assertThat(res.get(1).getNombreComercial()).isEqualTo("B Medicamento");
    }
}
