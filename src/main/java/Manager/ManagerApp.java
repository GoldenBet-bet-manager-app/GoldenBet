package Manager;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;
import java.nio.file.Files;

import Master.MasterHandler;
import models.Game;
import models.MasterMessage;
import org.json.JSONException;
import org.json.JSONObject;
import Client.Client;

import static models.MasterMessageType.*;


public class ManagerApp {

    private static Scanner scanner;


    private static ArrayList<File> showGamesList (String folderPath, String operation) {
        File folder = new File ( folderPath );

        // Filtering so that only json files are visible
        File[] filesArray = folder.listFiles ( (dir, name) -> name.toLowerCase ( ).endsWith ( ".json" ) );

        ArrayList<File> files = new ArrayList<> ( Arrays.asList ( filesArray ) );

        System.out.println ( );
        if ( files.isEmpty ( ) ) {
            System.out.println ( "There are no json files of game to be loaded in folder " + folderPath );
            return null;
        }

        System.out.println ( "Available game files to :" + operation );
        System.out.println ( "-----------------------------------------" );
        for ( int i = 0; i < files.size ( ); i++ ) {
            System.out.println ( (i + 1) + ". " + files.get ( i ).getName ( ) );
        }
        System.out.println ( "-----------------------------------------" );
        return files;
    }



