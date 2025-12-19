package es.uvigo.dagss.recetas.servicios;

import es.uvigo.dagss.recetas.entidades.Medicamento;
import es.uvigo.dagss.recetas.repositorios.MedicamentoDAO;
import es.uvigo.dagss.recetas.servicios.excepciones.RecursoNoEncontradoException;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MedicamentoService {

    private final MedicamentoDAO medicamentoDAO;

    public MedicamentoService(MedicamentoDAO medicamentoDAO) {
        this.medicamentoDAO = medicamentoDAO;
    }

    /** HU-A8: listado */
    @Transactional(readOnly = true)
    public List<Medicamento> listarActivos() {
        return medicamentoDAO.findByActivoTrueOrderByNombreComercialAsc();
    }

    /** HU-A8  y HU-M4: buscador LIKE en nombre/principio/fabricante/familia */
    @Transactional(readOnly = true)
    public List<Medicamento> buscarActivos(String texto) {
        String t = (texto == null || texto.isBlank()) ? null : texto.trim();
        return medicamentoDAO.buscarActivosLike(t);
    }

    /** HU-A8: alta */
    @Transactional
    public Medicamento crear(Medicamento m) {
        m.setActivo(true);
        return medicamentoDAO.save(m);
    }

    /** HU-A8: edición */
    @Transactional
    public Medicamento actualizar(Long id, Medicamento datos) {
        Medicamento m = medicamentoDAO.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Medicamento no encontrado: " + id));

        m.setNombreComercial(datos.getNombreComercial());
        m.setPrincipioActivo(datos.getPrincipioActivo());
        m.setFabricante(datos.getFabricante());
        m.setFamilia(datos.getFamilia());
        m.setNumeroDosis(datos.getNumeroDosis());
        if (datos.getActivo() != null) m.setActivo(datos.getActivo());

        return medicamentoDAO.save(m);
    }

    /** HU-A8: baja  */
    @Transactional
    public void baja(Long id) {
        Medicamento m = medicamentoDAO.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Medicamento no encontrado: " + id));
        m.setActivo(false);
        medicamentoDAO.save(m);
    }

 
}
