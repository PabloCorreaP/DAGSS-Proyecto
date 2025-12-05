package es.uvigo.dagss.recetas.entidades;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@DiscriminatorValue(value = "FARMACIA")
public class Farmacia extends Usuario {

    private String nombreEstablecimiento;
    private String nombreFarmaceutico;
    private String apellidosFarmaceutico;
    private String nif;

    @Column(unique = true)
    private String numeroColegiadoFarmaceutico;

    private String domicilio;
    private String localidad;
    private String codigoPostal;
    private String provincia;

    private String telefono;
    private String email;

    public Farmacia() {
        super(TipoUsuario.FARMACIA);
    }

    public Farmacia(String login, String password, String nombreEstablecimiento,
                    String nombreFarmaceutico, String apellidosFarmaceutico, String nif,
                    String numeroColegiadoFarmaceutico) {
        super(TipoUsuario.FARMACIA, login, password);
        this.nombreEstablecimiento = nombreEstablecimiento;
        this.nombreFarmaceutico = nombreFarmaceutico;
        this.apellidosFarmaceutico = apellidosFarmaceutico;
        this.nif = nif;
        this.numeroColegiadoFarmaceutico = numeroColegiadoFarmaceutico;
    }

    public String getNombreEstablecimiento() {
        return nombreEstablecimiento;
    }

    public void setNombreEstablecimiento(String nombreEstablecimiento) {
        this.nombreEstablecimiento = nombreEstablecimiento;
    }

    public String getNombreFarmaceutico() {
        return nombreFarmaceutico;
    }

    public void setNombreFarmaceutico(String nombreFarmaceutico) {
        this.nombreFarmaceutico = nombreFarmaceutico;
    }

    public String getApellidosFarmaceutico() {
        return apellidosFarmaceutico;
    }

    public void setApellidosFarmaceutico(String apellidosFarmaceutico) {
        this.apellidosFarmaceutico = apellidosFarmaceutico;
    }

    public String getNif() {
        return nif;
    }

    public void setNif(String nif) {
        this.nif = nif;
    }

    public String getNumeroColegiadoFarmaceutico() {
        return numeroColegiadoFarmaceutico;
    }

    public void setNumeroColegiadoFarmaceutico(String numeroColegiadoFarmaceutico) {
        this.numeroColegiadoFarmaceutico = numeroColegiadoFarmaceutico;
    }

    public String getDomicilio() {
        return domicilio;
    }

    public void setDomicilio(String domicilio) {
        this.domicilio = domicilio;
    }

    public String getLocalidad() {
        return localidad;
    }

    public void setLocalidad(String localidad) {
        this.localidad = localidad;
    }

    public String getCodigoPostal() {
        return codigoPostal;
    }

    public void setCodigoPostal(String codigoPostal) {
        this.codigoPostal = codigoPostal;
    }

    public String getProvincia() {
        return provincia;
    }

    public void setProvincia(String provincia) {
        this.provincia = provincia;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
