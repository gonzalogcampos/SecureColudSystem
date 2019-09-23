public class Excepciones extends Exception {
	String mensaje;
	
	public Excepciones(String mensaje) {
		this.mensaje = mensaje;
	};
	
	public String exErrorPersonalizado() {
        return mensaje;
    }
}
