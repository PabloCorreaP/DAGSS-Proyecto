package es.uvigo.dagss.recetas.servicios;

import es.uvigo.dagss.recetas.entidades.CentroSalud;
import es.uvigo.dagss.recetas.entidades.Medico;
import es.uvigo.dagss.recetas.repositorios.CentroSaludDAO;
import es.uvigo.dagss.recetas.repositorios.MedicoDAO;
import es.uvigo.dagss.recetas.repositorios.UsuarioDAO;
import es.uvigo.dagss.recetas.servicios.excepciones.OperacionNoPermitidaException;
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

    /** HU-A4: baja lógica */
    @Transactional
    public void bajaLogica(Long id) {
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

        // datos personales
        if (nombre != null) m.setNombre(nombre);
        if (apellidos != null) m.setApellidos(apellidos);
        if (telefono != null) m.setTelefono(telefono);
        if (email != null) m.setEmail(email);

        // Prohibido tocar centro desde perfil
        // (si alguien intentase por el controlador, mejor fallar aquí)
        // No hay parámetro de centro, así que estamos bien.

        return medicoRepository.save(m);
    }

    /** Utilidad: obtener médico o lanzar excepción */
    @Transactional(readOnly = true)
    public Medico getOrThrow(Long id) {
        return medicoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Médico no encontrado: " + id));
    }
}
