import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.sql.Blob;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.SealedObject;

public class ClientController extends Thread{
	boolean secureConnection = false; // Secure connection sentinel
	final Socket clientSocket;
	final ObjectInputStream in;
	final ObjectOutputStream out;
	final int kClient;
	Key connectionKey = null; // Secret connection key
	int userID = Integer.MAX_VALUE;
	KeyPair keyPair = null;
	GestorBD gestor;
	
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
	
	
	public ClientController(Socket clientSocket, ObjectInputStream in, 
			ObjectOutputStream out, int client) {
		this.clientSocket = clientSocket;
		this.in = in;
		this.out = out;
		this.kClient = client;
		this.keyPair = buildKeyPair();
		gestor = new GestorBD();
	} 
	 
	public void run(){
		 try {
	        String codigo = (String)secureReceive();
	        while(!codigo.isEmpty()) {
	        	if(codigo.equals(CONECCT)) {
	        		connect();
	        	}else if(codigo.equals(CHECK_PWD)){
	        		comprobarUserPassword(CHECK_PWD);
	        	}else if(codigo.equals(LOGIN)){
	        		login();
	        	}else if(codigo.equals(SIGNIT)) {
	        		signIn();
	        	}else if(codigo.equals(LOGOUT)) {
	        		logout();
	        	}else if(codigo.equals(CHECK)){
	        		check();
	        	}else if(codigo.equals(UPLOAD)){
	        		upload();
	        	}else if(codigo.equals(DOWNLOAD)){
	        		download();
	        	}else if(codigo.equals(DELETE)){
	        		delete();
	        	}else if(codigo.equals(SHARE)){
	        		share();
	        	}else if(codigo.equals(CHANGE_PASS)){
	        		changePassword();
	        	}else if(codigo.equals(CHANGE_USER)){
	        		changeUser();
	        	}else if(codigo.equals(OTHER)){
	        		// Void
	        		secureSend("E000");
	        	}else if(codigo.equals(CLOSE)) {
	        		// Close
	        		secureSend("910");
	        		System.out.println(this.kClient + ": Fin de conexion");
	        		return;
	        	}
	        	else{
	            	System.out.println(this.kClient + ": " + codigo);
	            	secureSend("E100");
            	}
	        	
	        	codigo = (String)secureReceive();
            }
            out.close();
            in.close();
            clientSocket.close();
		}
		catch(Exception e) {
			 System.out.println(e.getMessage());
		}
	}
	   
	public void connect() throws Exception{
		 System.out.println(kClient + ": Conectando...");
		 try {
			 // Emite clave publica
			 out.writeObject(keyPair.getPublic());
			 
			 // Recibe la clave secreta
			 SealedObject i = (SealedObject) in.readObject();
			 
			 // Desencripta la clave secreta
			 Cipher c = Cipher.getInstance("RSA");
			 c.init(Cipher.DECRYPT_MODE, keyPair.getPrivate());
			 connectionKey = (Key) i.getObject(c);
			 
			 // Encripta el socket
			 c = Cipher.getInstance("AES");
			 c.init(Cipher.ENCRYPT_MODE, connectionKey);
			 SealedObject socketEncrypted = new SealedObject("010", c);
			 
			 // TODO Clean method
			 System.out.println(kClient + ": Conexion segura!");
			 
			 out.writeObject(socketEncrypted);
			 secureConnection = true;
		 }
		 catch(Exception e) {
			 System.out.println(e.getMessage());
		 }
	}
	
	public void login() throws Exception {
		 if(userID!=Integer.MAX_VALUE) {
			 secureSend("E101");
			 return;
		 }
		 secureSend("101");
		 comprobarUserPassword(LOGIN);
	 }
	
	public void comprobarUserPassword(String accion) throws Exception {
		Object user = secureReceive();
		Object pasw = secureReceive();

		if (user.getClass().equals(String.class)) {
			// Check keys
			gestor.conectarBD(); // conectamos con la BD
			ResultSet rs = gestor.ejecutarQuery("SELECT * FROM user WHERE user='" + user + "'");
			if (rs.next()) {
				secureSend("102");
				
				byte[] pwd = rs.getBlob("pwd").getBytes(1, (int) rs.getBlob("pwd").length());
				if (Arrays.equals(pwd, (byte[])pasw)) {
					secureSend("103");
					
					if (accion.equals(LOGIN)) {
						// Send keys
						Blob privb = rs.getBlob("private");
						Blob pubb = rs.getBlob("public");
				
						byte[] priv = privb.getBytes(1, (int) privb.length());
						byte[] pub = pubb.getBytes(1, (int) pubb.length());
				
						secureSend(pub);
						secureSend(priv);
				
						userID = rs.getInt("id");
						System.out.println(kClient + ": Login: " + user);
					}
				}
				else {
					secureSend("E103"); // Invalid pass
					rs.close();
					gestor.close();
					System.out.println("Error clave");
					return;
				}
		
				// Limpiar resultSet y cerrar conexion con BD
				rs.close();
				gestor.close();
			}
			else {
				secureSend("E102"); // Invalid user
				rs.close();
				gestor.close();
				return;
			}
		}
	}
	
