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
class RecetaRepositoryTest {

    @Autowired CentroSaludDAO centroSaludDAO;
    @Autowired MedicoDAO medicoDAO;
    @Autowired PacienteDAO pacienteDAO;
    @Autowired MedicamentoDAO medicamentoDAO;
    @Autowired PrescripcionDAO prescripcionDAO;
    @Autowired RecetaDAO recetaDAO;

    @Test
    void findPendientesDePaciente_devuelve_planificadas_y_no_servidas_ordenadas_por_fechaValidezInicial() {
        CentroSalud cs = centroSaludDAO.save(TestDataFactory.centro("CS Vigo", "Vigo", "Pontevedra"));
        Medico m = TestDataFactory.medico("m1", "col1", "dni1", "Ana", "Perez");
        m.setCentroSalud(cs);
        m = medicoDAO.save(m);
        Paciente p = TestDataFactory.paciente("p1", "dniP1", "TS-1", "SS-1", "Luis", "Perez");
        p.setCentroSalud(cs);
        p.setMedicoAsignado(m);
        p = pacienteDAO.save(p);

        Medicamento med = medicamentoDAO.save(TestDataFactory.medicamento("Aspirina", "aa", "fab", "fam", 20));

        LocalDate hoy = LocalDate.now();
        Prescripcion pres = TestDataFactory.prescripcionBasica(m, p, med, 1.0, hoy.minusDays(1), hoy.plusDays(20));

        Receta r1 = new Receta(pres, hoy.minusDays(1), hoy.plusDays(1));
        Receta r2 = new Receta(pres, hoy.plusDays(5), hoy.plusDays(12));
        Receta rServida = new Receta(pres, hoy.minusDays(2), hoy.plusDays(2));
        rServida.setEstado(EstadoReceta.SERVIDA);

        pres.agregarReceta(r1);
        pres.agregarReceta(r2);
        pres.agregarReceta(rServida);
        prescripcionDAO.save(pres);

        var res = recetaDAO.findPendientesDePaciente(p.getId(), hoy);
        assertThat(res).hasSize(2);
        assertThat(res.get(0).getFechaValidezInicial()).isBeforeOrEqualTo(res.get(1).getFechaValidezInicial());
        assertThat(res).allMatch(r -> r.getEstado() == EstadoReceta.PLANIFICADA);
    }

    @Test
    void findPlanificadasEnVigorPorTarjetaSanitaria_filtra_por_validez_y_estado() {
        CentroSalud cs = centroSaludDAO.save(TestDataFactory.centro("CS Vigo", "Vigo", "Pontevedra"));
        Medico m = TestDataFactory.medico("m1", "col1", "dni1", "Ana", "Perez");
        m.setCentroSalud(cs);
        m = medicoDAO.save(m);
        Paciente p = TestDataFactory.paciente("p1", "dniP1", "TS-123", "SS-1", "Luis", "Perez");
        p.setCentroSalud(cs);
        p.setMedicoAsignado(m);
        p = pacienteDAO.save(p);

        Medicamento med = medicamentoDAO.save(TestDataFactory.medicamento("Aspirina", "aa", "fab", "fam", 20));
        LocalDate hoy = LocalDate.now();
        Prescripcion pres = TestDataFactory.prescripcionBasica(m, p, med, 1.0, hoy, hoy.plusDays(30));

        // En vigor (hoy dentro)
        Receta ok = new Receta(pres, hoy.minusDays(1), hoy.plusDays(7));
        // Fuera de vigor (ya vencida)
        Receta vencida = new Receta(pres, hoy.minusDays(10), hoy.minusDays(1));
        // No planificada
        Receta anulada = new Receta(pres, hoy.minusDays(1), hoy.plusDays(7));
        anulada.setEstado(EstadoReceta.ANULADA);

        pres.agregarReceta(ok);
        pres.agregarReceta(vencida);
        pres.agregarReceta(anulada);
        prescripcionDAO.save(pres);

        var res = recetaDAO.findPlanificadasEnVigorPorTarjetaSanitaria("TS-123", hoy);
        assertThat(res).hasSize(1);
        assertThat(res.get(0).getEstado()).isEqualTo(EstadoReceta.PLANIFICADA);
        assertThat(res.get(0).getFechaValidezInicial()).isBeforeOrEqualTo(hoy);
        assertThat(res.get(0).getFechaValidezFinal()).isAfterOrEqualTo(hoy);
    }
}
