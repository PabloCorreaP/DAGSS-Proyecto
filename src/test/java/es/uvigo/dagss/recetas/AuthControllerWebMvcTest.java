package es.uvigo.dagss.recetas;

import es.uvigo.dagss.recetas.controladores.AuthController;
import es.uvigo.dagss.recetas.controladores.RestExceptionHandler;
import es.uvigo.dagss.recetas.entidades.Administrador;
import es.uvigo.dagss.recetas.repositorios.UsuarioDAO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AuthController.class)
@Import(RestExceptionHandler.class)
class AuthControllerWebMvcTest {

    @Autowired MockMvc mvc;

    @MockBean UsuarioDAO usuarioDAO;

    @Test
    void login_ok_devuelve_rol_y_login() throws Exception {
        Administrador u = TestDataFactory.admin("admin", "pass");
        u.setId(1L);

        when(usuarioDAO.findActivoByLogin(eq("admin"))).thenReturn(Optional.of(u));
        when(usuarioDAO.save(any())).thenReturn(u);

        mvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"login\":\"admin\",\"password\":\"pass\"}"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.login").value("admin"))
                .andExpect(jsonPath("$.tipo").value("ADMINISTRADOR"));
    }

    @Test
    void login_sin_password_da_401() throws Exception {
        mvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"login\":\"admin\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Unauthorized"));
    }
}
