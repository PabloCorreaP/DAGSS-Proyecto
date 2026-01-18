package es.uvigo.dagss.recetas;

import es.uvigo.dagss.recetas.controladores.PrescripcionesController;
import es.uvigo.dagss.recetas.controladores.RestExceptionHandler;
import es.uvigo.dagss.recetas.entidades.Prescripcion;
import es.uvigo.dagss.recetas.servicios.PrescripcionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = PrescripcionesController.class)
@Import(RestExceptionHandler.class)
class PrescripcionesControllerWebMvcTest {

    @Autowired MockMvc mvc;

    @MockBean PrescripcionService prescripcionService;

    @Test
    void crear_sin_campos_obligatorios_da_400() throws Exception {
        mvc.perform(post("/api/prescripciones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"medicoId\":1}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"));
    }

    @Test
    void crear_ok_devuelve_201_y_location() throws Exception {
        Prescripcion p = new Prescripcion();
        p.setId(5L);
        p.setFechaInicio(LocalDate.now());
        p.setFechaFin(LocalDate.now().plusDays(10));
        p.setActiva(true);

        when(prescripcionService.crearPrescripcion(eq(1L), eq(2L), eq(3L), eq(1.0), any(), any()))
                .thenReturn(p);

        mvc.perform(post("/api/prescripciones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"medicoId\":1,\"pacienteId\":2,\"medicamentoId\":3,\"dosisDiaria\":1.0,\"indicaciones\":\"\",\"fechaFin\":\"2030-01-01\"}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", org.hamcrest.Matchers.containsString("/api/prescripciones/5")))
                .andExpect(jsonPath("$.id").value(5));
    }

    @Test
    void patch_activa_false_llama_a_service_y_devuelve_204() throws Exception {
        mvc.perform(patch("/api/prescripciones/7")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"activa\":false,\"medicoId\":99}"))
                .andExpect(status().isNoContent());

        verify(prescripcionService).anularPrescripcion(7L, 99L);
    }

    @Test
    void patch_activa_true_no_soportado_da_400() throws Exception {
        mvc.perform(patch("/api/prescripciones/7")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"activa\":true}"))
                .andExpect(status().isBadRequest());
    }
}
