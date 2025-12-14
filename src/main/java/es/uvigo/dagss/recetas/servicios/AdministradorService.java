package es.uvigo.dagss.recetas.servicios;

import es.uvigo.dagss.recetas.entidades.Administrador;
import es.uvigo.dagss.recetas.repositorios.AdministradorRepository;
import es.uvigo.dagss.recetas.repositorios.UsuarioRepository;
import es.uvigo.dagss.recetas.servicios.excepciones.RecursoNoEncontradoException;
import es.uvigo.dagss.recetas.servicios.excepciones.ValidacionException;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdministradorService {

    private final AdministradorRepository administradorRepository;
    private final UsuarioRepository usuarioRepository;

    public AdministradorService(AdministradorRepository administradorRepository,
                                UsuarioRepository usuarioRepository) {
        this.administradorRepository = administradorRepository;
        this.usuarioRepository = usuarioRepository;
    }

    /** HU-A2: listado de administradores activos */
    @Transactional(readOnly = true)
    public List<Administrador> listarActivos() {
        return administradorRepository.findByActivoTrueOrderByLoginAsc();
    }

    /** HU-A2: alta de administrador */
    @Transactional
    public Administrador crear(String login, String password, String nombre, String email) {
        if (login == null || login.isBlank()) throw new ValidacionException("login obligatorio");
        if (password == null || password.isBlank()) throw new ValidacionException("password obligatorio");
        if (usuarioRepository.existsByLogin(login.trim())) {
            throw new ValidacionException("Ya existe un usuario con ese login");
        }

        Administrador a = new Administrador();
        a.setLogin(login.trim());
        a.setPassword(password);
        a.setNombre(nombre);
        a.setEmail(email);
        a.setActivo(true);

        return administradorRepository.save(a);
    }

    /** HU-A2: modificación de administrador (sin cambiar login/password salvo que lo haga el propio usuario en perfil) */
    @Transactional
    public Administrador actualizar(Long id, String nombre, String email, Boolean activo) {
        Administrador a = administradorRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Administrador no encontrado: " + id));

        a.setNombre(nombre);
        a.setEmail(email);
        if (activo != null) a.setActivo(activo);

        return administradorRepository.save(a);
    }

    /** HU-A2: baja lógica */
    @Transactional
    public void bajaLogica(Long id) {
        Administrador a = administradorRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Administrador no encontrado: " + id));
        a.setActivo(false);
        administradorRepository.save(a);
    }
}
