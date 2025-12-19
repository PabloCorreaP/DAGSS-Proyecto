package es.uvigo.dagss.recetas.servicios;

import es.uvigo.dagss.recetas.entidades.CentroSalud;
import es.uvigo.dagss.recetas.repositorios.CentroSaludDAO;
import es.uvigo.dagss.recetas.servicios.excepciones.RecursoNoEncontradoException;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CentroSaludService {

    private final CentroSaludDAO centroSaludRepository;

    public CentroSaludService(CentroSaludDAO centroSaludDAO) {
        this.centroSaludRepository = centroSaludDAO;
    }

    /** HU-A3: listado */
    @Transactional(readOnly = true)
    public List<CentroSalud> listarActivos() {
        return centroSaludRepository.findByActivoTrueOrderByNombreAsc();
    }

    /** HU-A3: búsqueda nombre o localidad, LIKE */
    @Transactional(readOnly = true)
    public List<CentroSalud> buscarActivos(String texto) {
        String t = (texto == null || texto.isBlank()) ? null : texto.trim();
        return centroSaludRepository.buscarActivosPorNombreOLocalidadLike(t);
    }

    /** Para HU-A5: centros activos de una provincia */
    @Transactional(readOnly = true)
    public List<CentroSalud> centrosActivosDeProvincia(String provincia) {
        return centroSaludRepository.findByActivoTrueAndProvinciaIgnoreCaseOrderByLocalidadAscNombreAsc(provincia);
    }

    /** HU-A3: alta */
    @Transactional
    public CentroSalud crear(CentroSalud c) {
        c.setActivo(true);
        return centroSaludRepository.save(c);
    }

    /** HU-A3: modificación */
    @Transactional
    public CentroSalud actualizar(Long id, CentroSalud datos) {
        CentroSalud c = centroSaludRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Centro de salud no encontrado: " + id));

        c.setNombre(datos.getNombre());
        c.setDomicilio(datos.getDomicilio());
        c.setLocalidad(datos.getLocalidad());
        c.setCodigoPostal(datos.getCodigoPostal());
        c.setProvincia(datos.getProvincia());
        c.setTelefono(datos.getTelefono());
        c.setEmail(datos.getEmail());
        if (datos.getActivo() != null) c.setActivo(datos.getActivo());

        return centroSaludRepository.save(c);
    }

    /** HU-A3: baja  */
    @Transactional
    public void baja(Long id) {
        CentroSalud c = centroSaludRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Centro de salud no encontrado: " + id));
        c.setActivo(false);
        centroSaludRepository.save(c);
    }
}
