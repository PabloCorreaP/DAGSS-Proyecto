package es.uvigo.dagss.recetas.servicios;

import es.uvigo.dagss.recetas.entidades.Usuario;
import es.uvigo.dagss.recetas.repositorios.UsuarioRepository;
import es.uvigo.dagss.recetas.servicios.excepciones.CredencialesInvalidasException;
import java.util.Date;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AutenticacionService {

    private final UsuarioRepository usuarioRepository;

    public AutenticacionService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    /**
     * HU-C1: Login
     *
     * @throws CredencialesInvalidasException si login/password no son válidos o el usuario está inactivo.
     */
    @Transactional
    public Usuario login(String login, String password) {
        if (login == null || login.isBlank() || password == null || password.isBlank()) {
            throw new CredencialesInvalidasException("Login y password son obligatorios");
        }

        Usuario u = usuarioRepository.findActivoByLogin(login.trim())
                .orElseThrow(() -> new CredencialesInvalidasException("Credenciales inválidas"));

        if (u.getPassword() == null || !u.getPassword().equals(password)) {
            throw new CredencialesInvalidasException("Credenciales inválidas");
        }

        u.setUltimoAcceso(new Date());
        return usuarioRepository.save(u);
    }
}
