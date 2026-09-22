package org.claujunior.client;


import org.claujunior.heartbeat.TimeoutBasedFailureDetector;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;


public class HttpClient {
    private static final HttpClient instance = new HttpClient();

    private HttpClient(){};

    public static HttpClient getInstance() {
        return instance;
    }
    private TimeoutBasedFailureDetector<InetAddress> executor = TimeoutBasedFailureDetector.getInstance();

    public String request(String recurso, String httpMethod, String httpVersion,String json,String selecao) {
        InetAddress serverInetAddress = executor.choice(selecao);
        if(serverInetAddress==null){
            return null;
        }
        return request(recurso,httpMethod,httpVersion,json,selecao,serverInetAddress);
    }

    public String request(String recurso, String httpMethod, String httpVersion,String json,
                          String selecao, InetAddress serverInetAddress) {
        int port = "investimento".equals(selecao)
                ? Integer.getInteger("investimento.http.port", 8082)
                : Integer.getInteger("contacorrente.http.port", 8081);

        return request(recurso,httpMethod,httpVersion,json,selecao,serverInetAddress,port);
    }

    public String request(String recurso, String httpMethod, String httpVersion,String json,
                          String selecao, InetAddress serverInetAddress, int port) {
        try {
            if (serverInetAddress == null) {
                return errorResponse(503, "Service Unavailable", "Servico indisponivel: " + selecao);
            }

            try (Socket connection = new Socket()) {
                connection.connect(
                        new InetSocketAddress(serverInetAddress, port),
                        4500
                );

                connection.setSoTimeout(6000);

                try (OutputStream out = connection.getOutputStream();
                     InputStream in = connection.getInputStream()) {

                    sendRequest(
                            out,
                            recurso,
                            httpMethod,
                            httpVersion,
                            json
                    );

                    return getResponse(in);
                }
            }
        } catch (SocketTimeoutException e) {
            return errorResponse(
                    504,
                    "Gateway Timeout",
                    "Tempo esgotado ao acessar " + selecao
            );
        } catch (IOException e) {
            return errorResponse(
                    502,
                    "Bad Gateway",
                    "Falha ao acessar " + selecao
            );
        }
    }

    private void sendRequest(OutputStream out,String recurso, String httpMethod,
                             String httpVersion,String json) throws IOException {
        byte[] body = json.getBytes();
        String headers = httpMethod + " " + recurso + " " + httpVersion + "\r\n"
                + "Host: localhost\r\n"
                + "Content-Length: " + body.length + "\r\n"
                + "Connection: close\r\n\r\n";
        out.write(headers.getBytes());
        out.write(body);
        out.flush();
    }

    private String getResponse(InputStream in) throws IOException {
        return new String(in.readAllBytes());
    }

    private String errorResponse(int statusCode, String statusText, String message) {
        byte[] body = message.getBytes();
        return "HTTP/1.1 " + statusCode + " " + statusText + "\r\n"
                + "Content-Type: text/plain; charset=UTF-8\r\n"
                + "Content-Length: " + body.length + "\r\n"
                + "Connection: close\r\n\r\n"
                + message;
    }

}
