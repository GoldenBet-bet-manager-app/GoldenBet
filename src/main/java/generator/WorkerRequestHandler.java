package generator;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Map;

public class WorkerRequestHandler extends Thread{


    private Socket socket;
    private Map<String, RandomNumberGenerator> generators;
    private Map<String, String> secrets;

    public WorkerRequestHandler(Socket socket, Map<String, RandomNumberGenerator> generators,
                                    Map<String, String> secrets){

        this.socket = socket;
        this.generators = generators;
        this.secrets = secrets;
    }

    @Override
    public void run(){

        try(ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

            String gameName = (String) in.readObject();
            String hashKey = secrets.get(gameName);

            if(hashKey == null){

                out.writeInt(-1);
                out.writeObject("The game was not found");
                out.flush();
            }else{

                int randomNumber = generators.get(gameName).getNumberFromBuffer();
                String hash = computeSHA256(randomNumber + hashKey);

                out.writeInt(randomNumber);
                out.writeObject(hash);
                out.flush();
            }

        }catch (Exception e){
            System.err.println("Handler Error: " + e.getMessage());
        }

    }

    private String computeSHA256(String number) {

        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");

            byte[] hashBytes  = md.digest(number.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();

            for (byte b : hashBytes ) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();

        }catch(Exception e){
            throw new RuntimeException("SHA-256 algorithm not found!", e);
        }
    }

}