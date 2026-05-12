package gr.goldenbet.utils;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.CompletableFuture;

public class NetworkManager {

    public static CompletableFuture<String> sendRequest(String ip, int port, String jsonRequest) {

        return CompletableFuture.supplyAsync(() -> {
            try (Socket socket = new Socket(ip, port);
                 ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                 ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

                out.writeObject(jsonRequest);
                out.flush();

                return (String) in.readObject();

            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        });
    }
}
