import javax.crypto.Cipher;
import javax.crypto.SealedObject;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.sql.Blob;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;

import org.java_websocket.WebSocket;
import org.json.JSONObject;

public class ClientController extends Thread{
	boolean secureConnection = false; // Secure connection sentinel
	final WebSocket webSocket;
	final int kClient;
	Key connectionKey = null; // Secret connection key
	int userID = Integer.MAX_VALUE;
	KeyPair keyPair = null;
	GestorBD gestor;

	String currentUploadingFilename = "";
	byte[] currentUploadingKey;
	
	// Codigos
	final String CONNECT ="connect";
	final String LOGIN ="login";
	final String SIGNIN ="signin";
	final String LOGOUT ="logout";
	final String CHECK ="check";
	final String UPLOAD ="upload";
	final String DOWNLOAD ="download";
	final String DELETE ="delete";
	final String SHARE ="share";
	final String CHANGE_PASS ="changepassword";
	final String CHANGE_USER ="changeusername";
	
	
	public ClientController(WebSocket webSocket, int client) {
		this.webSocket = webSocket;
		this.kClient = client;
		this.keyPair = buildKeyPair();
		gestor = new GestorBD();
	}

	public void processCommand(JSONObject o) {
		try {
			String option = o.getString("option");
			switch (option) {
				case CONNECT:
					connect(o);
					break;
				case LOGIN:
					login(o);
					break;
				case LOGOUT:
					logout();
					break;
				case SIGNIN:
					signIn(o);
					break;
				case UPLOAD:
					upload(o);
					break;
				case DOWNLOAD:
					download(o);
					break;
				case DELETE:
					delete(o);
					break;
				case CHECK:
					check();
					break;
				case SHARE:
					//share();
					break;
				case CHANGE_PASS:
					changePassword(o);
					break;
				case CHANGE_USER:
					changeUser(o);
					break;
				default:
					System.out.println("Error: opcion incorrecta");
					break;
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
	   
	public void connect(JSONObject o) throws Exception{
		 System.out.println(kClient + ": Conectando...");
		 try {
		 	switch (o.getInt("stage")) {
				case 0:
					String publicKey = Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded());

					webSocket.send("{\"option\": \"connect\", \"stage\": 1, \"publicKey\": \""+publicKey+"\"}");
				break;
				case 2:
					Cipher c = Cipher.getInstance("RSA");
					c.init(Cipher.DECRYPT_MODE, keyPair.getPrivate());

					Object cKey = Base64.getDecoder().decode(o.getString("connectionKey"));

					SealedObject i = (SealedObject) cKey;

					connectionKey = (Key) i.getObject(c);

					c = Cipher.getInstance("AES");
					c.init(Cipher.ENCRYPT_MODE, connectionKey);
					SealedObject socketEncrypted = new SealedObject("true", c);

					webSocket.send("{\"option\": \"connect\", \"stage\": 3, \"isSecure\": \""+socketEncrypted.toString()+"\"}");
				break;
				case 4:
					secureConnection = true;
				break;
				default: break;
			}
		 }
		 catch(Exception e) {
			 System.out.println(e);
		 }
	}

	public void login(JSONObject o) throws Exception {
		if(userID != Integer.MAX_VALUE) {
			webSocket.send("{\"option\": \"login\", \"status\": 0, \"error\": \"Already logged in.\"}");
			return;
		}

		ArrayList<Blob> b = comprobarUserPassword(o);
		if (b.size() > 0){
			byte[] priv = b.get(0).getBytes(1, (int) b.get(0).length());
			byte[] pub = b.get(0).getBytes(1, (int) b.get(0).length());

			webSocket.send("{\"option\": \"login\", \"status\": 1, \"privatekey\": \""+priv+"\", \"publickey\": \""+pub+"\"}");
		} else {
			webSocket.send("{\"option\": \"login\", \"status\": 0, \"error\": \"Login failed.\"}");
		}
	}
	
	 public void logout() throws Exception {
		 if(userID == Integer.MAX_VALUE) {
			 webSocket.send("{\"option\": \"logout\", \"status\": 0, \"error\": \"Not logged in.\"}");
			 return;
		 }

		 userID = Integer.MAX_VALUE;

		 webSocket.send("{\"option\": \"logout\", \"status\": 1}");
		 System.out.println(kClient + ": Logout");
	 }

	 public void signIn(JSONObject o) throws Exception{
		 if(userID != Integer.MAX_VALUE) {
			 webSocket.send("{\"option\": \"signin\", \"status\": 0, \"error\": \"Already logged in.\"}");
			return;
		 }
		 
		 String newuser = o.getString("username");
		 byte[] pwdh = o.getString("password").getBytes();
		 
		 gestor.conectarBD(); // conectamos con la BD
		 ResultSet rs = gestor.ejecutarQuery("SELECT user FROM user WHERE user = '"+newuser+"' LIMIT 1");
		 if(rs.first()) {
			 webSocket.send("{\"option\": \"signin\", \"status\": 0, \"error\": \"Already signed in.\"}");
			 rs.close();
			 gestor.close();
			 return;
		 }
		 
		 byte[] pub = o.getString("publicKey").getBytes();
		 byte[] priv = o.getString("privateKey").getBytes();
		 
		 rs = gestor.insertarUser(newuser, pwdh, priv, pub);
	     if(!rs.first()) {
			 webSocket.send("{\"option\": \"signin\", \"status\": 0, \"error\": \"Already signed in.\"}");
			 rs.close();
			 gestor.close();
			 return;
		 }

		 webSocket.send("{\"option\": \"signin\", \"status\": 1}");
		 
		 rs.close();
		 gestor.close();
	 }

	 public void upload(JSONObject o) throws Exception{
		 if(userID == Integer.MAX_VALUE) {
			 webSocket.send("{\"option\": \"upload\", \"status\": 0, \"error\": \"Not logged in.\"}");
			 return;
		 }
		 
		 currentUploadingFilename =  o.getString("filename");
		 currentUploadingKey = o.getString("secretKey").getBytes();

		 webSocket.send("{\"option\": \"upload\", \"status\": 1}");
	 }

	 public void uploadFile(ByteBuffer b) throws Exception{
		 if(userID == Integer.MAX_VALUE) {
			 webSocket.send("{\"option\": \"upload\", \"status\": 0, \"error\": \"Not logged in.\"}");
			 return;
		 }

		 if (currentUploadingFilename.equals("") || currentUploadingKey.length <= 0){
			 webSocket.send("{\"option\": \"upload\", \"status\": 0, \"error\": \"Upload command failed.\"}");
			 return;
		 }

		 byte[] file;

		 if (secureConnection){
			 Cipher c = Cipher.getInstance("AES");
			 c.init(Cipher.DECRYPT_MODE, connectionKey);
			 SealedObject socket = new SealedObject((Serializable) b.array(), c);
			 file = (byte[]) socket.getObject(c);
		 } else {
		 	 file = b.array();
		 }

		 // Upload file and key
		 gestor.conectarBD();
		 ResultSet rs = gestor.subirArchivo(currentUploadingFilename, file);
		 if(!rs.first()) {
			 rs.close();
			 gestor.close();
			 webSocket.send("{\"option\": \"upload\", \"status\": 0, \"error\": \"Unable to upload.\"}");
			 return;
		 }

		 int fileID = rs.getInt(1);
		 gestor.subirKey(userID, fileID, currentUploadingKey);
		 System.out.println(kClient + ": Fichero subido: " + currentUploadingFilename);

		 webSocket.send("{\"option\": \"upload\", \"status\": 2}");

		 currentUploadingFilename = "";
		 currentUploadingKey = new byte[0];
		 rs.close();
		 gestor.close();
	 }

	public void download(JSONObject o) throws Exception{
		if(userID == Integer.MAX_VALUE) {
			webSocket.send("{\"option\": \"download\", \"status\": 0, \"error\": \"Not logged in.\"}");
			return;
		}

		int fileID = o.getInt("fileID");

		gestor.conectarBD(); // conectamos con la BD
		ResultSet rs = gestor.ejecutarQuery("SELECT * "
				+ "FROM fileuser, file "
				+ "WHERE fileuser.user = '"+ userID +"' "
				+ "AND file.id = fileuser.file "
				+ "AND file.id = "+fileID+" LIMIT 1");

		if(!rs.first()) {
			webSocket.send("{\"option\": \"download\", \"status\": 0, \"error\": \"File not found.\"}");
			rs.close();
			gestor.close();
			return;
		}

		webSocket.send("{\"option\": \"download\", \"status\": 1, \"filename\": \""+rs.getString("name")+"\", \"secretKey\": \""+ Arrays.toString(rs.getBlob("secretKey").getBytes(1, (int) rs.getBlob("secretKey").length())) +"\"}");

		ByteBuffer b;

		if (secureConnection){
			Cipher c = Cipher.getInstance("AES");
			c.init(Cipher.ENCRYPT_MODE, connectionKey);
			SealedObject socketEncrypted = new SealedObject(rs.getBlob("data").getBytes(1, (int) rs.getBlob("data").length()), c);

			b = ByteBuffer.wrap((byte[])socketEncrypted.getObject(c));
		} else {
			b = ByteBuffer.wrap(rs.getBlob("data").getBytes(1, (int) rs.getBlob("data").length()));
		}

		webSocket.send(b);

		String name = rs.getString("name");
		System.out.println(kClient + ": Descargado fichero: " + name + " id: " + fileID);
		rs.close();
		gestor.close();
	}
	 
	 public void check() throws Exception{
		 if(userID == Integer.MAX_VALUE) {
			 webSocket.send("{\"option\": \"check\", \"status\": 0, \"error\": \"Not logged in.\"}");
			 return;
		 }
		 
		 // Search in the DDBB
		 gestor.conectarBD(); // conectamos con la BD
		 ResultSet rs = gestor.ejecutarQuery("SELECT f.name filename, f.id fileid, u.user shared "
		 		+ "FROM fileuser r, file f, user u "
		 		+ "WHERE r.shared = u.id "
		 		+ "AND r.file = f.id "
		 		+ "AND r.user = "+userID+" ");

		 ArrayList<Integer>  id = new ArrayList<>();
		 ArrayList<String> shared = new ArrayList<>();
		 ArrayList<String> name = new ArrayList<>();
		 
		 while(rs.next()) {
			 id.add(rs.getInt("fileid"));
			 shared.add(rs.getString("shared"));
			 name.add(rs.getString("filename"));
		 }

		 rs.close();
		 gestor.close();

		 webSocket.send("{\"option\": \"check\", \"status\": 1, \"ids\": \""+Arrays.toString(id.toArray())+"\", \"shared\": \""+Arrays.toString(shared.toArray())+"\", \"filenames\": \""+Arrays.toString(name.toArray())+"\"}");
	 }
	 
	 public void delete(JSONObject o) throws Exception{
		 if(userID == Integer.MAX_VALUE) {
			 webSocket.send("{\"option\": \"delete\", \"status\": 0, \"error\": \"Not logged in.\"}");
			 return;
		 }
		 
		 int idArchivo = o.getInt("fileID");
		 try {
			 gestor.conectarBD(); // conectamos con la BD
			 gestor.borrarArchivo(userID, idArchivo);
			 gestor.close();
		 }
		 catch(SQLException e){
			 webSocket.send("{\"option\": \"delete\", \"status\": 0, \"error\": \"File not exist.\"}");
			 return;
		 }

		 webSocket.send("{\"option\": \"delete\", \"status\": 1}");
	 }
	 
	 public void changePassword(JSONObject o) throws Exception{
		 if(userID == Integer.MAX_VALUE) {
			 webSocket.send("{\"option\": \"changepassword\", \"status\": 0, \"error\": \"Not logged in.\"}");
			 return;
		 }
		 
		 byte[] actual = o.getString("currentPassword").getBytes();
		 byte[] nueva = o.getString("newPassword").getBytes();
		 byte[] priv = o.getString("privateKey").getBytes();
		 
		 gestor.conectarBD(); // conectamos con la BD
		 int numCambios = gestor.cambiarPassword(userID, nueva, priv, actual);
		 gestor.close();
		 
		 if(numCambios < 1) {
			 webSocket.send("{\"option\": \"changepassword\", \"status\": 0, \"error\": \"Failed on change password.\"}");
		 }else if(numCambios > 1) {
			 webSocket.send("{\"option\": \"changepassword\", \"status\": 0, \"error\": \"Failed on change password.\"}");
		 }else {
			 webSocket.send("{\"option\": \"changepassword\", \"status\": 1}");
		 } 
	 }
	 
	 public void changeUser(JSONObject o) throws Exception {
		 if(userID == Integer.MAX_VALUE) {
			 webSocket.send("{\"option\": \"changeusername\", \"status\": 0, \"error\": \"Not logged in.\"}");
			 return;
		 }

		 String newName = o.getString("username");
		 
		 gestor.conectarBD(); // conectamos con la BD
		 ResultSet rs = gestor.ejecutarQuery("SELECT id FROM user WHERE user.user='"+newName+"'");
		 
		 // Existe ya ese nombre
		 if (rs.next()) {
			 webSocket.send("{\"option\": \"changeusername\", \"status\": 0, \"error\": \"New name already exists.\"}");
			 rs.close();
			 gestor.close();
			 return;
		 }

		 int numCambios = gestor.cambiarUser(userID, newName);
		 if (numCambios==1) {
			 webSocket.send("{\"option\": \"changeusername\", \"status\": 1}");
			 rs.close();
			 gestor.close();
		 }
		 else {
			 webSocket.send("{\"option\": \"changeusername\", \"status\": 0, \"error\": \"Unknown error.\"}");
			 rs.close();
			 gestor.close();
			 return;
		 }
	 }

	 /*
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
			gestor.subirKey(usuid, file, k);
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
	*/

	public ArrayList<Blob> comprobarUserPassword(JSONObject o) throws Exception {
		ArrayList<Blob> b = new ArrayList<>();

		String user = o.getString("username");
		byte[] pasw = o.getString("password").getBytes();

		if (user.getClass().equals(String.class)) {
			// Check keys
			gestor.conectarBD(); // conectamos con la BD
			ResultSet rs = gestor.ejecutarQuery("SELECT * FROM user WHERE user='" + user + "'");
			if (rs.next()) {
				byte[] pwd = rs.getBlob("pwd").getBytes(1, (int) rs.getBlob("pwd").length());
				if (Arrays.equals(pwd, pasw)) {
					// Send keys
					Blob privb = rs.getBlob("private");
					Blob pubb = rs.getBlob("public");

					b.add(privb);
					b.add(pubb);

					userID = rs.getInt("id");
					System.out.println(kClient + ": Login: " + user);
				}
				else {
					System.out.println("Error clave");
				}
			}
			else {
				System.out.println("Error user");
			}

			// Limpiar resultSet y cerrar conexion con BD
			rs.close();
			gestor.close();
		}
		return b;
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
