package es.uvigo.dagss.recetas;

import es.uvigo.dagss.recetas.entidades.*;

import java.time.LocalDate;
import java.util.Date;

/**
 * Helper simple para crear datos coherentes en tests.
 *
 * Ojo: no usa builders sofisticados a proposito (menos magia = menos sorpresas).
 */
public final class TestDataFactory {

    private TestDataFactory() {}

    public static CentroSalud centro(String nombre, String localidad, String provincia) {
        return new CentroSalud(nombre, "Calle 1", localidad, "00000", provincia, "111", "cs@cs.com");
    }

    public static Medico medico(String login, String numColegiado, String dni, String nombre, String apellidos) {
        return new Medico(login, numColegiado, nombre, apellidos, dni, numColegiado, "222", "m@m.com");
    }

    public static Paciente paciente(String login, String dni, String nts, String nss, String nombre, String apellidos) {
        Paciente p = new Paciente(login, dni, nombre, apellidos, dni, nts, nss);
        p.setLocalidad("Vigo");
        p.setProvincia("Pontevedra");
        p.setDomicilio("Calle P");
        p.setCodigoPostal("36201");
        p.setEmail("p@p.com");
        p.setTelefono("333");
        p.setFechaNacimiento(new Date());
        return p;
    }

    public static Medicamento medicamento(String nombreComercial, String principio, String fabricante, String familia, int numeroDosis) {
        return new Medicamento(nombreComercial, principio, fabricante, familia, numeroDosis);
    }

    public static Farmacia farmacia(String login, String numColegiado, String nombreEst) {
        Farmacia f = new Farmacia(login, numColegiado, nombreEst, "Fran", "Farm", "X123", numColegiado);
        f.setLocalidad("Vigo");
        f.setProvincia("Pontevedra");
        f.setDomicilio("Calle F");
        f.setCodigoPostal("36201");
        f.setEmail("f@f.com");
        f.setTelefono("444");
        return f;
    }

    public static Administrador admin(String login, String pass) {
        return new Administrador(login, pass, "Admin", "admin@a.com");
    }

    public static Prescripcion prescripcionBasica(Medico medico, Paciente paciente, Medicamento medicamento,
                                                  double dosisDiaria, LocalDate inicio, LocalDate fin) {
        Prescripcion p = new Prescripcion();
        p.setMedico(medico);
        p.setPaciente(paciente);
        p.setMedicamento(medicamento);
        p.setDosisDiaria(dosisDiaria);
        p.setIndicaciones("test");
        p.setFechaInicio(inicio);
        p.setFechaFin(fin);
        p.setActiva(true);
        return p;
    }
}
