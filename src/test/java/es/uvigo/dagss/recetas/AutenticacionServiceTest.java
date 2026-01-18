package es.uvigo.dagss.recetas;

import es.uvigo.dagss.recetas.entidades.Administrador;
import es.uvigo.dagss.recetas.repositorios.UsuarioDAO;
import es.uvigo.dagss.recetas.servicios.AutenticacionService;
import es.uvigo.dagss.recetas.servicios.excepciones.CredencialesInvalidasException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@ActiveProfiles("test")
@Import(AutenticacionService.class)
class AutenticacionServiceTest {

    @Autowired UsuarioDAO usuarioDAO;
    @Autowired AutenticacionService autenticacionService;

    @Test
    void login_ok_actualiza_ultimoAcceso() {
        Administrador a = usuarioDAO.save(TestDataFactory.admin("admin", "pass"));

        var res = autenticacionService.login("admin", "pass");

        assertThat(res.getId()).isEqualTo(a.getId());
        assertThat(res.getUltimoAcceso()).isNotNull();
    }

    @Test
    void login_password_incorrecta_lanza_excepcion() {
        usuarioDAO.save(TestDataFactory.admin("admin", "pass"));

        assertThatThrownBy(() -> autenticacionService.login("admin", "nope"))
                .isInstanceOf(CredencialesInvalidasException.class);
    }
}
