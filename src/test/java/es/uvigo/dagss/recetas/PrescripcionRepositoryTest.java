package es.uvigo.dagss.recetas;

import es.uvigo.dagss.recetas.entidades.*;
import es.uvigo.dagss.recetas.repositorios.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class PrescripcionRepositoryTest {

    @Autowired CentroSaludDAO centroSaludDAO;
    @Autowired MedicoDAO medicoDAO;
    @Autowired PacienteDAO pacienteDAO;
    @Autowired MedicamentoDAO medicamentoDAO;
    @Autowired PrescripcionDAO prescripcionDAO;

    @Test
    void findEnVigorDePaciente_devuelve_activas_con_fechaFin_no_pasada_y_ordenadas_por_inicio() {
        CentroSalud cs = centroSaludDAO.save(TestDataFactory.centro("CS Vigo", "Vigo", "Pontevedra"));
        Medico m = TestDataFactory.medico("m1", "col1", "dni1", "Ana", "Perez");
        m.setCentroSalud(cs);
        m = medicoDAO.save(m);
        Paciente p = TestDataFactory.paciente("p1", "dniP1", "nts1", "nss1", "Luis", "Perez");
        p.setCentroSalud(cs);
        p.setMedicoAsignado(m);
        p = pacienteDAO.save(p);

        Medicamento med = medicamentoDAO.save(TestDataFactory.medicamento("Aspirina", "aa", "fab", "fam", 20));

        LocalDate hoy = LocalDate.now();

        Prescripcion enVigor1 = TestDataFactory.prescripcionBasica(m, p, med, 1.0, hoy.minusDays(2), hoy.plusDays(10));
        prescripcionDAO.save(enVigor1);

        Prescripcion enVigor2 = TestDataFactory.prescripcionBasica(m, p, med, 1.0, hoy.minusDays(1), hoy); // fin hoy
        prescripcionDAO.save(enVigor2);

        Prescripcion caducada = TestDataFactory.prescripcionBasica(m, p, med, 1.0, hoy.minusDays(10), hoy.minusDays(1));
        prescripcionDAO.save(caducada);

        Prescripcion inactiva = TestDataFactory.prescripcionBasica(m, p, med, 1.0, hoy.minusDays(5), hoy.plusDays(5));
        inactiva.setActiva(false);
        prescripcionDAO.save(inactiva);

        var res = prescripcionDAO.findEnVigorDePaciente(p.getId(), hoy);
        assertThat(res).hasSize(2);
        assertThat(res.get(0).getFechaInicio()).isBeforeOrEqualTo(res.get(1).getFechaInicio());
        assertThat(res).allMatch(Prescripcion::getActiva);
        assertThat(res).allMatch(x -> !x.getFechaFin().isBefore(hoy));
    }
}
