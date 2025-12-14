package es.uvigo.dagss.recetas.servicios;

import es.uvigo.dagss.recetas.entidades.Medicamento;
import es.uvigo.dagss.recetas.repositorios.MedicamentoRepository;
import es.uvigo.dagss.recetas.servicios.excepciones.RecursoNoEncontradoException;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MedicamentoService {

    private final MedicamentoRepository medicamentoRepository;

    public MedicamentoService(MedicamentoRepository medicamentoRepository) {
        this.medicamentoRepository = medicamentoRepository;
    }

    /** HU-A8: listado */
    @Transactional(readOnly = true)
    public List<Medicamento> listarActivos() {
        return medicamentoRepository.findByActivoTrueOrderByNombreComercialAsc();
    }

    /** HU-A8 / HU-M4: buscador (LIKE en nombre/principio/fabricante/familia) */
    @Transactional(readOnly = true)
    public List<Medicamento> buscarActivos(String texto) {
        String t = (texto == null || texto.isBlank()) ? null : texto.trim();
        return medicamentoRepository.buscarActivosLike(t);
    }

    /** HU-A8: alta */
    @Transactional
    public Medicamento crear(Medicamento m) {
        m.setActivo(true);
        return medicamentoRepository.save(m);
    }

    /** HU-A8: edición */
    @Transactional
    public Medicamento actualizar(Long id, Medicamento datos) {
        Medicamento m = medicamentoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Medicamento no encontrado: " + id));

        m.setNombreComercial(datos.getNombreComercial());
        m.setPrincipioActivo(datos.getPrincipioActivo());
        m.setFabricante(datos.getFabricante());
        m.setFamilia(datos.getFamilia());
        m.setNumeroDosis(datos.getNumeroDosis());
        if (datos.getActivo() != null) m.setActivo(datos.getActivo());

        return medicamentoRepository.save(m);
    }

    /** HU-A8: baja lógica */
    @Transactional
    public void bajaLogica(Long id) {
        Medicamento m = medicamentoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Medicamento no encontrado: " + id));
        m.setActivo(false);
        medicamentoRepository.save(m);
    }

    @Transactional(readOnly = true)
    public Medicamento getOrThrow(Long id) {
        return medicamentoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Medicamento no encontrado: " + id));
    }
}
