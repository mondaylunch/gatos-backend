package club.mondaylunch.gatos.basicnodes.test;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.Scanner;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class HttpMockServer {

    public static void start() throws Exception{
        HttpServer httpServer = HttpServer.create(new InetSocketAddress(8000), 0);
        httpServer.createContext("/test", new HttpRequestHandler());
        httpServer.setExecutor(null);
        httpServer.start();
    }

    static class HttpRequestHandler implements HttpHandler {
        private static final int HTTP_OK_STATUS = 200;
    
        @Override
        public void handle(HttpExchange t) throws IOException {
            InputStream requestBody = t.getRequestBody();
            String response = this.createResponse(requestBody);
            
            t.sendResponseHeaders(HTTP_OK_STATUS, response.getBytes().length);
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    
        private String createResponse(InputStream body) {
            try (Scanner scanner = new Scanner(body).useDelimiter("\\A")) {
                String requestBody = scanner.hasNext() ? scanner.next() : "";

                if ( requestBody.equals("")) {
                    return "there was a query sent: " + requestBody;
                }
            }
            return "no query was sent :(";
        }
    }
}