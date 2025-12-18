package es.uvigo.dagss.recetas.controladores;

import es.uvigo.dagss.recetas.entidades.Administrador;
import es.uvigo.dagss.recetas.entidades.Farmacia;
import es.uvigo.dagss.recetas.entidades.Medico;
import es.uvigo.dagss.recetas.entidades.Paciente;
import es.uvigo.dagss.recetas.servicios.AdministradorService;
import es.uvigo.dagss.recetas.servicios.FarmaciaService;
import es.uvigo.dagss.recetas.servicios.MedicoService;
import es.uvigo.dagss.recetas.servicios.PacienteService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminUsuariosController {

    private final AdministradorService administradorService;
    private final MedicoService medicoService;
    private final PacienteService pacienteService;
    private final FarmaciaService farmaciaService;

    public AdminUsuariosController(AdministradorService administradorService,
                                   MedicoService medicoService,
                                   PacienteService pacienteService,
                                   FarmaciaService farmaciaService) {
        this.administradorService = administradorService;
        this.medicoService = medicoService;
        this.pacienteService = pacienteService;
        this.farmaciaService = farmaciaService;
    }


    @GetMapping("/administradores")
    public List<Administrador> listarAdministradoresActivos() {
        return administradorService.listarActivos();
    }

    @PostMapping("/administradores")
    public ResponseEntity<Administrador> crearAdministrador(@RequestBody Administrador a) {
        Administrador creado = administradorService.crear(
                a.getLogin(),
                a.getPassword(),
                a.getNombre(),
                a.getEmail()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @PutMapping("/administradores/{id}")
    public Administrador actualizarAdministrador(@PathVariable Long id, @RequestBody Administrador cambios) {
        return administradorService.actualizar(
                id,
                cambios.getNombre(),
                cambios.getEmail(),
                cambios.getActivo()
        );
    }

    @DeleteMapping("/administradores/{id}")
    public ResponseEntity<Void> bajaAdministrador(@PathVariable Long id) {
        administradorService.bajaLogica(id);
        return ResponseEntity.noContent().build();
    }


    @GetMapping("/medicos")
    public List<Medico> listarMedicosActivos(@RequestParam(required = false) String nombre,
                                             @RequestParam(required = false) String localidad,
                                             @RequestParam(required = false) Long centroId) {

        if ((nombre == null || nombre.isBlank())
                && (localidad == null || localidad.isBlank())
                && centroId == null) {
            return medicoService.listarActivos();
        }

        String n = (nombre == null || nombre.isBlank()) ? null : nombre;
        String l = (localidad == null || localidad.isBlank()) ? null : localidad;

        return medicoService.buscarActivos(n, l, centroId);
    }

    @PostMapping("/medicos")
    public ResponseEntity<Medico> crearMedico(@RequestBody Medico m) {
        Long csId = (m.getCentroSalud() != null) ? m.getCentroSalud().getId() : null;

        Medico creado = medicoService.crear(
                m.getLogin(),
                m.getNombre(),
                m.getApellidos(),
                m.getDni(),
                m.getNumeroColegiado(),
                m.getTelefono(),
                m.getEmail(),
                csId
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @PutMapping("/medicos/{id}")
    public Medico actualizarMedico(@PathVariable Long id, @RequestBody Medico cambios) {
        Long csId = (cambios.getCentroSalud() != null) ? cambios.getCentroSalud().getId() : null;

        return medicoService.actualizarPorAdmin(
                id,
                cambios.getNombre(),
                cambios.getApellidos(),
                cambios.getDni(),
                cambios.getNumeroColegiado(),
                cambios.getTelefono(),
                cambios.getEmail(),
                csId,
                cambios.getActivo()
        );
    }

    @DeleteMapping("/medicos/{id}")
    public ResponseEntity<Void> bajaMedico(@PathVariable Long id) {
        medicoService.bajaLogica(id);
        return ResponseEntity.noContent().build();
    }

    // ---- Pacientes ----

    @GetMapping("/pacientes")
    public List<Paciente> listarPacientesActivos(@RequestParam(required = false) String nombre,
                                                 @RequestParam(required = false) String localidad,
                                                 @RequestParam(required = false) Long centroId,
                                                 @RequestParam(required = false) Long medicoId) {

        if ((nombre == null || nombre.isBlank())
                && (localidad == null || localidad.isBlank())
                && centroId == null
                && medicoId == null) {
            return pacienteService.listarActivos();
        }

        String n = (nombre == null || nombre.isBlank()) ? null : nombre;
        String l = (localidad == null || localidad.isBlank()) ? null : localidad;

        return pacienteService.buscarActivos(n, l, centroId, medicoId);
    }

    @PostMapping("/pacientes")
    public ResponseEntity<Paciente> crearPaciente(@RequestBody Paciente p) {
        Long csId = (p.getCentroSalud() != null) ? p.getCentroSalud().getId() : null;
        Long medId = (p.getMedicoAsignado() != null) ? p.getMedicoAsignado().getId() : null;

        Paciente creado = pacienteService.crear(
                p.getLogin(),
                p.getNombre(),
                p.getApellidos(),
                p.getDni(),
                p.getNumeroTarjetaSanitaria(),
                p.getNumeroSeguridadSocial(),
                p.getDomicilio(),
                p.getLocalidad(),
                p.getCodigoPostal(),
                p.getProvincia(),
                p.getTelefono(),
                p.getEmail(),
                p.getFechaNacimiento(),
                csId,
                medId
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @PutMapping("/pacientes/{id}")
    public Paciente actualizarPaciente(@PathVariable Long id, @RequestBody Paciente cambios) {
        Long csId = (cambios.getCentroSalud() != null) ? cambios.getCentroSalud().getId() : null;
        Long medId = (cambios.getMedicoAsignado() != null) ? cambios.getMedicoAsignado().getId() : null;

        return pacienteService.actualizarPorAdmin(
                id,
                cambios.getNombre(),
                cambios.getApellidos(),
                cambios.getDni(),
                cambios.getNumeroTarjetaSanitaria(),
                cambios.getNumeroSeguridadSocial(),
                cambios.getDomicilio(),
                cambios.getLocalidad(),
                cambios.getCodigoPostal(),
                cambios.getProvincia(),
                cambios.getTelefono(),
                cambios.getEmail(),
                cambios.getFechaNacimiento(),
                csId,
                medId,
                cambios.getActivo()
        );
    }

    @DeleteMapping("/pacientes/{id}")
    public ResponseEntity<Void> bajaPaciente(@PathVariable Long id) {
        pacienteService.bajaLogica(id);
        return ResponseEntity.noContent().build();
    }


    @GetMapping("/farmacias")
    public List<Farmacia> listarFarmaciasActivas(@RequestParam(required = false) String texto) {
        return (texto == null || texto.isBlank())
                ? farmaciaService.listarActivas()
                : farmaciaService.buscarActivas(texto);
    }

    @PostMapping("/farmacias")
    public ResponseEntity<Farmacia> crearFarmacia(@RequestBody Farmacia f) {
        Farmacia creada = farmaciaService.crear(
                f.getLogin(),
                f.getNombreEstablecimiento(),
                f.getNombreFarmaceutico(),
                f.getApellidosFarmaceutico(),
                f.getNif(),
                f.getNumeroColegiadoFarmaceutico(),
                f.getDomicilio(),
                f.getLocalidad(),
                f.getCodigoPostal(),
                f.getProvincia(),
                f.getTelefono(),
                f.getEmail()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(creada);
    }

    @PutMapping("/farmacias/{id}")
    public Farmacia actualizarFarmacia(@PathVariable Long id, @RequestBody Farmacia cambios) {
        return farmaciaService.actualizarPorAdmin(
                id,
                cambios.getNombreEstablecimiento(),
                cambios.getNombreFarmaceutico(),
                cambios.getApellidosFarmaceutico(),
                cambios.getNif(),
                cambios.getNumeroColegiadoFarmaceutico(),
                cambios.getDomicilio(),
                cambios.getLocalidad(),
                cambios.getCodigoPostal(),
                cambios.getProvincia(),
                cambios.getTelefono(),
                cambios.getEmail(),
                cambios.getActivo()
        );
    }

    @DeleteMapping("/farmacias/{id}")
    public ResponseEntity<Void> bajaFarmacia(@PathVariable Long id) {
        farmaciaService.bajaLogica(id);
        return ResponseEntity.noContent().build();
    }
}
