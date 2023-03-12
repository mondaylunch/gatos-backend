package club.mondaylunch.gatos.basicnodes.test;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.Scanner;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

public class HttpMockServer {

    public static void start() throws Exception {
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
            String method = t.getRequestMethod();

            String response = this.createResponse(requestBody, method);

            t.sendResponseHeaders(HTTP_OK_STATUS, response.getBytes().length);
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }

        private String createResponse(InputStream body, String method) {
            String response = "";

            switch (method) {
                case "GET": response += "GET request"; break;
                case "POST": response += "POST request"; break;
                case "PUT": response += "PUT request"; break;
                case "DELETE": response += "DELETE request"; break;
            }

            try (Scanner scanner = new Scanner(body).useDelimiter("\\A")) {

                String requestBody = scanner.hasNext() ? scanner.next() : null;

                if (requestBody != null) {
                    return response + " request has a body: " + requestBody;
                }
            }
            return response;
        }
    }
}
