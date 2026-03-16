package Manager;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
import java.nio.file.Files;

import models.Game;
import models.MasterMessage;
import org.json.JSONException;
import org.json.JSONObject;
import Client.Client;

import static models.MasterMessageType.ADD_GAME;


public class ManagerApp {

    private static ArrayList<File> showGamesList(String folderPath, String operation){
        File folder = new File(folderPath);

        // Filtering so that only json files are visible
        File[] filesArray = folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".json"));

        ArrayList<File> files = new ArrayList<>(Arrays.asList(filesArray));

        System.out.println();
        if (files.isEmpty()) {
            System.out.println("No JSON files were found in folder " + folderPath);
            return null;
        }

        System.out.println("Available game files to :" + operation);
        System.out.println("-----------------------------------------");
        for (int i = 0; i < files.size(); i++) {
            System.out.println((i + 1) + ". " + files.get(i).getName());
        }
        System.out.println("-----------------------------------------");
        return files;
    }

    private static int gameJSONSelection(ArrayList<File> jsonFiles, Scanner scanner){
        String gameChoiceStr;

        System.out.print("Game number: ");
        gameChoiceStr = scanner.nextLine();
        int index = -1;
        while (true) {
            // check whether gameChoiceStr is actually a number
            if (gameChoiceStr.matches("\\d+")) {
                int gameChoiceInt = Integer.parseInt(gameChoiceStr);

                if (gameChoiceInt > 0 && gameChoiceInt <= jsonFiles.size()) {
                    index = gameChoiceInt - 1;
                    break;
                } else {
                    System.out.print("Wrong number! Please type from 1 to " + jsonFiles.size() + ": ");
                    gameChoiceStr = scanner.nextLine();
                }
            } else {
                System.out.print("Invalid! Type only a number (e.g. 1): ");
                gameChoiceStr = scanner.nextLine();
            }
        }
    }

    public static void startConsoleMenu() {
        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        while (running) {
            System.out.println ( "\n=========== ΜΕΝΟΥ ΔΙΑΧΕΙΡΙΣΗΣ ===========" );
            System.out.println ( "1. Προσθήκη Παιχνιδιών" );
            System.out.println ( "2. Αφαίρεση Παιχνιδιού" );
            System.out.println ( "3. Τροποποίηση Παιχνιδιού" );
            System.out.println ( "4. Εμφάνιση κερδών/ζημιών ανά Παιχνίδι" );
            System.out.println ( "5. Εμφάνιση κερδών/ζημιών ανά Παίκτη" );
            System.out.println ( "6. Εμφάνιση κερδών/ζημιών ανά Πάροχο" );
            System.out.println ( "0. Έξοδος" );
            System.out.print ( "Επιλέξτε μια ενέργεια: " );
            String choice = scanner.nextLine();
            ArrayList<File> jsonFiles;

            switch (choice){
                //**************EXIT***************
                case "0":
                    running=false;
                    break;
                //************ADD GAME*************
                case "1":
                    jsonFiles = showGamesList("games","Add");

                    // When there are no json files in games folder
                    if(jsonFiles == null) break;

                    int index = gameJSONSelection(jsonFiles,scanner);
                    // The selected game
                    File selectedGameFile = jsonFiles.get(index);

                    // ----------------Parsing file and creating Game object-----------------
                    try {
                        byte[] bytes = Files.readAllBytes(selectedGameFile.toPath());
                        String fileContent = new String(bytes);

                        JSONObject jsonObject = new org.json.JSONObject(fileContent);

                        Game selectedGame = new Game(
                                jsonObject.getString("GameName"),
                                jsonObject.getString("ProviderName"),
                                jsonObject.getInt("Stars"),
                                jsonObject.getInt("NoOfVotes"),
                                jsonObject.getString("GameLogo"),
                                jsonObject.getBigDecimal("MinBet"),
                                jsonObject.getBigDecimal("MaxBet"),
                                jsonObject.getString("RiskLevel"),
                                jsonObject.getString("HashKey")
                                );


                        MasterMessage message = new MasterMessage(ADD_GAME, selectedGame);

                        Client client = new Client("127.0.0.1", 43210, message);
                        client.start();

                        // Wait for the client thread to finish before returning to manager's main menu
                        client.join();
                    } catch (IOException e) {
                        System.err.println("Σφάλμα κατά την ανάγνωση του αρχείου: " + e.getMessage());
                    } catch (JSONException e) {
                        System.err.println("Σφάλμα στη δομή του JSON: " + e.getMessage());
                    } catch (InterruptedException e) {
                        System.err.println("Thread interrupted at client.join() logic: " + e.getMessage());
                    }

                    break;
                //****************REMOVE GAME****************
                case "2":
                    jsonFiles = showGamesList("games","Remove");

                    // When there are no json files in games folder
                    if(jsonFiles == null) break;



                break;
                //****************MODIFY GAME****************
                case "3":

                //****************PROFITS/LOSS PER GAME****************
                case "4":

                //****************PROFITS/LOSS PER PLAYER****************
                case "5":

                //****************PROFITS/LOSS PER PROVIDER****************
                case "6":

            }
        }
        scanner.close();
    }
    public static void main(String[] args) {
        startConsoleMenu ();
    }


}