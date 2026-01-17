package es.uvigo.dagss.recetas.controladores.dto;

import es.uvigo.dagss.recetas.entidades.TipoUsuario;

public class LoginResponse {
    public Long id;
    public TipoUsuario tipo;
    public String login;

    public LoginResponse(Long id, TipoUsuario tipo, String login) {
        this.id = id;
        this.tipo = tipo;
        this.login = login;
    }
}
