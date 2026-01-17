package es.uvigo.dagss.recetas.controladores;

import es.uvigo.dagss.recetas.controladores.dto.LoginRequest;
import es.uvigo.dagss.recetas.controladores.dto.LoginResponse;
import es.uvigo.dagss.recetas.entidades.Usuario;
import es.uvigo.dagss.recetas.repositorios.UsuarioDAO;
import es.uvigo.dagss.recetas.servicios.excepciones.CredencialesInvalidasException;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

/**
 * HU-C1 (simplificado): login por login/password.
 *
 * Nota: esto NO implementa seguridad (sesiones/JWT). Sólo valida credenciales y devuelve el rol.
 */
@RestController
@RequestMapping(path = "/api", produces = MediaType.APPLICATION_JSON_VALUE)
public class AuthController {

    private final UsuarioDAO usuarioDAO;

    public AuthController(UsuarioDAO usuarioDAO) {
        this.usuarioDAO = usuarioDAO;
    }

    @PostMapping(path = "/login", consumes = MediaType.APPLICATION_JSON_VALUE)
    public LoginResponse login(@RequestBody LoginRequest req) {
        if (req == null || req.login == null || req.login.isBlank() || req.password == null || req.password.isBlank()) {
            throw new CredencialesInvalidasException("Credenciales inválidas");
        }

        Usuario u = usuarioDAO.findActivoByLogin(req.login.trim())
                .orElseThrow(() -> new CredencialesInvalidasException("Credenciales inválidas"));

        if (!req.password.equals(u.getPassword())) {
            throw new CredencialesInvalidasException("Credenciales inválidas");
        }

        u.setUltimoAcceso(java.util.Date.from(java.time.ZonedDateTime.now().toInstant()));
        usuarioDAO.save(u);

        return new LoginResponse(u.getId(), u.getTipo(), u.getLogin());
    }
}
