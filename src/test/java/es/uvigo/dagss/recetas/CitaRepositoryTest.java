package es.uvigo.dagss.recetas;

import es.uvigo.dagss.recetas.entidades.*;
import es.uvigo.dagss.recetas.repositorios.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class CitaRepositoryTest {

    @Autowired CentroSaludDAO centroSaludDAO;
    @Autowired MedicoDAO medicoDAO;
    @Autowired PacienteDAO pacienteDAO;
    @Autowired CitaDAO citaDAO;

    @Test
    void buscarPorFechaConFiltros_ordena_y_aplica_filtros_opcionales() {
        CentroSalud cs = centroSaludDAO.save(TestDataFactory.centro("CS Vigo", "Vigo", "Pontevedra"));
        Medico m1 = TestDataFactory.medico("m1", "col1", "dni1", "Ana", "Perez");
        m1.setCentroSalud(cs);
        m1 = medicoDAO.save(m1);
        Medico m2 = TestDataFactory.medico("m2", "col2", "dni2", "Juan", "Lopez");
        m2.setCentroSalud(cs);
        m2 = medicoDAO.save(m2);

        Paciente p1 = TestDataFactory.paciente("p1", "dniP1", "nts1", "nss1", "Luis", "Perez");
        p1.setCentroSalud(cs);
        p1.setMedicoAsignado(m1);
        p1 = pacienteDAO.save(p1);

        LocalDate dia = LocalDate.now().plusDays(2);
        Cita c1 = new Cita(p1, m1, dia, LocalTime.of(9, 0));
        Cita c2 = new Cita(p1, m2, dia, LocalTime.of(8, 30));
        citaDAO.save(c1);
        citaDAO.save(c2);

        var todas = citaDAO.buscarPorFechaConFiltros(dia, null, null);
        assertThat(todas).hasSize(2);
        assertThat(todas.get(0).getHoraInicio()).isEqualTo(LocalTime.of(8, 30));
        assertThat(todas.get(1).getHoraInicio()).isEqualTo(LocalTime.of(9, 0));

        var soloM1 = citaDAO.buscarPorFechaConFiltros(dia, m1.getId(), null);
        assertThat(soloM1).hasSize(1);
        assertThat(soloM1.get(0).getMedico().getId()).isEqualTo(m1.getId());

        var soloP1 = citaDAO.buscarPorFechaConFiltros(dia, null, p1.getId());
        assertThat(soloP1).hasSize(2);
    }

    @Test
    void findFuturasPlanificadasDePaciente_filtra_por_hoy_y_hora() {
        CentroSalud cs = centroSaludDAO.save(TestDataFactory.centro("CS Vigo", "Vigo", "Pontevedra"));
        Medico m = TestDataFactory.medico("m1", "col1", "dni1", "Ana", "Perez");
        m.setCentroSalud(cs);
        m = medicoDAO.save(m);
        Paciente p = TestDataFactory.paciente("p1", "dniP1", "nts1", "nss1", "Luis", "Perez");
        p.setCentroSalud(cs);
        p.setMedicoAsignado(m);
        p = pacienteDAO.save(p);

        LocalDate hoy = LocalDate.now();
        LocalTime ahora = LocalTime.of(10, 0);

        // Hoy pero antes de ahora -> no
        Cita pasada = new Cita(p, m, hoy, LocalTime.of(9, 0));
        pasada.setEstado(EstadoCita.PLANIFICADA);
        citaDAO.save(pasada);

        // Hoy pero despues de ahora -> si
        Cita hoyDespues = new Cita(p, m, hoy, LocalTime.of(11, 0));
        hoyDespues.setEstado(EstadoCita.PLANIFICADA);
        citaDAO.save(hoyDespues);

        // Futuro -> si
        Cita futura = new Cita(p, m, hoy.plusDays(1), LocalTime.of(8, 30));
        futura.setEstado(EstadoCita.PLANIFICADA);
        citaDAO.save(futura);

        // Anulada en futuro -> no
        Cita anulada = new Cita(p, m, hoy.plusDays(1), LocalTime.of(9, 0));
        anulada.setEstado(EstadoCita.ANULADA);
        citaDAO.save(anulada);

        var res = citaDAO.findFuturasPlanificadasDePaciente(p.getId(), hoy, ahora);
        assertThat(res).hasSize(2);
        assertThat(res).allMatch(c -> c.getEstado() == EstadoCita.PLANIFICADA);
    }
}
