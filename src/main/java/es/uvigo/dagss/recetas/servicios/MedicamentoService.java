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

    @Transactional(readOnly = true)
public List<Medicamento> buscar(String nombre, String principioActivo, String fabricante, String familia) {
    String n = (nombre == null || nombre.isBlank()) ? null : nombre.trim().toLowerCase();
    String p = (principioActivo == null || principioActivo.isBlank()) ? null : principioActivo.trim().toLowerCase();
    String f = (fabricante == null || fabricante.isBlank()) ? null : fabricante.trim().toLowerCase();
    String fam = (familia == null || familia.isBlank()) ? null : familia.trim().toLowerCase();

    // Si no hay filtros, devuelvo activos
    List<Medicamento> base = listarActivos();

    return base.stream()
        .filter(m -> n == null || (m.getNombreComercial() != null && m.getNombreComercial().toLowerCase().contains(n)))
        .filter(m -> p == null || (m.getPrincipioActivo() != null && m.getPrincipioActivo().toLowerCase().contains(p)))
        .filter(m -> f == null || (m.getFabricante() != null && m.getFabricante().toLowerCase().contains(f)))
        .filter(m -> fam == null || (m.getFamilia() != null && m.getFamilia().toLowerCase().contains(fam)))
        .toList();
}

 
}
