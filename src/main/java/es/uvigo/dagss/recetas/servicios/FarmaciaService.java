package es.uvigo.dagss.recetas.servicios;

import es.uvigo.dagss.recetas.entidades.Farmacia;
import es.uvigo.dagss.recetas.repositorios.FarmaciaRepository;
import es.uvigo.dagss.recetas.repositorios.UsuarioRepository;
import es.uvigo.dagss.recetas.servicios.excepciones.RecursoNoEncontradoException;
import es.uvigo.dagss.recetas.servicios.excepciones.ValidacionException;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FarmaciaService {

    private final FarmaciaRepository farmaciaRepository;
    private final UsuarioRepository usuarioRepository;

    public FarmaciaService(FarmaciaRepository farmaciaRepository,
                           UsuarioRepository usuarioRepository) {
        this.farmaciaRepository = farmaciaRepository;
        this.usuarioRepository = usuarioRepository;
    }

    /** HU-A6: listado */
    @Transactional(readOnly = true)
    public List<Farmacia> listarActivas() {
        return farmaciaRepository.findByActivoTrueOrderByNombreEstablecimientoAsc();
    }

    /** HU-A6: búsqueda por nombre/localidad (LIKE) */
    @Transactional(readOnly = true)
    public List<Farmacia> buscarActivas(String texto) {
        String t = (texto == null || texto.isBlank()) ? null : texto.trim();
        return farmaciaRepository.buscarActivasPorNombreEstablecimientoOLocalidadLike(t);
    }

    /** HU-A6: alta. Password inicial = nº colegiado farmaceútico */
    @Transactional
    public Farmacia crear(String login,
                          String nombreEstablecimiento,
                          String nombreFarmaceutico,
                          String apellidosFarmaceutico,
                          String nif,
                          String numeroColegiadoFarmaceutico,
                          String domicilio,
                          String localidad,
                          String codigoPostal,
                          String provincia,
                          String telefono,
                          String email) {

        if (login == null || login.isBlank()) throw new ValidacionException("login obligatorio");
        if (numeroColegiadoFarmaceutico == null || numeroColegiadoFarmaceutico.isBlank()) {
            throw new ValidacionException("nº colegiado obligatorio");
        }
        if (usuarioRepository.existsByLogin(login.trim())) throw new ValidacionException("Ya existe un usuario con ese login");

        Farmacia f = new Farmacia();
        f.setLogin(login.trim());
        f.setPassword(numeroColegiadoFarmaceutico); // password inicial
        f.setNombreEstablecimiento(nombreEstablecimiento);
        f.setNombreFarmaceutico(nombreFarmaceutico);
        f.setApellidosFarmaceutico(apellidosFarmaceutico);
        f.setNif(nif);
        f.setNumeroColegiadoFarmaceutico(numeroColegiadoFarmaceutico);
        f.setDomicilio(domicilio);
        f.setLocalidad(localidad);
        f.setCodigoPostal(codigoPostal);
        f.setProvincia(provincia);
        f.setTelefono(telefono);
        f.setEmail(email);
        f.setActivo(true);

        return farmaciaRepository.save(f);
    }

    /** HU-A6: edición por administrador */
    @Transactional
    public Farmacia actualizarPorAdmin(Long id,
                                      String nombreEstablecimiento,
                                      String nombreFarmaceutico,
                                      String apellidosFarmaceutico,
                                      String nif,
                                      String numeroColegiadoFarmaceutico,
                                      String domicilio,
                                      String localidad,
                                      String codigoPostal,
                                      String provincia,
                                      String telefono,
                                      String email,
                                      Boolean activo) {

        Farmacia f = farmaciaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Farmacia no encontrada: " + id));

        f.setNombreEstablecimiento(nombreEstablecimiento);
        f.setNombreFarmaceutico(nombreFarmaceutico);
        f.setApellidosFarmaceutico(apellidosFarmaceutico);
        f.setNif(nif);
        f.setNumeroColegiadoFarmaceutico(numeroColegiadoFarmaceutico);
        f.setDomicilio(domicilio);
        f.setLocalidad(localidad);
        f.setCodigoPostal(codigoPostal);
        f.setProvincia(provincia);
        f.setTelefono(telefono);
        f.setEmail(email);
        if (activo != null) f.setActivo(activo);

        return farmaciaRepository.save(f);
    }

    /** HU-A6: baja lógica */
    @Transactional
    public void bajaLogica(Long id) {
        Farmacia f = farmaciaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Farmacia no encontrada: " + id));
        f.setActivo(false);
        farmaciaRepository.save(f);
    }

    /** HU-F4: perfil (cambia password + datos básicos, no toca login) */
    @Transactional
    public Farmacia actualizarPerfil(Long farmaciaId,
                                    String nuevaPassword,
                                    String nombreEstablecimiento,
                                    String domicilio,
                                    String localidad,
                                    String codigoPostal,
                                    String provincia,
                                    String telefono,
                                    String email) {

        Farmacia f = farmaciaRepository.findById(farmaciaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Farmacia no encontrada: " + farmaciaId));

        if (nuevaPassword != null && !nuevaPassword.isBlank()) {
            f.setPassword(nuevaPassword);
        }
        if (nombreEstablecimiento != null) f.setNombreEstablecimiento(nombreEstablecimiento);
        if (domicilio != null) f.setDomicilio(domicilio);
        if (localidad != null) f.setLocalidad(localidad);
        if (codigoPostal != null) f.setCodigoPostal(codigoPostal);
        if (provincia != null) f.setProvincia(provincia);
        if (telefono != null) f.setTelefono(telefono);
        if (email != null) f.setEmail(email);

        return farmaciaRepository.save(f);
    }

    @Transactional(readOnly = true)
    public Farmacia getOrThrow(Long id) {
        return farmaciaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Farmacia no encontrada: " + id));
    }
}
