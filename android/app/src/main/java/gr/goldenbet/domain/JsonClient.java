package gr.goldenbet.domain;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

            // Use of write/readObject instead of write/readUTF
            // to handle bigger messages since game Objects contain png files

            out.writeObject(jsonRequest);
            out.flush();

            jsonResponse = (String) in.readObject();

        } catch (UnknownHostException unknownHost) {
            System.err.println("You are trying to connect to an unknown host!");
        } catch (IOException e) {
            System.err.println("Σφάλμα I/O στην επικοινωνία με την πόρτα " + port);
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }


    public String getJsonResponse() {
        return jsonResponse;
    }
}