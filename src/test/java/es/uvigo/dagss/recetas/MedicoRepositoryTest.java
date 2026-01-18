package es.uvigo.dagss.recetas;

import es.uvigo.dagss.recetas.entidades.CentroSalud;
import es.uvigo.dagss.recetas.entidades.Medico;
import es.uvigo.dagss.recetas.repositorios.CentroSaludDAO;
import es.uvigo.dagss.recetas.repositorios.MedicoDAO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class MedicoRepositoryTest {

    @Autowired CentroSaludDAO centroSaludDAO;
    @Autowired MedicoDAO medicoDAO;

    @Test
    void buscarActivos_filtra_por_nombre_localidad_y_centro() {
        CentroSalud vigo = centroSaludDAO.save(TestDataFactory.centro("CS Vigo", "Vigo", "Pontevedra"));
        CentroSalud ourense = centroSaludDAO.save(TestDataFactory.centro("CS Ourense", "Ourense", "Ourense"));

        Medico m1 = TestDataFactory.medico("m1", "col1", "dni1", "Ana", "Perez");
        m1.setCentroSalud(vigo);
        medicoDAO.save(m1);

        Medico m2 = TestDataFactory.medico("m2", "col2", "dni2", "Juan", "Lopez");
        m2.setCentroSalud(ourense);
        medicoDAO.save(m2);

        Medico m3 = TestDataFactory.medico("m3", "col3", "dni3", "Ana", "Garcia");
        m3.setCentroSalud(ourense);
        m3.setActivo(false);
        medicoDAO.save(m3);

        var res1 = medicoDAO.buscarActivos("ana", null, null);
        assertThat(res1).hasSize(1);
        assertThat(res1.get(0).getLogin()).isEqualTo("m1");

        var res2 = medicoDAO.buscarActivos(null, "our", null);
        assertThat(res2).hasSize(1);
        assertThat(res2.get(0).getLogin()).isEqualTo("m2");

        var res3 = medicoDAO.buscarActivos(null, null, vigo.getId());
        assertThat(res3).hasSize(1);
        assertThat(res3.get(0).getLogin()).isEqualTo("m1");
    }

    @Test
    void findActivosByCentro_devuelve_solo_activos_de_ese_centro() {
        CentroSalud vigo = centroSaludDAO.save(TestDataFactory.centro("CS Vigo", "Vigo", "Pontevedra"));
        CentroSalud ourense = centroSaludDAO.save(TestDataFactory.centro("CS Ourense", "Ourense", "Ourense"));

        Medico a = TestDataFactory.medico("a", "cola", "dnia", "A", "A");
        a.setCentroSalud(vigo);
        medicoDAO.save(a);

        Medico b = TestDataFactory.medico("b", "colb", "dnib", "B", "B");
        b.setCentroSalud(vigo);
        b.setActivo(false);
        medicoDAO.save(b);

        Medico c = TestDataFactory.medico("c", "colc", "dnic", "C", "C");
        c.setCentroSalud(ourense);
        medicoDAO.save(c);

        var res = medicoDAO.findActivosByCentro(vigo.getId());
        assertThat(res).hasSize(1);
        assertThat(res.get(0).getLogin()).isEqualTo("a");
    }
}
