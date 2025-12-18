package es.uvigo.dagss.recetas.repositorios;

import es.uvigo.dagss.recetas.entidades.Usuario;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


public interface UsuarioDAO extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByLogin(String login);

    @Query("select u from Usuario u where u.login = :login and u.activo = true")
    Optional<Usuario> findActivoByLogin(@Param("login") String login);

    boolean existsByLogin(String login);
}
