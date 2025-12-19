package es.uvigo.dagss.recetas.repositorios;

import es.uvigo.dagss.recetas.entidades.Administrador;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
//* jspecificatioexecutor
// findcell(Specification<cliente> pageable)
// ver en ejemplo cliente controller ejemplo bucar todos */
public interface AdministradorDAO extends JpaRepository<Administrador, Long> {

    Optional<Administrador> findByLogin(String login);

    List<Administrador> findByActivoTrueOrderByLoginAsc();
}
