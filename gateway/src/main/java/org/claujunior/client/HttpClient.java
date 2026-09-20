package org.claujunior.client;


import org.claujunior.heartbeat.TimeoutBasedFailureDetector;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class HttpClient {
    private static final HttpClient instance = new HttpClient();

    private HttpClient(){};

    public static HttpClient getInstance() {
        return instance;
    }
    private TimeoutBasedFailureDetector<InetAddress> executor = TimeoutBasedFailureDetector.getInstance();

    public String request(String recurso, String httpMethod, String httpVersion,String json,String selecao) {
        try {
            InetAddress serverInetAddress = executor.choice(selecao);
            if (serverInetAddress == null) {
                return errorResponse(503, "Service Unavailable", "Servico indisponivel: " + selecao);
            }

            int port = "investimento".equals(selecao)
                    ? Integer.getInteger("investimento.http.port", 8082)
                    : Integer.getInteger("contacorrente.http.port", 8081);

            try (Socket connection = new Socket(serverInetAddress, port);
                 OutputStream out = connection.getOutputStream();
                 InputStream in = connection.getInputStream()) {
                sendRequest(out,recurso,httpMethod,httpVersion,json);
                return getResponse(in);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            return errorResponse(502, "Bad Gateway", "Falha ao acessar " + selecao);
        }
    }

    private void sendRequest(OutputStream out,String recurso, String httpMethod, String httpVersion,String json) {
        try {
            byte[] body = json.getBytes(StandardCharsets.UTF_8);
            String headers = httpMethod + " " + recurso + " " + httpVersion + "\r\n"
                    + "Host: localhost\r\n"
                    + "Content-Length: " + body.length + "\r\n"
                    + "Connection: close\r\n\r\n";
            out.write(headers.getBytes(StandardCharsets.US_ASCII));
            out.write(body);
            out.flush();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private String getResponse(InputStream in) {
        try {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return errorResponse(502, "Bad Gateway", "Resposta invalida do servico");
    }

    private String errorResponse(int statusCode, String statusText, String message) {
        byte[] body = message.getBytes(StandardCharsets.UTF_8);
        return "HTTP/1.1 " + statusCode + " " + statusText + "\r\n"
                + "Content-Type: text/plain; charset=UTF-8\r\n"
                + "Content-Length: " + body.length + "\r\n"
                + "Connection: close\r\n\r\n"
                + message;
    }

}