    public static void startConsoleMenu() {
        Scanner scanner = ManagerApp.scanner;
        boolean running = true;
        Client client;
        MasterMessage message;

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
                    addGame();
                    break;
                //****************REMOVE GAME****************
                case "2":
                    removeGame();
                    break;
                //****************MODIFY GAME****************
                case "3":
                    modifyGame();
                    break;
                //****************PROFITS/LOSS PER GAME****************
                case "4":

                    break;
                //****************PROFITS/LOSS PER PLAYER****************
                case "5":

                    break;
                //****************PROFITS/LOSS PER PROVIDER****************
                case "6":
                    profitsPerProvider();
                    break;
                default:
                    System.out.println("Λανθασμένη επιλογή, δώστε τον αριθμό λειτουργίας (0-6)");
                    break;
            }
        }
        scanner.close();
    }

    public static void addGame() {
        ArrayList<File> jsonFiles = showGamesList("games", "Add");

        // When there are no json files in games folder
        if (jsonFiles == null){return;}
        int index = validateGameSelectionInput(jsonFiles);
        // The selected game
        File selectedGameFile = jsonFiles.get(index);
        Game selectedGame = null;

        // ----------------Parsing file and creating Game object-----------------
        try {
            byte[] bytes = Files.readAllBytes(selectedGameFile.toPath());
            String fileContent = new String(bytes);

            JSONObject jsonObject = new org.json.JSONObject(fileContent);

            selectedGame = new Game(
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
            } catch (IOException e) {
                System.err.println("Σφάλμα κατά την ανάγνωση του αρχείου: " + e.getMessage());
            } catch (JSONException e) {
                System.err.println("Σφάλμα στη δομή του JSON: " + e.getMessage());
            }


        if(selectedGame != null) {
            try {
                MasterMessage message = new MasterMessage(ADD_GAME, selectedGame);

                Client client = new Client("127.0.0.1", 43210, message);
                client.start();

                // Wait for the client thread to finish add game before returning to manager's main menu
                client.join();
                String responseToGameAddition = (String) client.getResponse();
                System.out.println(responseToGameAddition);

            }catch(InterruptedException e){
                    System.err.println("Thread interrupted at client.join() logic: " + e.getMessage());
            }
        }

    }

    public static void removeGame() {

        // Query for the active games of all workers
        ArrayList<Game> activeGames = getAndDisplayActiveGames();

        if(activeGames == null) return;
        int index = validateGameSelectionInput(activeGames);

        Game gameToDelete = activeGames.get(index);

       MasterMessage removalMessage = new MasterMessage(REMOVE_GAME, gameToDelete);

       Client client = new Client("127.0.0.1", 43210, removalMessage);
       client.start();

        try {
            client.join();
            String responseToGameRemoval = (String) client.getResponse();
            System.out.println(responseToGameRemoval);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    public static void modifyGame(){
        ArrayList<Game> activeGames = getAndDisplayActiveGames();

        if(activeGames == null) return;
        int index = validateGameSelectionInput(activeGames);

        Game gameToModify = activeGames.get(index);
        boolean endModify = false;

        while(!endModify) {
            System.out.println("\n--- Τροποποίηση Παιχνιδιού: " + gameToModify.getGameName() + " ---");
            System.out.println("1. Όνομα Παρόχου (Τρέχον: " + gameToModify.getProviderName() + ")");
            System.out.println("2. Επίπεδο Ρίσκου (Τρέχον: " + gameToModify.getRiskLevel() + ")");
            System.out.println("3. Ελάχιστο Ποντάρισμα (Τρέχον: " + gameToModify.getMinBet() + ")");
            System.out.println("4. Μέγιστο Ποντάρισμα (Τρέχον: " + gameToModify.getMaxBet() + ")");
            System.out.println("5. Path Λογοτύπου (Τρέχον: " + gameToModify.getGameLogo() + ")");
            System.out.println("6. Αποθήκευση αλλαγών και επιστροφή στο Μενού");
            System.out.println("0. Ακύρωση αλλαγών και επιστροφή στο Μενού");
            System.out.print("Επιλέξτε πεδίο προς αλλαγή: ");

            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    System.out.print("Νέο όνομα παρόχου: ");
                    gameToModify.setProviderName(scanner.nextLine());
                    break;

                case "2":
                    System.out.print("Νέο επίπεδο ρίσκου (low, medium, high): ");
                    String newRisk = scanner.nextLine().toLowerCase();
                    if (newRisk.equals("low") || newRisk.equals("medium") || newRisk.equals("high")) {
                        gameToModify.setRiskLevel(newRisk);
                    } else {
                        System.out.println("Άκυρη τιμή ρίσκου.");
                        return;
                    }
                    break;

                case "3":
                    System.out.print("Νέο ελάχιστο ποντάρισμα (π.χ. 0.1, 1, 5): ");
                    try {
                        java.math.BigDecimal min = new java.math.BigDecimal(scanner.nextLine());
                        gameToModify.setMinBet(min);
                    } catch (NumberFormatException e) {
                        System.out.println("Μη έγκυρος αριθμός.");
                        return;
                    }
                    break;

                case "4":
                    System.out.print("Νέο μέγιστο ποντάρισμα: ");
                    try {
                        gameToModify.setMaxBet(new java.math.BigDecimal(scanner.nextLine()));
                    } catch (NumberFormatException e) {
                        System.out.println("Μη έγκυρος αριθμός.");
                        return;
                    }
                    break;

                case "5":
                    System.out.print("Νέο path λογοτύπου: ");
                    gameToModify.setGameLogo(scanner.nextLine());
                    break;
                case "6":
                    endModify = true;
                    break;
                case "0":
                    System.out.println("Οι αλλαγές ακυρώθηκαν.");
                    return;

                default:
                    System.out.println("Λανθασμένη επιλογή.");
                    return;
            }
        }

        MasterMessage modificationMessage = new MasterMessage(MODIFY_GAME, gameToModify);


        Client client = new Client("127.0.0.1", 43210, modificationMessage);
        client.start();

        String responseToGameModification = null;
        try {
            client.join();
            responseToGameModification = (String) client.getResponse();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println(responseToGameModification);

    }

    public static void profitsPerProvider(){
        System.out.print("Please, type provider's name: ");
        String providerName = ManagerApp.scanner.nextLine();

        MasterMessage providerAggregationMessage = new MasterMessage(PROVIDER_GAME_PROFITS,providerName);
        Client client = new Client("127.0.0.1", 43210, providerAggregationMessage);
        client.start();

        HashMap<String,Integer> providerGameProfits = null;

        try {
            client.join();
            providerGameProfits = (HashMap<String, Integer>) client.getResponse() ;

        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if(providerGameProfits != null && !providerGameProfits.isEmpty()){
            int i=0;
            System.out.println("Number " + "| " + "Name " + "| " + " ProviderName");
            for (String gameName : providerGameProfits.keySet()){
                System.out.println(++i + "| "+ gameName + "| " + providerGameProfits.get(gameName));
            }
        }
        else{
            System.out.println("No games were found for provider's Name: " + providerName);
        }

    }

    private static int validateGameSelectionInput(ArrayList<?> list){
        System.out.printf("\n Please select one of the above games (from 1 to %d): ",list.size());
        String gameChoiceStr = scanner.nextLine();
        int index;

        while (true) {
            // check whether gameChoiceStr is actually a number
            if ( gameChoiceStr.matches ( "\\d+" ) ) {
                int gameChoiceInt = Integer.parseInt ( gameChoiceStr );

                if ( gameChoiceInt > 0 && gameChoiceInt <= list.size() ) {
                    index = gameChoiceInt - 1;
                    break;
                } else {
                    System.out.print ( "Wrong number! Please type from 1 to " + list.size ( ) + ": " );
                    gameChoiceStr = scanner.nextLine ( );
                }
            } else {
                System.out.print ( "Invalid! Type only a NUMBER (e.g. 1): " );
                gameChoiceStr = scanner.nextLine ( );
            }
        }

       return index;
    }


    private static ArrayList<Game> getAndDisplayActiveGames(){
        MasterMessage message = new MasterMessage(GET_ACTIVE_GAMES, null);

        Client client = new Client("127.0.0.1",43210,message);

        client.start();

        Object responseActiveGames = null;
        // Wait for the client thread to finish remove game before returning to manager's main menu
        try {
            client.join();

            responseActiveGames = client.getResponse();

        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        ArrayList<Game> activeGames = (ArrayList<Game>) responseActiveGames;

        if (activeGames != null && !activeGames.isEmpty()) {
            System.out.println("\t\t ACTIVE GAMES");
            System.out.println("Number " + "| " + "Name " + "| " + " ProviderName");
            for(int i=0;i<activeGames.size();i++){
                Game curGame = activeGames.get(i);
                System.out.println((i+1) + "|  " + curGame.getGameName() + "|  " + curGame.getProviderName());
            }
        }
        else {
            System.out.println("There are no active games to modify");
            return null;
        }

        return activeGames;
    }

    public static void main(String[] args) {
        ManagerApp.scanner = new Scanner(System.in);
        startConsoleMenu ();
    }

}