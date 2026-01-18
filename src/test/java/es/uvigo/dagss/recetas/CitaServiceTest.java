package es.uvigo.dagss.recetas;

import es.uvigo.dagss.recetas.entidades.*;
import es.uvigo.dagss.recetas.repositorios.*;
import es.uvigo.dagss.recetas.servicios.CitaService;
import es.uvigo.dagss.recetas.servicios.excepciones.OperacionNoPermitidaException;
import es.uvigo.dagss.recetas.servicios.excepciones.ValidacionException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@ActiveProfiles("test")
@Import(CitaService.class)
class CitaServiceTest {

    @Autowired CentroSaludDAO centroSaludDAO;
    @Autowired MedicoDAO medicoDAO;
    @Autowired PacienteDAO pacienteDAO;
    @Autowired CitaDAO citaDAO;

    @Autowired CitaService citaService;

    @Test
    void huecosDisponibles_devuelve_todos_menos_los_ocupados() {
        CentroSalud cs = centroSaludDAO.save(TestDataFactory.centro("CS Vigo", "Vigo", "Pontevedra"));

        Medico mTmp = TestDataFactory.medico("m1", "COL-1", "DNI-1", "Ana", "Perez");
        mTmp.setCentroSalud(cs);
        Medico m = medicoDAO.save(mTmp);

        Paciente pTmp = TestDataFactory.paciente("p1", "DNI-P1", "TS-1", "SS-1", "Luis", "Perez");
        pTmp.setCentroSalud(cs);
        pTmp.setMedicoAsignado(m);
        Paciente p = pacienteDAO.save(pTmp);

        LocalDate dia = LocalDate.now().plusDays(3);
        citaDAO.save(new Cita(p, m, dia, LocalTime.of(9, 0)));

        var huecos = citaService.huecosDisponibles(m.getId(), dia);
        assertThat(huecos).hasSize(27); // 28 slots - 1 ocupado
        assertThat(huecos).doesNotContain(LocalTime.of(9, 0));
        assertThat(huecos).contains(LocalTime.of(8, 30));
        assertThat(huecos).contains(LocalTime.of(9, 15));
    }

    @Test
    void crearCitaPaciente_crea_y_evitar_duplicados_en_mismo_hueco() {
        CentroSalud cs = centroSaludDAO.save(TestDataFactory.centro("CS Vigo", "Vigo", "Pontevedra"));

        Medico mTmp = TestDataFactory.medico("m1", "COL-1", "DNI-1", "Ana", "Perez");
        mTmp.setCentroSalud(cs);
        Medico m = medicoDAO.save(mTmp);

        Paciente pTmp = TestDataFactory.paciente("p1", "DNI-P1", "TS-1", "SS-1", "Luis", "Perez");
        pTmp.setCentroSalud(cs);
        pTmp.setMedicoAsignado(m);
        final Paciente p = pacienteDAO.save(pTmp); // ← final/effectively-final

        LocalDate dia = LocalDate.now().plusDays(4);
        LocalTime hora = LocalTime.of(10, 0);

        Cita c = citaService.crearCitaPaciente(p.getId(), dia, hora);
        assertThat(c.getId()).isNotNull();
        assertThat(c.getEstado()).isEqualTo(EstadoCita.PLANIFICADA);

        assertThatThrownBy(() -> citaService.crearCitaPaciente(p.getId(), dia, hora))
                .isInstanceOf(OperacionNoPermitidaException.class);
    }

    @Test
    void crearCitaPaciente_valida_horario_y_intervalos_15_min() {
        CentroSalud cs = centroSaludDAO.save(TestDataFactory.centro("CS Vigo", "Vigo", "Pontevedra"));

        Medico mTmp = TestDataFactory.medico("m1", "COL-1", "DNI-1", "Ana", "Perez");
        mTmp.setCentroSalud(cs);
        Medico m = medicoDAO.save(mTmp);

        Paciente pTmp = TestDataFactory.paciente("p1", "DNI-P1", "TS-1", "SS-1", "Luis", "Perez");
        pTmp.setCentroSalud(cs);
        pTmp.setMedicoAsignado(m);
        final Paciente p = pacienteDAO.save(pTmp); // ← final

        LocalDate dia = LocalDate.now().plusDays(5);

        assertThatThrownBy(() -> citaService.crearCitaPaciente(p.getId(), dia, LocalTime.of(8, 0)))
                .isInstanceOf(ValidacionException.class);

        assertThatThrownBy(() -> citaService.crearCitaPaciente(p.getId(), dia, LocalTime.of(9, 7)))
                .isInstanceOf(ValidacionException.class);
    }

    @Test
    void anularComoPaciente_solo_su_propietario() {
        CentroSalud cs = centroSaludDAO.save(TestDataFactory.centro("CS Vigo", "Vigo", "Pontevedra"));

        Medico mTmp = TestDataFactory.medico("m1", "COL-1", "DNI-1", "Ana", "Perez");
        mTmp.setCentroSalud(cs);
        Medico m = medicoDAO.save(mTmp);

        Paciente p1Tmp = TestDataFactory.paciente("p1", "DNI-P1", "TS-1", "SS-1", "Luis", "Perez");
        p1Tmp.setCentroSalud(cs);
        p1Tmp.setMedicoAsignado(m);
        final Paciente p1 = pacienteDAO.save(p1Tmp);

        Paciente p2Tmp = TestDataFactory.paciente("p2", "DNI-P2", "TS-2", "SS-2", "Lucia", "Lopez");
        p2Tmp.setCentroSalud(cs);
        p2Tmp.setMedicoAsignado(m);
        final Paciente p2 = pacienteDAO.save(p2Tmp);

        Cita c = citaDAO.save(new Cita(p1, m, LocalDate.now().plusDays(6), LocalTime.of(11, 0)));

        assertThatThrownBy(() -> citaService.anularComoPaciente(c.getId(), p2.getId()))
                .isInstanceOf(OperacionNoPermitidaException.class);

        citaService.anularComoPaciente(c.getId(), p1.getId());
        Cita recargada = citaDAO.findById(c.getId()).orElseThrow();
        assertThat(recargada.getEstado()).isEqualTo(EstadoCita.ANULADA);
    }
}
