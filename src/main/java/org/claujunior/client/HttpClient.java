package org.claujunior.client;


import org.claujunior.heartbeat.TimeoutBasedFailureDetector;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;

public class HttpClient {
    private static final HttpClient instance = new HttpClient();

    private HttpClient(){};

    public static HttpClient getInstance() {
        return instance;
    }
    private TimeoutBasedFailureDetector<String> executor = TimeoutBasedFailureDetector.getInstance();

    public String request(String recurso, String httpMethod, String httpVersion,String json,String selecao) {
        String response = "";
        try {
            InetAddress serverInetAddress = InetAddress.getByName(executor.choice(selecao));
            Socket connection = new Socket(serverInetAddress, 8080);

            try (OutputStream out = connection.getOutputStream();
                 BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                sendGet(out,recurso,httpMethod,httpVersion,json);
                response = getResponse(in);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return response;
    }

    private void sendGet(OutputStream out,String recurso, String httpMethod, String httpVersion,String json) {
        try {
            out.write(("POST " + recurso + " " + httpVersion + "\r\n").getBytes());
            out.write("Host: localhost\r\n".getBytes());
            out.write("\r\n".getBytes());
            out.write(json.getBytes());
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private String getResponse(BufferedReader in) {
        try {
            String inputLine;
            StringBuilder response = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine).append("\n");
            }
            return response.toString();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return "";
    }

}
