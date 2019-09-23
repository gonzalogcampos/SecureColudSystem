import org.java_websocket.server.DefaultSSLWebSocketServerFactory;

import javax.net.ssl.SSLContext;
import java.net.InetSocketAddress;

public class Main {

    public static void main(String[] args) {
        String host = "localhost";
        int port = 8081;

        String cert = "../certs";
        String passwd = "passwd";

        if (args.length >= 1) host = args[0];
        if (args.length >= 2) port = Integer.parseInt(args[1]);

        Middleware middleware = new Middleware(new InetSocketAddress(host, port));
        SSLEncryption ssl = new SSLEncryption(cert, passwd);

        SSLContext context = ssl.context;
        if(context != null) {
            middleware.setWebSocketFactory(new DefaultSSLWebSocketServerFactory(ssl.context));
        } else {
            System.out.println("Error de certificado");
        }
        middleware.setConnectionLostTimeout(30);
        middleware.start();
    }

}
