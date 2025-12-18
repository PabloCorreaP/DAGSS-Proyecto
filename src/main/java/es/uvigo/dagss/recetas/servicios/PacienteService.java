package es.uvigo.dagss.recetas.servicios;

import es.uvigo.dagss.recetas.entidades.CentroSalud;
import es.uvigo.dagss.recetas.entidades.Medico;
import es.uvigo.dagss.recetas.entidades.Paciente;
import es.uvigo.dagss.recetas.repositorios.CentroSaludDAO;
import es.uvigo.dagss.recetas.repositorios.MedicoDAO;
import es.uvigo.dagss.recetas.repositorios.PacienteDAO;
import es.uvigo.dagss.recetas.repositorios.UsuarioDAO;
import es.uvigo.dagss.recetas.servicios.excepciones.OperacionNoPermitidaException;
import es.uvigo.dagss.recetas.servicios.excepciones.RecursoNoEncontradoException;
import es.uvigo.dagss.recetas.servicios.excepciones.ValidacionException;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PacienteService {

    private final PacienteDAO pacienteRepository;
    private final CentroSaludDAO centroSaludRepository;
    private final MedicoDAO medicoRepository;
    private final UsuarioDAO usuarioRepository;

    public PacienteService(PacienteDAO pacienteRepository,
                           CentroSaludDAO centroSaludRepository,
                           MedicoDAO medicoRepository,
                           UsuarioDAO usuarioRepository) {
        this.pacienteRepository = pacienteRepository;
        this.centroSaludRepository = centroSaludRepository;
        this.medicoRepository = medicoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    /** HU-A5: listado */
    @Transactional(readOnly = true)
    public List<Paciente> listarActivos() {
        return pacienteRepository.findByActivoTrueOrderByApellidosAscNombreAsc();
    }

    /** HU-A5: búsqueda por nombre/localidad/centro/médico */
    @Transactional(readOnly = true)
    public List<Paciente> buscarActivos(String nombre, String localidad, Long centroId, Long medicoId) {
        String n = (nombre == null || nombre.isBlank()) ? null : nombre.trim();
        String l = (localidad == null || localidad.isBlank()) ? null : localidad.trim();
        return pacienteRepository.buscarActivos(n, l, centroId, medicoId);
    }

    /** HU-A5: alta. Password inicial = DNI */
    @Transactional
    public Paciente crear(String login,
                          String nombre,
                          String apellidos,
                          String dni,
                          String numeroTarjetaSanitaria,
                          String numeroSeguridadSocial,
                          String domicilio,
                          String localidad,
                          String codigoPostal,
                          String provincia,
                          String telefono,
                          String email,
                          java.util.Date fechaNacimiento,
                          Long centroSaludId,
                          Long medicoId) {

        if (login == null || login.isBlank()) throw new ValidacionException("login obligatorio");
        if (dni == null || dni.isBlank()) throw new ValidacionException("dni obligatorio");
        if (usuarioRepository.existsByLogin(login.trim())) throw new ValidacionException("Ya existe un usuario con ese login");

        CentroSalud cs = centroSaludRepository.findById(centroSaludId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Centro de salud no encontrado: " + centroSaludId));

        Medico m = medicoRepository.findById(medicoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Médico no encontrado: " + medicoId));

        if (m.getCentroSalud() == null || cs.getId() == null || !cs.getId().equals(m.getCentroSalud().getId())) {
            throw new OperacionNoPermitidaException("El médico asignado debe pertenecer al centro de salud del paciente");
        }

        Paciente p = new Paciente();
        p.setLogin(login.trim());
        p.setPassword(dni); // password inicial
        p.setNombre(nombre);
        p.setApellidos(apellidos);
        p.setDni(dni);
        p.setNumeroTarjetaSanitaria(numeroTarjetaSanitaria);
        p.setNumeroSeguridadSocial(numeroSeguridadSocial);
        p.setDomicilio(domicilio);
        p.setLocalidad(localidad);
        p.setCodigoPostal(codigoPostal);
        p.setProvincia(provincia);
        p.setTelefono(telefono);
        p.setEmail(email);
        p.setFechaNacimiento(fechaNacimiento);
        p.setCentroSalud(cs);
        p.setMedicoAsignado(m);
        p.setActivo(true);

        return pacienteRepository.save(p);
    }

    /** HU-A5: edición por administrador (permite cambiar centro y médico, con coherencia centro-médico) */
    @Transactional
    public Paciente actualizarPorAdmin(Long id,
                                      String nombre,
                                      String apellidos,
                                      String dni,
                                      String numeroTarjetaSanitaria,
                                      String numeroSeguridadSocial,
                                      String domicilio,
                                      String localidad,
                                      String codigoPostal,
                                      String provincia,
                                      String telefono,
                                      String email,
                                      java.util.Date fechaNacimiento,
                                      Long centroSaludId,
                                      Long medicoId,
                                      Boolean activo) {

        Paciente p = pacienteRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Paciente no encontrado: " + id));

        CentroSalud cs = p.getCentroSalud();
        if (centroSaludId != null) {
            cs = centroSaludRepository.findById(centroSaludId)
                    .orElseThrow(() -> new RecursoNoEncontradoException("Centro de salud no encontrado: " + centroSaludId));
            p.setCentroSalud(cs);
        }

        if (medicoId != null) {
            Medico m = medicoRepository.findById(medicoId)
                    .orElseThrow(() -> new RecursoNoEncontradoException("Médico no encontrado: " + medicoId));

            if (m.getCentroSalud() == null || cs == null || cs.getId() == null ||
                !cs.getId().equals(m.getCentroSalud().getId())) {
                throw new OperacionNoPermitidaException("El médico asignado debe pertenecer al centro de salud del paciente");
            }
            p.setMedicoAsignado(m);
        }

        p.setNombre(nombre);
        p.setApellidos(apellidos);
        p.setDni(dni);
        p.setNumeroTarjetaSanitaria(numeroTarjetaSanitaria);
        p.setNumeroSeguridadSocial(numeroSeguridadSocial);
        p.setDomicilio(domicilio);
        p.setLocalidad(localidad);
        p.setCodigoPostal(codigoPostal);
        p.setProvincia(provincia);
        p.setTelefono(telefono);
        p.setEmail(email);
        p.setFechaNacimiento(fechaNacimiento);
        if (activo != null) p.setActivo(activo);

        return pacienteRepository.save(p);
    }

    /** HU-A5: baja lógica */
    @Transactional
    public void bajaLogica(Long id) {
        Paciente p = pacienteRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Paciente no encontrado: " + id));
        p.setActivo(false);
        pacienteRepository.save(p);
    }

    /** HU-P5: perfil (no permite cambiar centro/medico) */
    @Transactional
    public Paciente actualizarPerfil(Long pacienteId,
                                    String nuevaPassword,
                                    String nombre,
                                    String apellidos,
                                    String domicilio,
                                    String localidad,
                                    String codigoPostal,
                                    String provincia,
                                    String telefono,
                                    String email) {

        Paciente p = pacienteRepository.findById(pacienteId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Paciente no encontrado: " + pacienteId));

        if (nuevaPassword != null && !nuevaPassword.isBlank()) {
            p.setPassword(nuevaPassword);
        }

        if (nombre != null) p.setNombre(nombre);
        if (apellidos != null) p.setApellidos(apellidos);
        if (domicilio != null) p.setDomicilio(domicilio);
        if (localidad != null) p.setLocalidad(localidad);
        if (codigoPostal != null) p.setCodigoPostal(codigoPostal);
        if (provincia != null) p.setProvincia(provincia);
        if (telefono != null) p.setTelefono(telefono);
        if (email != null) p.setEmail(email);

        return pacienteRepository.save(p);
    }

    @Transactional(readOnly = true)
    public Paciente getOrThrow(Long id) {
        return pacienteRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Paciente no encontrado: " + id));
    }
}
