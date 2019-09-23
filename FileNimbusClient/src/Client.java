import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.ConnectException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SealedObject;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class Client{
    private boolean printDebug = true; // Activate print debug
	private int portNum;
	private String ip;
	
	private boolean isSecure = false;
	private String pwd;
	private String username;
	private Key connectionKey = null; 
	private KeyPair userKP = null;

	private ObjectOutputStream out = null;
	private ObjectInputStream in = null;
    private Socket socket = null;
    
    // Codigos
    final String CONECCT ="000";
 	final String CHECK_PWD ="099";
 	final String LOGIN ="100";
 	final String SIGNIT ="110";
 	final String LOGOUT ="120";
 	final String CHECK ="200";
 	final String UPLOAD ="300";
	final String DOWNLOAD ="400";
	final String DELETE ="500";
	final String SHARE ="600";
	final String CHANGE_PASS ="710";
	final String CHANGE_USER ="720";
	final String OTHER ="800";
	final String CLOSE ="900";
	
 	
    // Client class constructor 
    public Client(int portNum, String ip){
        this.portNum = portNum;
        this.ip = ip;
    }

    // Init the client connection
    public boolean initializeClient() throws Exception{
        try{
            socket = new Socket(ip, portNum);
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());
        }
        catch(ConnectException e) {
            throw new Excepciones("Server disconnected, try it later");
        }
        connectTo();
        return isSecure;
    }

    // Connects to the server socket
    public void connectTo() throws Exception {
    	println("Generating secure connection ...");
        secureSend(CONECCT);
    	Key K = (Key) secureReceive();
    	KeyGenerator kgen = KeyGenerator.getInstance("AES");
        kgen.init(128);
        connectionKey = kgen.generateKey();
        
    	Cipher ciph = Cipher.getInstance("RSA");
    	ciph.init(Cipher.ENCRYPT_MODE, K); 
    	SealedObject conectionKeyEncrypted = 
    		new SealedObject(connectionKey, ciph);
    	
        secureSend(conectionKeyEncrypted);
        isSecure = true;
        Object codeRv = secureReceive();
    	if(codeRv.equals("010")) {
            println("Secure AES connection!");
            isSecure = true;
        }
        else {
    		isSecure = false;
    	}
    }

    // Logs the client
    public boolean login(String user, String pass) throws Exception {
            	
        if (comprobarUserPassword(user, pass, LOGIN)) {
			byte[]  pub = (byte[])secureReceive();
			byte[]  priv = (byte[])secureReceive();
			
			// Create AES key from pwd
	        SecretKeyFactory factory = 
	        		SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
	        KeySpec spec = new PBEKeySpec(
	        		pwd.toCharArray(), pwd.getBytes(), 65536, 256);
	        SecretKey tmp = factory.generateSecret(spec);
	        SecretKey secret = new SecretKeySpec(tmp.getEncoded(), "AES");
	        
	        // Decrypt
	        Cipher ciph = Cipher.getInstance("AES");
			ciph.init(Cipher.DECRYPT_MODE, secret);
			priv = ciph.doFinal(priv);
			
			// Cast from byte to key
			KeyFactory kf = KeyFactory.getInstance("RSA"); 
			PrivateKey privateKey = kf.generatePrivate(
					new PKCS8EncodedKeySpec(priv));
			PublicKey publicKey = kf.generatePublic(
					new X509EncodedKeySpec(pub));
			
			userKP = new KeyPair(publicKey, privateKey);
            return true;
		}
        return false;
    }

	// Logout the client
    public void logout() throws Exception {
    	if(username == null) {
    		println("You are not logged in");
        }
        else{
    		secureSend(LOGOUT);
    		Object r = secureReceive();
    		if(r.getClass().equals(String.class)) {
    			if(((String) r).equals("121")) {
    				println("Logout successfull!");
    				username = null;
    				pwd = null;
    				userKP = null;
                }
                else {
    				println("You are not loggued into the server");
    				username = null;
    				pwd = null;
    				userKP = null;
    			}
    		}
        }
    }

    public void close() throws Exception{
    	secureSend(CLOSE);
		if(secureReceive().equals("910")) {
			socket.close();
			in.close();
			out.close();
			println("Connection closed successfully");
        }
        else {
			socket.close();
			in.close();
			out.close();
			println("Closing error, closed also");
		}
    }

    public boolean signUp(String user, String pass) throws Exception {
        boolean signinResponse = false;
        secureSend(SIGNIT);
        
        String datos = secureReceive().toString();
    	if(datos.equals("E100")) {
    		throw new Excepciones("You have already login");
    	}
    	else{
            username = user;
            secureSend(username);
            secureSend(obtenerHash(pass)); // Mandar el HASH de PWD
            
            datos = secureReceive().toString();
            if(datos.equals("E111")) {
                throw new Excepciones("User name already registered");
            }
            else {
                // Crea las claves y se las manda al server
                crearClavesPubPriv(pass, SIGNIT);
                
                if(secureReceive().equals("113")) {
                    signinResponse = true;
                }
            }
        }
        return signinResponse;
    }

    
    // Check user files in the server
    public ArrayList<Archivo> check() throws Exception {
    	
    	// Creamos la lista que contendr� todos los archivos del usuario
    	ArrayList<Archivo> lista = new ArrayList<Archivo>();
    	
    	if(username==null) {
    		println("You are not loggued in");
    		return lista;
    	}
    	
    	secureSend(CHECK);
    	Object r = secureReceive();
    	if(r.getClass().equals(String.class) && ((String)r).equals("E201")) {
    		println("Synchronization error");
    		return lista;
    	}
    	
    	//Leemos tres arrays de misma longitud
    	Object[] id = (Object[]) secureReceive();
    	Object[] shared = (Object[]) secureReceive();
    	Object[] name = (Object[]) secureReceive();
    	
    	if(id.length==shared.length && id.length==name.length) {
    		for(int num=0; num<id.length; num++) {
    			
    			// Creamos el objeto Archivo y lo agregamos a la lista
    			Archivo archivo = new Archivo(id[num].toString(), shared[num].toString(),
    					name[num].toString());
    			lista.add(archivo);
    		}
    	}
    	return lista;
    }

    // TODO Checking this method
    public void upload(File file) throws Exception {
    	if(username == null) {
    		println("You are not logged in");
    		return;
    	}
    	print("Fichero a subir: ");
    	if(!file.exists()) {
    		println("File not found");
    		return;
    	}
        byte[] fileContent = Files.readAllBytes(file.toPath());
        //Genera AES
        KeyGenerator kgen = KeyGenerator.getInstance("AES");
        kgen.init(128); //TODO Tamanyo de clave secreta
        SecretKey k = kgen.generateKey();
         
        //Encripta File AES
        Cipher c = Cipher.getInstance("AES");
        c.init(Cipher.ENCRYPT_MODE, k);
        byte[] fctypyed = c.doFinal(fileContent);
         
        //Encripta K RSA
        c = Cipher.getInstance("RSA");
        c.init(Cipher.ENCRYPT_MODE, userKP.getPublic());
        byte[] kcrypted = c.doFinal(k.getEncoded());
         
        
        secureSend(UPLOAD);
        Object r = secureReceive();
        if(r.getClass().equals(String.class)) {
        	if(((String)r).equals("E301")) {
        		println("Synchronization error");//Logueado en cliente y no en servidor
			}
			else {
        		secureSend(file.toPath().getFileName().toString()); // Enviar el nombre
        		secureSend(fctypyed); // Enviar el file es demasiado grande y habra que fraccionarlo
        		secureSend(kcrypted); // Enviar la clave    		
      
        		// A la espera de confirmacion
        		r = secureReceive();
        		if(r.getClass().equals(String.class)) {
        			if(((String)r).equals("303")){
        				println("File uploaded successfully");
                    }
                    else if(((String)r).equals("E302")){
        				println("Error uploading the file");
        			}
                }
                else{println("Unknown error");}
        	}
        }
        else{println("Unknown error");}       
    }

    public void download(int idArchivo, String directorio) throws Exception {
    	if(username == null) {
    		println("You are not logged in");
    		return;
    	}
    	
    	secureSend(DOWNLOAD);
    	Object r = secureReceive();
        if(r.getClass().equals(String.class)) {
        	if(((String)r).equals("E401")) {
        		println("Synchronization error");//Logueado en cliente y no en servidor
			}
			else {
				// Enviamos el id del archivo
				secureSend(idArchivo);
				
        		// A la espera de confirmacion
        		r = secureReceive();
        		if(r.getClass().equals(String.class)) {
        			if(((String)r).equals("402")){
        				
        				//Obtenemos el fichero, la clave, y el nombre
        		    	byte[] file = (byte[]) secureReceive();
        		    	byte[] key = (byte[]) secureReceive();
        		    	String filename = (String) secureReceive();
        				
        		    	//Desencriptar la clave
        		    	Cipher c = Cipher.getInstance("RSA");
        		    	c.init(Cipher.DECRYPT_MODE, userKP.getPrivate());
        		    	key = c.doFinal(key);
        		    	
        		    	//Desencriptamos el fichero
        		    	c = Cipher.getInstance("AES");
        		    	SecretKey sk = new SecretKeySpec(key, 0, key.length, "AES");
        		    	c.init(Cipher.DECRYPT_MODE, sk);
        		    	file = c.doFinal(file);
        				
    		        	File filepath = new File(directorio +"/"+ filename);
	    	    		
		    	    	// Metodo para renombrar el fichero si ya existe en "directorio"
    		        	if(filepath.exists()) {

    		        		int contFichIguales = 0;
    		        		String name = filepath.getName().substring(0, filepath.getName().lastIndexOf("."));
    		        		String ext = filepath.getName().substring(filepath.getName().lastIndexOf("."));
    		        		
    		        		while (filepath.exists()) {
    		        			contFichIguales++;
    		    	    		filename = name + " ("+contFichIguales+")" + ext;
    		    	    		filepath = new File(directorio +"/"+ filename);
    		    	    	}
    		    	    }
		    	    	
    		        	// Meter los datos del fichero en "filepath"
	    		    	FileOutputStream stream = new FileOutputStream(filepath);
	    			    stream.write(file);
	    			    stream.close();
                    }
                    else if(((String)r).equals("E402")){
        				println("Error - file not exist in BD");
        			}
                }
                else{println("Unknown error");}
        	}
        }
        else {println("Unknown error");}
    }

    public void delete(int idArchivo) throws Exception {
    	if(username == null) {
    		println("You are not logged in");
    		return;
    	}
    	secureSend(DELETE);
    	
    	 Object r = secureReceive();
         if(r.getClass().equals(String.class)) {
         	if(((String)r).equals("E501")) {
         		println("Synchronization error");//Logueado en cliente y no en servidor
         		return;
 			}
 			else {
         		secureSend(idArchivo); // Enviar el idArchivo	
       
         		// A la espera de confirmacion
         		r = secureReceive();
         		if(r.getClass().equals(String.class)) {
         			if(((String)r).equals("502")){
         				println("File deleted successfully");
                     }
                     else if(((String)r).equals("E502")){
         				println("Error - File not delete");
         			}
                 }
                 else{println("Unknown error");}
         	}
         }
         else{println("Unknown error");}
    }
    
    public boolean share(String usu, int file) throws Exception {
    	boolean resultado = false;
    	if(username == null) {
    		println("You are not logged in");
    		return false;
    	}
    	secureSend(SHARE);
    	
    	String r =(String) secureReceive();
    	if(r.equals("E600")) {
    		println("Synchronization error");
    		return false;
    	}
    	// Enviamos el ID del fichero y el nombre a quien se comparte
    	secureSend(file);
    	secureSend(usu);
    	
    	//Esperamos respuesta
    	r = (String)secureReceive();
    	if (r.equals("E601")) {
    		throw new Excepciones("File not exist");
    	} else if(r.equals("E602")){
    		throw new Excepciones("User not exist");
    	}
    	
    	if(r.equals("601")) {
	    	//Leemos las claves
	    	byte[] kf = (byte[]) secureReceive();//Clave secreta de fichero
	    	byte[] ku = (byte[]) secureReceive();//Clave publica de usuario
	    	
	    	//Desencriptar la clave
	    	Cipher c = Cipher.getInstance("RSA");
	    	c.init(Cipher.DECRYPT_MODE, userKP.getPrivate());
	    	byte[] k = c.doFinal(kf);
	    	
	    	//Encriptar la clave
	    	PublicKey pku = KeyFactory.getInstance("RSA").generatePublic(
	    			new X509EncodedKeySpec(ku));
	    	c.init(Cipher.ENCRYPT_MODE, pku);
	    	k = c.doFinal(k);
	    	
	    	//Enviamos la clave
	    	secureSend(k);
	    	r = (String) secureReceive();
	    	if (r.equals("E603")) {
	    		throw new Excepciones("This user already has that file");
	    	} else if (r.equals("603")) { // File shared
	    		resultado = true;
	    	}
    	}
    	return resultado;
    }
    
    public boolean cambiarUser(String newName) throws Exception {
    	boolean userNameCambiado = false;
    	secureSend(CHANGE_USER);
    	String datos = secureReceive().toString();
    	
    	if (datos.equals("721")) {
    		secureSend(newName); 		// New name
    		
			datos = secureReceive().toString();
			if (datos.equals("722")) {
				username = newName;
				userNameCambiado = true;
			} else if (datos.equals("E722")) {
				throw new Excepciones("Name already used");
			} else {
				throw new Excepciones("Error changing the username");
			}
		}
    	return userNameCambiado;
    }
    
	public boolean cambiarPassword(String pass) throws Exception {
		boolean passCambiada = false;
		secureSend(CHANGE_PASS);
		String datos = secureReceive().toString();
		
		if (datos.equals("711")) {
			secureSend(obtenerHash(pwd)); // Mandamos la antigua
			secureSend(obtenerHash(pass)); // mandar el hash de la nueva
			crearClavesPubPriv(pass, CHANGE_PASS); // Crea nuevas claves y se las manda al server
			
			datos = secureReceive().toString();
			if (datos.equals("E712")) {
				throw new Excepciones("Incorrect old password");
			} else if(datos.equals("E713")) {
				throw new Excepciones("Unknown error");
			} else {
				pwd = pass;
				passCambiada = true;
			}
		} else {
			throw new Excepciones("Synchronization error");
		}
		return passCambiada;
	}

    // Se llama desde el panel de settings para comprobar si la contrasenya es correcta
    public boolean comprobarUserPassword(String pass) throws Exception {
    	return comprobarUserPassword(username, pass, CHECK_PWD);
    }
    
    // Comprueba que el usuario y la contrasenya coinciden con los datos de la BD
    // Se utiliza en los paneles Login y Settings
    public boolean comprobarUserPassword(String user, String pass, String codigo)
    	throws Exception {
    	
    	String datos = "";
    	secureSend(codigo); 					// Envia LOGIN o CHECK_PWD
    	
    	if (codigo.equals(LOGIN)) {
    		datos = secureReceive().toString();
        	if (datos.equals("E101")) {
                println("You have already login");
                return false;
            } else if (datos.equals("101")) {
            	username = user; 				// se asigna 1 vez
            }
    	}
		secureSend(username);
        secureSend(obtenerHash(pass)); // mandar el hash de la password
        
        datos = secureReceive().toString();
		if(datos.equals("E102")) {
			// Pasamos la excepcion a la interfaz
			throw new Excepciones("Incorrect user"); 
			
        } else if (datos.equals("102")) { // Usuario correcto
        	datos = secureReceive().toString();
			
        	if (datos.equals("E103")) {
				// Pasamos la excepcion a la interfaz
				if (codigo.equals(LOGIN)) {
					throw new Excepciones("Incorrect password");
				} else {
					// Para Settings
					throw new Excepciones("Incorrect old password");
				}
			} else if (datos.equals("103")) { // Password correcto
				pwd = pass;
            	return true;
            }
        }
    	return false;
    }
    
    // Hash
    private byte[] obtenerHash(String pass) throws NoSuchAlgorithmException {
		MessageDigest messageDig = MessageDigest.getInstance("SHA-512");
		return messageDig.digest(pass.getBytes(StandardCharsets.UTF_8));
    }
    
    // Crear un par de claves (publica y privada)
    private void crearClavesPubPriv(String pass, String codigo) 
    	throws Exception {
    	
    	// Solo se crea cuando registramos un usuario
    	if (codigo.equals(SIGNIT)) {
	        KeyPairGenerator keyPairGenerator = 
	        		KeyPairGenerator.getInstance("RSA");
	        keyPairGenerator.initialize(2048);
	        userKP = keyPairGenerator.genKeyPair();
    	}
    	
        //Crear AES KEY desde pass
        SecretKeyFactory factory = 
        		SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        KeySpec spec = new PBEKeySpec(
        		pass.toCharArray(), pass.getBytes(), 65536, 256);
        SecretKey tmp = factory.generateSecret(spec);
        SecretKey secret = new SecretKeySpec(tmp.getEncoded(), "AES");
        
        //Encriptamos
        Cipher c = Cipher.getInstance("AES");
        c.init(Cipher.ENCRYPT_MODE, secret);
        byte[] encrypted = c.doFinal(userKP.getPrivate().getEncoded());		
        
        //Enviamos las claves
        if (codigo.equals(SIGNIT)) { // Esta cuando registramos un usuario
        	secureSend(userKP.getPublic().getEncoded());
        }
        secureSend(encrypted);
    }
    
    public String getUserName() {
    	return username;
    }
    
    // Sends data with security checks
    private void secureSend(Object o) throws Exception{
        if (isSecure) {
            Cipher ciph = Cipher.getInstance("AES");
			ciph.init(Cipher.ENCRYPT_MODE, connectionKey);
			SealedObject socketEncrypted = 
				new SealedObject((Serializable) o, ciph);
	    	out.writeObject(socketEncrypted);
        } else {
            out.writeObject(o);
        }
    }
    
    // Receives data with security checks
    private Object secureReceive() throws Exception{
        if(isSecure){
            SealedObject socket = (SealedObject)in.readObject();
            Cipher ciph = Cipher.getInstance("AES");
            ciph.init(Cipher.DECRYPT_MODE, connectionKey);
            return socket.getObject(ciph);
        } else {
            return in.readObject();
        }
    }

    // Overload of print
    private void print(Object o) {
    	if(printDebug) {
    		System.out.print(o);
    	}
    }

    // Overload of println
    private void println(Object o) {
    	if(printDebug) {
    		System.out.println(o);
    	}
    }

    // TODO Is this alright?
    public int menu() {
    	if(username == null) {
    	    print(" Unkown: ");
        }
        else {
    	    print(" " + username + ": ");
    	}
    	
    	String r = System.console().readLine();
    	
    	try {
    		return Integer.parseInt(r);
        }
        catch(NumberFormatException e) {
    		println("Enter a number");
    		return 0;
    	}
    }
}