	 public void logout() throws Exception {
		 if(userID == Integer.MAX_VALUE) {
			 secureSend("E121");
		 }else {
			 userID=Integer.MAX_VALUE;
			 secureSend("121");
			 System.out.println(kClient + ": Logout");
		 }
	 }
	 public void signIn() throws Exception{
		 if(userID != Integer.MAX_VALUE) {
			secureSend("E100");
			return;
		 }
		 secureSend("111");
		 
		 String newuser = secureReceive().toString();
		 byte[] pwdh = (byte[]) secureReceive();
		 
		 gestor.conectarBD(); // conectamos con la BD
		 ResultSet rs = gestor.ejecutarQuery("SELECT user FROM user WHERE user = '"+newuser+"' LIMIT 1");
		 if(rs.first()) {
			 secureSend("E111");//USUARIO YA REGISTRADO
			 rs.close();
			 gestor.close();
			 return;
		 }
		 else {
			 secureSend("112");
		 }
		 
		 byte[] pub = (byte[]) secureReceive();
		 byte[] priv = (byte[]) secureReceive();
		 
		 rs = gestor.insertarUser(newuser, pwdh, priv, pub);
	     if(rs.first())
	     {
	         //userID = rs.getInt(1);
	         secureSend("113");
	         System.out.println(kClient + ": Signin id:" +userID+ " username:" + newuser);
	     }else {
	    	 secureSend("E113");
	     }
		 
		 rs.close();
		 gestor.close();
		 
	 }
	 public void upload() throws Exception{
		 if(userID== Integer.MAX_VALUE) {
			 // Not registered
			 secureSend("E301");
			 return;
		 }
		 secureSend("301");
		 
		 String filename = (String) secureReceive();
		 byte[] file = (byte[]) secureReceive(); // TODO Repeat send
		 byte[] key = (byte[]) secureReceive();
		 
		 // Upload file and key
		 gestor.conectarBD();
		 ResultSet rs = gestor.subirArchivo(filename, file);    		 
		 if(rs.first()){
	         int fileID = rs.getInt(1);
	         gestor.subirKey(userID, fileID, key);
	 		 System.out.println(kClient + ": Fichero subido: " + filename);
			 secureSend("303"); // OK
	     }else{
	    	 secureSend("E302"); // Error uploading file
	     }
		 rs.close();
		 gestor.close();
	 }
	 public void share() throws Exception{
		 if(userID==Integer.MAX_VALUE) {
			 secureSend("E600");
			 return;
		 }
		 else {
			 secureSend("601");
		 }
	
		 int file = (int) secureReceive();
		 String usu = (String) secureReceive();
		 int usuid;
		 byte[] ku;
		 byte[] kf;
	
		 // Search the file and save the key
		 gestor.conectarBD(); // conectamos con la BD
		 ResultSet rs = gestor.ejecutarQuery("SELECT secretKey "
		 		+ "FROM fileuser "
		 		+ "WHERE user='"+ userID +"' "
		 		+ "AND file= " + file);
		 
		 if(!rs.first()) {
			 secureSend("E601");
			 return;
		 }
		 else {
			 kf = rs.getBlob("secretKey").getBytes(1, (int) rs.getBlob("secretKey").length());
		 }
		 
		 // Search the user to share with and upload key
		 rs = gestor.ejecutarQuery("SELECT id, public "
			 		+ "FROM user "
			 		+ "WHERE user='"+ usu +"'");
		 if(!rs.first()) {
			 secureSend("E602");
			 return;
		 }
		 else {
			secureSend("601");
			usuid = rs.getInt("id");
			ku = rs.getBlob("public").getBytes(1, (int) rs.getBlob("public").length());
		 }
		 
		 rs.close();
		 
		 // Send keys
		 secureSend(kf);
		 secureSend(ku);
		 
		 byte[] k = (byte[]) secureReceive();
		 
		 // Update table
		 try {
			 gestor.shareFile(usuid, file, k, userID);
		 }
		 catch(SQLException e) {
			 secureSend("E603"); // Already shared
			 System.out.println(e);
			 gestor.close();
			 return;
		 }
		 gestor.close();
		 secureSend("603");
	 }
	 
	 public void download() throws Exception{
		 if(userID == Integer.MAX_VALUE) {
			 secureSend("E401");
			 return;
		 }
		 secureSend("401");
		 
		 int fileID = (int) secureReceive();
		 
		 gestor.conectarBD(); // conectamos con la BD
		 ResultSet rs = gestor.ejecutarQuery("SELECT * "
		 		+ "FROM fileuser, file "
		 		+ "WHERE fileuser.user = '"+ userID +"' "
		 		+ "AND file.id = fileuser.file "
		 		+ "AND file.id = "+fileID+" LIMIT 1");
		 if(!rs.first()) {
			 secureSend("E402");
			 return;
		 }
		 else {
			 secureSend("402");
			 secureSend(rs.getBlob("data").getBytes(1, (int) rs.getBlob("data").length()));
			 secureSend(rs.getBlob("secretKey").getBytes(1, (int) rs.getBlob("secretKey").length()));
			 secureSend(rs.getString("name"));
			 
			 String name =rs.getString("name");
			 System.out.println(kClient + ": Descargado fichero: " + name + " id: " + fileID);
		 }
		 rs.close();
		 gestor.close();
	 }
	 
