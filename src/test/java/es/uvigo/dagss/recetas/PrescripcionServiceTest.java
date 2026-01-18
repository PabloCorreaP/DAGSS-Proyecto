package es.uvigo.dagss.recetas;

import es.uvigo.dagss.recetas.entidades.*;
import es.uvigo.dagss.recetas.repositorios.*;
import es.uvigo.dagss.recetas.servicios.PrescripcionService;
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
@Import(PrescripcionService.class)
class PrescripcionServiceTest {

    @Autowired CentroSaludDAO centroSaludDAO;
    @Autowired MedicoDAO medicoDAO;
    @Autowired PacienteDAO pacienteDAO;
    @Autowired MedicamentoDAO medicamentoDAO;
    @Autowired PrescripcionDAO prescripcionDAO;
    @Autowired RecetaDAO recetaDAO;

    @Autowired PrescripcionService prescripcionService;

    @Test
    void crearPrescripcion_genera_plan_de_recetas_y_setea_fechaInicio_hoy() {
        CentroSalud cs = centroSaludDAO.save(TestDataFactory.centro("CS Vigo", "Vigo", "Pontevedra"));
        Medico m = TestDataFactory.medico("m1", "COL-1", "DNI-1", "Ana", "Perez");
        m.setCentroSalud(cs);
        m = medicoDAO.save(m);

        Paciente p = TestDataFactory.paciente("p1", "DNI-P1", "TS-1", "SS-1", "Luis", "Perez");
        p.setCentroSalud(cs);
        p.setMedicoAsignado(m);
        p = pacienteDAO.save(p);

        Medicamento med = medicamentoDAO.save(TestDataFactory.medicamento(
                "Aspirina 500", "aa", "Bayer", "analgesicos", 20));

        LocalDate hoy = LocalDate.now();
        LocalDate fin = hoy.plusDays(29); // 30 dias incl.

        Prescripcion pres = prescripcionService.crearPrescripcion(
                m.getId(), p.getId(), med.getId(), 2.0, "2 al dia", fin);

        assertThat(pres.getId()).isNotNull();
        assertThat(pres.getActiva()).isTrue();
        assertThat(pres.getFechaInicio()).isEqualTo(hoy);
        assertThat(pres.getFechaFin()).isEqualTo(fin);

        // 30 dias * 2 dosis/dia = 60 dosis -> /20 = 3 cajas => 3 recetas (1 caja por receta)
        var recetas = recetaDAO.findPendientesDePaciente(p.getId(), hoy);
        assertThat(recetas).hasSize(3);
        assertThat(recetas).allMatch(r -> r.getEstado() == EstadoReceta.PLANIFICADA);
        assertThat(recetas).allMatch(r -> r.getNumeroUnidades() == 1);

        // Orden y validez razonable (margen de +- 1 semana alrededor de fechas exactas)
        assertThat(recetas)
                .extracting(Receta::getFechaValidezInicial)
                .isSorted();

        assertThat(recetas.get(0).getFechaValidezInicial()).isEqualTo(hoy);
        assertThat(recetas.get(0).getFechaValidezFinal()).isEqualTo(hoy.plusWeeks(1));
    }

    @Test
    void anularPrescripcion_marca_inactiva_y_anula_sus_recetas() {
        CentroSalud cs = centroSaludDAO.save(TestDataFactory.centro("CS Vigo", "Vigo", "Pontevedra"));
        Medico m = TestDataFactory.medico("m1", "COL-1", "DNI-1", "Ana", "Perez");
        m.setCentroSalud(cs);
        m = medicoDAO.save(m);

        Paciente p = TestDataFactory.paciente("p1", "DNI-P1", "TS-1", "SS-1", "Luis", "Perez");
        p.setCentroSalud(cs);
        p.setMedicoAsignado(m);
        p = pacienteDAO.save(p);

        Medicamento med = medicamentoDAO.save(TestDataFactory.medicamento(
                "Aspirina 500", "aa", "Bayer", "analgesicos", 20));

        Prescripcion pres = prescripcionService.crearPrescripcion(
                m.getId(), p.getId(), med.getId(), 1.0, "", LocalDate.now().plusDays(10));

        prescripcionService.anularPrescripcion(pres.getId(), m.getId());

        Prescripcion recargada = prescripcionDAO.findById(pres.getId()).orElseThrow();
        assertThat(recargada.getActiva()).isFalse();

        var recetas = recetaDAO.findByPrescripcionIdAndEstado(pres.getId(), EstadoReceta.ANULADA);
        assertThat(recetas).isNotEmpty();
    }
        @Test
        void anularPrescripcion_con_medico_distinto_da_forbidden() {
        CentroSalud cs = centroSaludDAO.save(TestDataFactory.centro("CS Vigo", "Vigo", "Pontevedra"));

        Medico m1Tmp = TestDataFactory.medico("m1", "COL-1", "DNI-1", "Ana", "Perez");
        m1Tmp.setCentroSalud(cs);
        Medico m1 = medicoDAO.save(m1Tmp);

        Medico m2Tmp = TestDataFactory.medico("m2", "COL-2", "DNI-2", "Juan", "Lopez");
        m2Tmp.setCentroSalud(cs);
        final Medico m2 = medicoDAO.save(m2Tmp); // <- final, no reasignado

        Paciente pTmp = TestDataFactory.paciente("p1", "DNI-P1", "TS-1", "SS-1", "Luis", "Perez");
        pTmp.setCentroSalud(cs);
        pTmp.setMedicoAsignado(m1);
        Paciente p = pacienteDAO.save(pTmp);

        Medicamento med = medicamentoDAO.save(TestDataFactory.medicamento(
                "Aspirina 500", "aa", "Bayer", "analgesicos", 20));

        Prescripcion pres = prescripcionService.crearPrescripcion(
                m1.getId(), p.getId(), med.getId(), 1.0, "", LocalDate.now().plusDays(10));

        assertThatThrownBy(() -> prescripcionService.anularPrescripcion(pres.getId(), m2.getId()))
                .isInstanceOf(OperacionNoPermitidaException.class);
        }

}
