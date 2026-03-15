package generator;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Scanner;

public class TestGenerator {

    public static void main(String[] args){

        try {
            Socket s = new Socket("127.0.0.1", 5000);

            ObjectOutputStream out = new
                    ObjectOutputStream(s.getOutputStream());
            ObjectInputStream in = new
                    ObjectInputStream(s.getInputStream());

            out.writeObject("Fruit Fiesta");
            out.flush();

            int number = in.readInt();
            String response = (String) in.readObject();

            if (number == -1) {
                System.out.println("Σφάλμα από τον Server: " + response);
            } else {
                System.out.println("Επιτυχία!");
                System.out.println("Τυχαίος Αριθμός: " + number);
                System.out.println("Hash: " + response);
            }

            s.close();

            Scanner sc = new Scanner(System.in);
            System.out.print("δωσε ονομα παιχνιδου: ");
            String name = sc.nextLine();

            while (name != "finish"){
               s = new Socket("127.0.0.1", 5000);
               out= new ObjectOutputStream(s.getOutputStream());
               in = new ObjectInputStream(s.getInputStream());

               out.writeObject(name);
               out.flush();

               number = in.readInt();
               response = (String) in.readObject();

               if (number == -1) {
                   System.out.println("Σφάλμα από τον Server: " + response);
               } else {
                   System.out.println("Επιτυχία!");
                   System.out.println("Τυχαίος Αριθμός: " + number);
                   System.out.println("Hash: " + response);
               }

               s.close();

                System.out.print("δωσε ονομα παιχνιδου: ");
                name = sc.nextLine();
            }

        }catch (Exception e){
            System.err.println("Client Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
