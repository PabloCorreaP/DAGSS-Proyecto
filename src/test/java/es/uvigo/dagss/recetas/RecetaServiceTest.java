package es.uvigo.dagss.recetas;

import es.uvigo.dagss.recetas.entidades.*;
import es.uvigo.dagss.recetas.repositorios.*;
import es.uvigo.dagss.recetas.servicios.RecetaService;
import es.uvigo.dagss.recetas.servicios.excepciones.OperacionNoPermitidaException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@ActiveProfiles("test")
@Import(RecetaService.class)
class RecetaServiceTest {

    @Autowired CentroSaludDAO centroSaludDAO;
    @Autowired MedicoDAO medicoDAO;
    @Autowired PacienteDAO pacienteDAO;
    @Autowired MedicamentoDAO medicamentoDAO;
    @Autowired PrescripcionDAO prescripcionDAO;
    @Autowired RecetaDAO recetaDAO;
    @Autowired FarmaciaDAO farmaciaDAO;

    @Autowired RecetaService recetaService;

    @Test
    void servirReceta_actualiza_estado_y_asigna_farmacia() {
        CentroSalud cs = centroSaludDAO.save(TestDataFactory.centro("CS Vigo", "Vigo", "Pontevedra"));
        Medico m = TestDataFactory.medico("m1", "COL-1", "DNI-1", "Ana", "Perez");
        m.setCentroSalud(cs);
        m = medicoDAO.save(m);
        Paciente p = TestDataFactory.paciente("p1", "DNI-P1", "TS-1", "SS-1", "Luis", "Perez");
        p.setCentroSalud(cs);
        p.setMedicoAsignado(m);
        p = pacienteDAO.save(p);
        Medicamento med = medicamentoDAO.save(TestDataFactory.medicamento("Aspirina", "aa", "fab", "fam", 20));

        LocalDate hoy = LocalDate.now();
        Prescripcion pres = TestDataFactory.prescripcionBasica(m, p, med, 1.0, hoy, hoy.plusDays(10));
        Receta r = new Receta(pres, hoy.minusDays(1), hoy.plusDays(3));
        pres.agregarReceta(r);
        pres = prescripcionDAO.save(pres);

        Farmacia f = farmaciaDAO.save(TestDataFactory.farmacia("f1", "COL-F1", "Farmacia Centro"));

        Receta servida = recetaService.servirReceta(r.getId(), f.getId(), hoy);
        assertThat(servida.getEstado()).isEqualTo(EstadoReceta.SERVIDA);
        assertThat(servida.getFarmacia()).isNotNull();
        assertThat(servida.getFarmacia().getId()).isEqualTo(f.getId());
    }

    @Test
    void servirReceta_fuera_de_validez_da_forbidden() {
        CentroSalud cs = centroSaludDAO.save(TestDataFactory.centro("CS Vigo", "Vigo", "Pontevedra"));
        Medico m = TestDataFactory.medico("m1", "COL-1", "DNI-1", "Ana", "Perez");
        m.setCentroSalud(cs);
        m = medicoDAO.save(m);
        Paciente p = TestDataFactory.paciente("p1", "DNI-P1", "TS-1", "SS-1", "Luis", "Perez");
        p.setCentroSalud(cs);
        p.setMedicoAsignado(m);
        p = pacienteDAO.save(p);
        Medicamento med = medicamentoDAO.save(TestDataFactory.medicamento("Aspirina", "aa", "fab", "fam", 20));

        LocalDate hoy = LocalDate.now();
        Prescripcion pres = TestDataFactory.prescripcionBasica(m, p, med, 1.0, hoy, hoy.plusDays(10));
        Receta r = new Receta(pres, hoy.plusDays(5), hoy.plusDays(10));
        pres.agregarReceta(r);
        prescripcionDAO.save(pres);

        Farmacia f = farmaciaDAO.save(TestDataFactory.farmacia("f1", "COL-F1", "Farmacia Centro"));

        assertThatThrownBy(() -> recetaService.servirReceta(r.getId(), f.getId(), hoy))
                .isInstanceOf(OperacionNoPermitidaException.class);
    }
}