	 public void check() throws Exception{
		 if(userID==Integer.MAX_VALUE) {
			 secureSend("E201");
		 }else {
			 secureSend("201");
		 }
		 
		 // Search in the DDBB
		 gestor.conectarBD(); // conectamos con la BD
		 ResultSet rs = gestor.ejecutarQuery("SELECT f.name filename, f.id fileid, u.user shared "
		 		+ "FROM fileuser r, file f, user u "
		 		+ "WHERE r.shared = u.id "
		 		+ "AND r.file = f.id "
		 		+ "AND r.user = "+userID+" ");

		 ArrayList<Integer>  id = new ArrayList<Integer>();
		 ArrayList<String> shared = new ArrayList<String>();
		 ArrayList<String> name = new ArrayList<String>();
		 
		 while(rs.next()) {
			 id.add(rs.getInt("fileid"));
			 shared.add(rs.getString("shared"));
			 name.add(rs.getString("filename"));
		 }
		 rs.close();
		 gestor.close();
		 
		 secureSend(id.toArray());
		 secureSend(shared.toArray());
		 secureSend(name.toArray());
	 }
	 
	 public void delete() throws Exception{
		 if(userID==Integer.MAX_VALUE) {
			 secureSend("E501");
			 return;
		 }
		 else {
			 secureSend("501");
		 }
		 
		 int idArchivo = (Integer) secureReceive();
		 try {
			 gestor.conectarBD(); // conectamos con la BD
			 gestor.borrarArchivo(userID, idArchivo);
			 gestor.close();
		 }
		 catch(SQLException e){
			 secureSend("E502");
			 gestor.close();
			 return;
		 }
		 secureSend("502");
	 }
	 
	 public void changePassword() throws Exception{
		 if(userID==Integer.MAX_VALUE) {
			 secureSend("E711");
		 }
		 else {
			 secureSend("711");
		 }
		 
		 byte[] actual = (byte[]) secureReceive();
		 byte[] nueva = (byte[]) secureReceive();
		 byte[] priv = (byte[]) secureReceive();
		 
		 gestor.conectarBD(); // conectamos con la BD
		 int numCambios = gestor.cambiarPassword(userID, nueva, priv, actual);
		 gestor.close();
		 
		 if(numCambios < 1) {
			 secureSend("E712");
		 }else if(numCambios > 1) {
			 secureSend("E713");
		 }else {
			 secureSend("712");
		 } 
	 }
	 
	 public void changeUser() throws Exception {
		 if(userID==Integer.MAX_VALUE) {
			 secureSend("E721");
		 }else {
			 secureSend("721");
		 }
		 String newName = secureReceive().toString();
		 
		 gestor.conectarBD(); // conectamos con la BD
		 ResultSet rs = gestor.ejecutarQuery("SELECT id FROM user WHERE user.user='"+newName+"'");
		 
		 // Existe ya ese nombre
		 if (rs.next()) {
			 secureSend("E722");
			 rs.close();
			 gestor.close();
			 return;
			 
		// No existe ese nombre
		 } else {
			 int numCambios = gestor.cambiarUser(userID, newName);
			 if (numCambios==1) {
				 secureSend("722"); // Cambio bien hecho
				 rs.close();
				 gestor.close();
			 }
			 else {
				 secureSend("723");
				 rs.close();
				 gestor.close();
				 return;
			 }
		 }
	 }
	 
	public void secureSend(Object o) throws Exception{
		if(secureConnection) {
			Cipher c = Cipher.getInstance("AES");
			c.init(Cipher.ENCRYPT_MODE, connectionKey);
			SealedObject socketEncrypted = new SealedObject((Serializable) o, c);
			out.writeObject(socketEncrypted);
		}
		else {
			out.writeObject(o);
		}
    }
	
	public Object secureReceive() throws Exception {
		if(secureConnection) {
			Object o = in.readObject();
			System.out.println(o);
			SealedObject socket = (SealedObject) o;
			Cipher c = Cipher.getInstance("AES");
			c.init(Cipher.DECRYPT_MODE, connectionKey);
			return socket.getObject(c);
		}
		else {
			return in.readObject();
		}
	}
	
	public KeyPair buildKeyPair(){
		try {
	        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
	        keyPairGenerator.initialize(2048);      
	        return keyPairGenerator.genKeyPair();
		}
		catch(Exception e) {
			System.out.println(e);
		}
		return null;
    }
}
