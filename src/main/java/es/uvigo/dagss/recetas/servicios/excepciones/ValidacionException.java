package es.uvigo.dagss.recetas.servicios.excepciones;

public class ValidacionException extends RuntimeException {
    public ValidacionException(String message) {
        super(message);
    }
}
