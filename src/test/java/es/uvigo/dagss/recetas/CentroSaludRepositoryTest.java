package es.uvigo.dagss.recetas;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import es.uvigo.dagss.recetas.entidades.CentroSalud;
import es.uvigo.dagss.recetas.repositorios.CentroSaludRepository;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class CentroSaludRepositoryTest {

  @Autowired CentroSaludRepository repo;

  @Test void like_por_nombre_o_localidad() {
    repo.save(new CentroSalud("CS Vigo","Calle 1","Vigo","36201","Pontevedra","1","a@a.com"));
    repo.save(new CentroSalud("CS Ourense","Calle 2","Ourense","32001","Ourense","2","b@b.com"));

    var res = repo.buscarActivosPorNombreOLocalidadLike("vig");
    assertThat(res).hasSize(1);
    assertThat(res.get(0).getLocalidad()).isEqualTo("Vigo");
  }
}
