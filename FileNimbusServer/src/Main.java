public class Main {

	public static void main(String[] args) {
        String ip = "localhost";
		int port = 8080;
		
		try{
		    if (args.length >= 1) ip = args[0];
		    if (args.length >= 2) port = Integer.parseInt(args[1]);

			Server server = new Server(ip, port);
			server.initServer();
		}
		catch(Exception e) {
			System.out.println("Error: "+e.getMessage());
		}
		return;
	}
}
