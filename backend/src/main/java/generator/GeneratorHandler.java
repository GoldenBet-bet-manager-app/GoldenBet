package generator;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Map;
import static gr.goldenbet.domain.SHA256Generator.*;

public class GeneratorHandler extends Thread{


    private Socket socket;
    private Map<String, RandomNumberGenerator> generators;
    private Map<String, String> secrets;

    public GeneratorHandler(Socket socket, Map<String, RandomNumberGenerator> generators,
                            Map<String, String> secrets){

        this.socket = socket;
        this.generators = generators;
        this.secrets = secrets;
    }

    @Override
    public void run(){

        try(ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

            byte command = in.readByte();
            String gameName = (String) in.readObject();

            if(command == 0){

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
            }else{

                String hashKey = (String) in.readObject();

                RandomNumberGenerator gen = new RandomNumberGenerator(20);
                gen.start();

                generators.put(gameName, gen);
                secrets.put(gameName, hashKey);
                System.out.println("New game registered: " + gameName);
            }

        }catch (Exception e){
            System.err.println("Handler Error: " + e.getMessage());
        }
    }
}