package es.uvigo.dagss.recetas.servicios;

import es.uvigo.dagss.recetas.entidades.Administrador;
import es.uvigo.dagss.recetas.repositorios.AdministradorDAO;
import es.uvigo.dagss.recetas.repositorios.UsuarioDAO;
import es.uvigo.dagss.recetas.servicios.excepciones.RecursoNoEncontradoException;
import es.uvigo.dagss.recetas.servicios.excepciones.ValidacionException;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdministradorService {

    private final AdministradorDAO administradorDAO;
    private final UsuarioDAO usuarioDAO;

    public AdministradorService(AdministradorDAO administradorRepository,
                                UsuarioDAO usuarioRepository) {
        this.administradorDAO = administradorRepository;
        this.usuarioDAO = usuarioRepository;
    }

    /** HU-A2: listado de administradores activos */
    @Transactional(readOnly = true)
    public List<Administrador> listarActivos() {
        return administradorDAO.findByActivoTrueOrderByLoginAsc();
    }

    /** HU-A2: alta de administrador */
    @Transactional
    public Administrador crear(String login, String password, String nombre, String email) {
        if (login == null || login.isBlank()) throw new ValidacionException("login obligatorio");
        if (password == null || password.isBlank()) throw new ValidacionException("password obligatorio");
        if (usuarioDAO.existsByLogin(login.trim())) {
            throw new ValidacionException("Ya existe un usuario con ese login");
        }

        Administrador a = new Administrador();
        a.setLogin(login.trim());
        a.setPassword(password);
        a.setNombre(nombre);
        a.setEmail(email);
        a.setActivo(true);

        return administradorDAO.save(a);
    }

    /** HU-A2: modificación de administrador  */
    @Transactional
    public Administrador actualizar(Long id, String nombre, String email, Boolean activo) {
        Administrador a = administradorDAO.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Administrador no encontrado: " + id));

        a.setNombre(nombre);
        a.setEmail(email);
        if (activo != null) a.setActivo(activo);

        return administradorDAO.save(a);
    }

    /** HU-A2: baja  */
    @Transactional
    public void bajaLogica(Long id) {
        Administrador a = administradorDAO.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Administrador no encontrado: " + id));
        a.setActivo(false);
        administradorDAO.save(a);
    }
}
