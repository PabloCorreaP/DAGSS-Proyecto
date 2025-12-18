package es.uvigo.dagss.recetas.servicios;

import es.uvigo.dagss.recetas.entidades.Usuario;
import es.uvigo.dagss.recetas.repositorios.UsuarioDAO;
import es.uvigo.dagss.recetas.servicios.excepciones.CredencialesInvalidasException;
import java.util.Date;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AutenticacionService {

    private final UsuarioDAO usuarioDAO;

    public AutenticacionService(UsuarioDAO usuarioDAO) {
        this.usuarioDAO = usuarioDAO;
    }

    /**
     * HU-C1: Login
     */
    @Transactional
    public Usuario login(String login, String password) {
        if (login == null || login.isBlank() || password == null || password.isBlank()) {
            throw new CredencialesInvalidasException("Login y password son obligatorios");
        }

        Usuario u = usuarioDAO.findActivoByLogin(login.trim())
                .orElseThrow(() -> new CredencialesInvalidasException("Credenciales inválidas"));

        if (u.getPassword() == null || !u.getPassword().equals(password)) {
            throw new CredencialesInvalidasException("Credenciales inválidas");
        }

        u.setUltimoAcceso(new Date());
        return usuarioDAO.save(u);
    }
}
