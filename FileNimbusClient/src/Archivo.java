public class Archivo {
	String id;
	String shared;
	String name;
	
	public Archivo(String id, String shared, String name) {
		this.id = id;
		this.shared = shared;
		this.name = name;
	}
	
	public String getId() {
    	return id;
    }
	
	public String getShared() {
    	return shared;
    }
	
	public String getNombre() {
    	return name.substring(0, name.lastIndexOf("."));
    }
    
    public String getTipo() {
    	return name.substring(name.lastIndexOf(".")+1, name.length()).toUpperCase();
    }
}
