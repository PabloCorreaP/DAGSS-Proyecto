package es.uvigo.dagss.recetas.repositorios;

import es.uvigo.dagss.recetas.entidades.Usuario;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Repositorio base para login y operaciones comunes sobre usuarios.
 * Con {@code InheritanceType.TABLE_PER_CLASS} Hibernate resuelve las consultas sobre Usuario
 * mediante UNION sobre las tablas de sus subclases.
 */
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByLogin(String login);

    @Query("select u from Usuario u where u.login = :login and u.activo = true")
    Optional<Usuario> findActivoByLogin(@Param("login") String login);

    boolean existsByLogin(String login);
}
