package es.uvigo.dagss.recetas.servicios;

import es.uvigo.dagss.recetas.entidades.CentroSalud;
import es.uvigo.dagss.recetas.entidades.Medico;
import es.uvigo.dagss.recetas.repositorios.CentroSaludDAO;
import es.uvigo.dagss.recetas.repositorios.MedicoDAO;
import es.uvigo.dagss.recetas.repositorios.UsuarioDAO;
import es.uvigo.dagss.recetas.servicios.excepciones.RecursoNoEncontradoException;
import es.uvigo.dagss.recetas.servicios.excepciones.ValidacionException;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MedicoService {

    private final MedicoDAO medicoRepository;
    private final CentroSaludDAO centroSaludRepository;
    private final UsuarioDAO usuarioRepository;

    public MedicoService(MedicoDAO medicoRepository,
                         CentroSaludDAO centroSaludRepository,
                         UsuarioDAO usuarioRepository) {
        this.medicoRepository = medicoRepository;
        this.centroSaludRepository = centroSaludRepository;
        this.usuarioRepository = usuarioRepository;
    }

    /** HU-A4: listado */
    @Transactional(readOnly = true)
    public List<Medico> listarActivos() {
        return medicoRepository.findByActivoTrueOrderByApellidosAscNombreAsc();
    }

    /** HU-A4: búsqueda por nombre/localidad/centro */
    @Transactional(readOnly = true)
    public List<Medico> buscarActivos(String nombre, String localidad, Long centroId) {
        String n = (nombre == null || nombre.isBlank()) ? null : nombre.trim();
        String l = (localidad == null || localidad.isBlank()) ? null : localidad.trim();
        return medicoRepository.buscarActivos(n, l, centroId);
    }

    /** HU-A5 (desplegable): médicos activos de un centro */
    @Transactional(readOnly = true)
    public List<Medico> medicosActivosDeCentro(Long centroId) {
        return medicoRepository.findActivosByCentro(centroId);
    }

    /** HU-A4: alta. Password inicial = nº colegiado */
    @Transactional
    public Medico crear(String login,
                        String nombre,
                        String apellidos,
                        String dni,
                        String numeroColegiado,
                        String telefono,
                        String email,
                        Long centroSaludId) {

        if (login == null || login.isBlank()) throw new ValidacionException("login obligatorio");
        if (numeroColegiado == null || numeroColegiado.isBlank()) throw new ValidacionException("nº colegiado obligatorio");
        if (centroSaludId == null) throw new ValidacionException("centroSaludId obligatorio");
        if (usuarioRepository.existsByLogin(login.trim())) throw new ValidacionException("Ya existe un usuario con ese login");

        CentroSalud cs = centroSaludRepository.findById(centroSaludId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Centro de salud no encontrado: " + centroSaludId));

        Medico m = new Medico();
        m.setLogin(login.trim());
        m.setPassword(numeroColegiado); // password inicial
        m.setNombre(nombre);
        m.setApellidos(apellidos);
        m.setDni(dni);
        m.setNumeroColegiado(numeroColegiado);
        m.setTelefono(telefono);
        m.setEmail(email);
        m.setCentroSalud(cs);
        m.setActivo(true);

        return medicoRepository.save(m);
    }

    /** HU-A4: edición (incluye cambio de centro) */
    @Transactional
    public Medico actualizarPorAdmin(Long id,
                                    String nombre,
                                    String apellidos,
                                    String dni,
                                    String numeroColegiado,
                                    String telefono,
                                    String email,
                                    Long centroSaludId,
                                    Boolean activo) {

        Medico m = medicoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Médico no encontrado: " + id));

        if (centroSaludId != null) {
            CentroSalud cs = centroSaludRepository.findById(centroSaludId)
                    .orElseThrow(() -> new RecursoNoEncontradoException("Centro de salud no encontrado: " + centroSaludId));
            m.setCentroSalud(cs);
        }

        m.setNombre(nombre);
        m.setApellidos(apellidos);
        m.setDni(dni);
        m.setNumeroColegiado(numeroColegiado);
        m.setTelefono(telefono);
        m.setEmail(email);
        if (activo != null) m.setActivo(activo);

        return medicoRepository.save(m);
    }

    /** WRAPPER: alta pasando Medico + centroId (para el controller REST) */
    @Transactional
    public Medico crear(Medico m, Long centroSaludId) {
        if (m == null) throw new ValidacionException("Body obligatorio");
        return crear(
                m.getLogin(),
                m.getNombre(),
                m.getApellidos(),
                m.getDni(),
                m.getNumeroColegiado(),
                m.getTelefono(),
                m.getEmail(),
                centroSaludId
        );
    }

    /** WRAPPER: edición admin pasando Medico + centroId */
    @Transactional
    public Medico actualizarPorAdmin(Long id, Medico cambios, Long centroSaludId) {
        if (cambios == null) throw new ValidacionException("Body obligatorio");
        return actualizarPorAdmin(
                id,
                cambios.getNombre(),
                cambios.getApellidos(),
                cambios.getDni(),
                cambios.getNumeroColegiado(),
                cambios.getTelefono(),
                cambios.getEmail(),
                centroSaludId,
                cambios.getActivo()
        );
    }

    /** HU-A4: baja lógica */
    @Transactional
    public void baja(Long id) {
        Medico m = medicoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Médico no encontrado: " + id));
        m.setActivo(false);
        medicoRepository.save(m);
    }

    /** HU-M6: perfil (no permite cambiar centro) */
    @Transactional
    public Medico actualizarPerfil(Long medicoId,
                                  String nuevaPassword,
                                  String nombre,
                                  String apellidos,
                                  String telefono,
                                  String email) {

        Medico m = medicoRepository.findById(medicoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Médico no encontrado: " + medicoId));

        if (nuevaPassword != null && !nuevaPassword.isBlank()) {
            m.setPassword(nuevaPassword);
        }

        if (nombre != null) m.setNombre(nombre);
        if (apellidos != null) m.setApellidos(apellidos);
        if (telefono != null) m.setTelefono(telefono);
        if (email != null) m.setEmail(email);

        return medicoRepository.save(m);
    }

    /** WRAPPER: perfil pasando Medico */
    @Transactional
    public Medico actualizarPerfil(Long medicoId, Medico cambios) {
        if (cambios == null) throw new ValidacionException("Body obligatorio");
        return actualizarPerfil(
                medicoId,
                cambios.getPassword(),
                cambios.getNombre(),
                cambios.getApellidos(),
                cambios.getTelefono(),
                cambios.getEmail()
        );
    }

    /** WRAPPER: cambiar solo password */
    @Transactional
    public void cambiarPassword(Long medicoId, String nuevaPassword) {
        if (nuevaPassword == null || nuevaPassword.isBlank()) {
            throw new ValidacionException("password obligatoria");
        }
        actualizarPerfil(medicoId, nuevaPassword, null, null, null, null);
    }
}
