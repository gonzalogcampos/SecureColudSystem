import java.sql.*;

public class GestorBD {
	final String SQL_IP = "localhost";
	final String SQL_PORT = "3306";
	final String SQL_DB = "filenimbusdb";
	final String SQL_USER = "root";
	final String SQL_USERPWD = "";
	//final String SQL_DRIVER = "com.mysql.jdbc.Driver";
	final String DB_URL = "jdbc:mysql://" + SQL_IP + ":" + SQL_PORT + "/" + SQL_DB;
	
	Statement sqlSentence;
	PreparedStatement preparedSentence;
	Connection sqlConnection;
	
	public GestorBD() {
		sqlConnection = null;
		preparedSentence = null;
		sqlSentence = null;
	}


	public void conectarBD() throws SQLException {
		/* Registrar JDBC driver:
		   A partir de JDK 6, los drivers JDBC 4 ya se registran automaticamente 
		   y no es necesario el Class.forName(), solo que esten en el classpath de la JVM.*/
		//Class.forName(SQL_DRIVER);
		
		// Abrir conexion
	    System.out.println("Connecting to database...");
		sqlConnection = DriverManager.getConnection(DB_URL, SQL_USER, SQL_USERPWD);
	}
	
	public ResultSet ejecutarQuery(String sql) throws SQLException {
		ResultSet resultados = null;
		sqlSentence = sqlConnection.createStatement();
		resultados = sqlSentence.executeQuery(sql);
		return resultados;
	}
	
	public void borrarArchivo(int userID, int idArchivo) throws SQLException {
		System.out.println("Creating delete statement...");
		
		sqlSentence = sqlConnection.createStatement();
		String sql = "DELETE FROM fileuser WHERE user=" + userID + " AND file=" + idArchivo;
		sqlSentence.executeUpdate(sql);
	}
	
	public int cambiarUser(int userID, String name) throws SQLException {
		System.out.println("Creating update statement...");
		
		sqlSentence = sqlConnection.createStatement();
		String sql = "UPDATE user SET user.user='"+name+"' WHERE id = "+userID;
		return sqlSentence.executeUpdate(sql);
	}
	
	public int cambiarPassword(int userID, byte[] nueva, byte[] priv, byte[] actual) throws SQLException {
		preparedSentence = sqlConnection.prepareStatement("UPDATE user SET "
		 		+ "pwd = ?, "
		 		+ "private = ? "
		 		+ "WHERE id = "+userID
		 		+ " AND pwd = ?");
		preparedSentence.setBytes(1, nueva);
		preparedSentence.setBytes(2, priv);
		preparedSentence.setBytes(3, actual);
		
		return preparedSentence.executeUpdate();
	}
	
	public ResultSet insertarUser(String newuser, byte[] pwdh, byte[] priv, byte[] pub) throws SQLException {
		ResultSet resultado = null;
		preparedSentence = sqlConnection.prepareStatement("INSERT INTO user(user, pwd, private, public) VALUES('"+ newuser +"', ?, ?, ?)",
			PreparedStatement.RETURN_GENERATED_KEYS);
		
		preparedSentence.setBytes(1, pwdh);
		preparedSentence.setBytes(2, priv);
		preparedSentence.setBytes(3, pub);
		preparedSentence.executeUpdate();
		resultado = preparedSentence.getGeneratedKeys();
		return resultado;
	}
	
	public ResultSet subirArchivo(String filename, byte[] file) throws SQLException {
		ResultSet resultado = null;
		
		String sentence = "INSERT INTO file(data, name) " + "VALUES(?, '" + filename + "')";
		preparedSentence = sqlConnection.prepareStatement(sentence, PreparedStatement.RETURN_GENERATED_KEYS);
		 
		preparedSentence.setBytes(1, file);
		preparedSentence.executeUpdate();
		resultado = preparedSentence.getGeneratedKeys();
		return resultado;
	}
	
	public void subirKey(int userID, int fileID, byte[] key) throws SQLException {
		String sentence = "INSERT INTO fileuser(user, file, secretKey, shared) " + "VALUES('"+ userID +"', "+ fileID +", ?, '"+ userID + "')";
		preparedSentence = sqlConnection.prepareStatement(sentence);
		preparedSentence.setBytes(1, key);
		preparedSentence.executeUpdate();
	}
	
	public void close() throws SQLException {
		System.out.println("Close conection BD...");
		if(preparedSentence!=null)
			preparedSentence.close();
		
		if(sqlSentence!=null)
			sqlSentence.close();
		
         if(sqlConnection!=null)
        	 sqlConnection.close();
	}
}
