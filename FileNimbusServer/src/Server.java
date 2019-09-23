import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class Server{
	private int port;
	private String ip;
	
	private ServerSocket listener;
	private ObjectOutputStream out;
	private ObjectInputStream in;
	
	private int client;
	
	public Server(String server_ip, int server_port) {
		this.port = server_port;
		this.ip = server_ip;
		
		this.listener = null;
		this.out = null;
		this.in = null;
		
		this.client = 0;
	}

	public void initServer() throws Exception {
		
    	// Overload the kill signal
    	Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			System.out.println("Cerrando Servidor...");
			try{
				in.close();
				out.close();
				listener.close();
			}
			catch(Exception e) {
				// TODO
			}
		}));
    	
        // Creates the socket
        listener = new ServerSocket(port, 0, InetAddress.getByName(ip));
        
        System.out.println("Servidor iniciado en: " + listener.getInetAddress() + ":" +port);
        
        // Client socket
        Socket clientSocket = null;
        
        while (true) {
            try {
                // Accept a client connection once Server receives one.
            	clientSocket = listener.accept();
                System.out.println(client + ": Nuevo cliente");

                if (clientSocket != null){
					// Creating in/out and output streams
					out = new ObjectOutputStream(clientSocket.getOutputStream());
					in = new ObjectInputStream(clientSocket.getInputStream());

					// Starts threading
					Thread t = new ClientController(clientSocket, in, out, client);
					t.start();
				}

                client++;
            }
            finally {
            	// TODO
            } 
        }
	}
}