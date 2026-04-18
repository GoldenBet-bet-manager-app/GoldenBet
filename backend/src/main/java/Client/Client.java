package Client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client extends Thread {
    private String ip;
    private int port;
    private Object object;
    private Object response;

    public Client (String ip, int port, Object object) {
        this.ip = ip;
        this.port = port;
        this.object = object;
    }

    public void run ( ) {

        try (Socket socket = new Socket(ip, port);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

            out.writeObject ( object );
            out.flush ( );
            response = in.readObject();


        } catch (UnknownHostException unknownHost) {
            System.err.println ( "You are trying to connect to an unknown host!" );
        } catch (IOException e) {
            System.err.println ( "I/O error in communication with port " + port );
            e.printStackTrace ( );
        } catch (ClassNotFoundException e) {
            System.err.println ( "The class of the read object was not found!" );
            e.printStackTrace ( );
        }
    }

    public Object getObject ( ) {
        return object;
    }

    public Object getResponse ( ) {
        return response;
    }
}