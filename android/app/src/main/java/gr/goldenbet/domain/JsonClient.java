package gr.goldenbet.domain;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

public class JsonClient extends Thread {
    private String ip;
    private int port;
    private String jsonRequest;
    private String jsonResponse;

    public JsonClient(String ip, int port, String jsonRequest) {
        this.ip = ip;
        this.port = port;
        this.jsonRequest = jsonRequest;
    }

    @Override
    public void run() {
        try (Socket socket = new Socket(ip, port);
             DataOutputStream out = new DataOutputStream(socket.getOutputStream());
             DataInputStream in = new DataInputStream(socket.getInputStream())) {

            out.writeUTF(jsonRequest);
            out.flush();
            jsonResponse = in.readUTF();

        } catch (UnknownHostException unknownHost) {
            System.err.println("You are trying to connect to an unknown host!");
        } catch (IOException e) {
            System.err.println("Σφάλμα I/O στην επικοινωνία με την πόρτα " + port);
            e.printStackTrace();
        }
    }


    public String getJsonResponse() {
        return jsonResponse;
    }
}