package es.uvigo.dagss.recetas;

import es.uvigo.dagss.recetas.entidades.CentroSalud;
import es.uvigo.dagss.recetas.entidades.Medico;
import es.uvigo.dagss.recetas.entidades.Paciente;
import es.uvigo.dagss.recetas.repositorios.CentroSaludDAO;
import es.uvigo.dagss.recetas.repositorios.MedicoDAO;
import es.uvigo.dagss.recetas.repositorios.PacienteDAO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class PacienteRepositoryTest {

    @Autowired CentroSaludDAO centroSaludDAO;
    @Autowired MedicoDAO medicoDAO;
    @Autowired PacienteDAO pacienteDAO;

    @Test
    void buscarActivos_filtra_por_nombre_localidad_centro_y_medico() {
        CentroSalud vigo = centroSaludDAO.save(TestDataFactory.centro("CS Vigo", "Vigo", "Pontevedra"));
        CentroSalud ourense = centroSaludDAO.save(TestDataFactory.centro("CS Ourense", "Ourense", "Ourense"));

        Medico mVigo = TestDataFactory.medico("mv", "colmv", "dnimv", "Ana", "Med");
        mVigo.setCentroSalud(vigo);
        mVigo = medicoDAO.save(mVigo);

        Medico mOur = TestDataFactory.medico("mo", "colmo", "dnimo", "Juan", "Med");
        mOur.setCentroSalud(ourense);
        mOur = medicoDAO.save(mOur);

        Paciente p1 = TestDataFactory.paciente("p1", "dniP1", "nts1", "nss1", "Luis", "Perez");
        p1.setCentroSalud(vigo);
        p1.setMedicoAsignado(mVigo);
        p1.setLocalidad("Vigo");
        pacienteDAO.save(p1);

        Paciente p2 = TestDataFactory.paciente("p2", "dniP2", "nts2", "nss2", "Lucia", "Lopez");
        p2.setCentroSalud(ourense);
        p2.setMedicoAsignado(mOur);
        p2.setLocalidad("Ourense");
        pacienteDAO.save(p2);

        Paciente p3 = TestDataFactory.paciente("p3", "dniP3", "nts3", "nss3", "Luis", "Garcia");
        p3.setCentroSalud(ourense);
        p3.setMedicoAsignado(mOur);
        p3.setLocalidad("Ourense");
        p3.setActivo(false);
        pacienteDAO.save(p3);

        var porNombre = pacienteDAO.buscarActivos("luis", null, null, null);
        assertThat(porNombre).hasSize(1);
        assertThat(porNombre.get(0).getLogin()).isEqualTo("p1");

        var porLocalidad = pacienteDAO.buscarActivos(null, "our", null, null);
        assertThat(porLocalidad).hasSize(1);
        assertThat(porLocalidad.get(0).getLogin()).isEqualTo("p2");

        var porCentro = pacienteDAO.buscarActivos(null, null, ourense.getId(), null);
        assertThat(porCentro).hasSize(1);
        assertThat(porCentro.get(0).getLogin()).isEqualTo("p2");

        var porMedico = pacienteDAO.buscarActivos(null, null, null, mOur.getId());
        assertThat(porMedico).hasSize(1);
        assertThat(porMedico.get(0).getLogin()).isEqualTo("p2");
    }
}
