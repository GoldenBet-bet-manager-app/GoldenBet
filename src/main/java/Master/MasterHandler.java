package Master;

import models.Game;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

import Worker.Worker;

public class MasterHandler extends Thread{
    ObjectInputStream in;
    ObjectOutputStream out;
    private ArrayList<Worker> workers;

    public MasterHandler(Socket connection,ArrayList<Worker> workers) {
        this.workers = workers;
        try {
            out = new ObjectOutputStream(connection.getOutputStream());
            in = new ObjectInputStream(connection.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void run() {

        try {

            MasterMessage message = (MasterMessage) in.readObject();
            MasterMessageType type = message.getType();

            switch (type){
                case ADD_GAME:
                    Game addedGame = (Game) message.getPayload();
                    int n = workers.size();
                    int gameWorkerIndex = hashGameToWorker(addedGame.getGameName(),n);

                    Worker target = workers.get(gameWorkerIndex);


                    Object response = sendRequestToWorker(target, message); // response returned to master

                    out.writeObject(response); // response returned to manager
                    out.flush();



                    break;
                case REMOVE_GAME:
                case SEARCH:
                case PLAY:
                case ADD_BALANCE:
                case STATS:

            }


        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                in.close();
                out.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }


    }


    private Object sendRequestToWorker(Worker target, MasterMessage message) {

        Socket requestSocket = null;
        ObjectOutputStream outWorker = null;
        ObjectInputStream inWorker = null;

        try {
            requestSocket = new Socket(target.getIp(), target.getPort());
            outWorker = new ObjectOutputStream(requestSocket.getOutputStream());
            inWorker = new ObjectInputStream(requestSocket.getInputStream());

            outWorker.writeObject(message);
            outWorker.flush();

            return inWorker.readObject();

        } catch (UnknownHostException unknownHost) {
            System.err.println("You are trying to connect to an unknown host!");
        } catch (IOException ioException) {
            ioException.printStackTrace();
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            try {

                in.close();	out.close();
                requestSocket.close();

            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }

    }


    private int hashGameToWorker(String gameName,int numbersofWorkers){
        int hash=Math.abs ( gameName.hashCode () );
        return hash % numbersofWorkers;
    }
}
