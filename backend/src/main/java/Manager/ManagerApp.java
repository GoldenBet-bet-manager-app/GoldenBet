package Manager;


import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.*;
import java.nio.file.Files;

import Models.Game;
import Models.MasterMessage;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.json.JSONException;
import org.json.JSONObject;
import Client.*;
import utils.ConfigLoader;

import static Models.MasterMessageType.*;


public class ManagerApp {

    private static Scanner scanner;
    private static String masterIp;
    private static int masterPort;
    private static HashMap<String, String> gamescategories = new HashMap<>();

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
            System.out.println ( "1. Display games" );
            System.out.println ( "2. Add game" );
            System.out.println ( "3. Remove game" );
            System.out.println ( "4. Modify game" );
            System.out.println ( "5. Display Profit/Loss per game" );
            System.out.println ( "6. Display Profit/Loss per player (for all players)");
            System.out.println ( "7. Display Profit/Loss per player (for a particular player)");
            System.out.println ( "8. Display Profit/Loss per provider" );
            System.out.println ( "0. Exit" );
            System.out.print ( "Select an action: " );
            String choice = scanner.nextLine();
            ArrayList<File> jsonFiles;

            switch (choice){
                //**************EXIT***************
                case "0":
                    running=false;
                    break;
                //************DISPLAY GAMES*************
                case "1":
                    getAndDisplayAllGames();
                    break;
                //****************ADD GAME****************
                case "2":
                    addGame();
                    break;
                //****************REMOVE GAME****************
                case "3":
                    removeGame();
                    break;
                //****************MODIFY GAME****************
                case "4":
                    modifyGame();
                    break;
                //****************PROFITS/LOSS PER GAME****************
                case "5":
                    profitsPerGame();
                    break;
                //****************PROFITS/LOSS PER PLAYER****************
                // For all Player and for a particular one, depending on choice
                case "6","7":
                    profitsPerPlayer(choice);
                    break;
                //****************PROFITS/LOSS PER PROVIDER****************
                case "8":
                    profitsPerProvider();
                    break;
                default:
                    System.out.println("Wrong choice, type a valid operation number (0-6)");
                    break;
            }
        }
        scanner.close();
    }

    public static void addGame() {
        ArrayList<File> jsonFiles = showGamesList("src/main/resources/games", "Add");

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
                    jsonObject.getFloat("Stars"),
                    jsonObject.getInt("NoOfVotes"),
                    jsonObject.getString("GameLogo"),
                    jsonObject.getBigDecimal("MinBet"),
                    jsonObject.getBigDecimal("MaxBet"),
                    jsonObject.getString("RiskLevel"),
                    jsonObject.getString("HashKey")
            );




        } catch (IOException e) {
            System.err.println("Error reading JSON file: " + e.getMessage());
        } catch (JSONException e) {
            System.err.println("Error in JSON's structure: " + e.getMessage());
        }

        if(selectedGame != null) {
            try {
                String categoryName = "";
                if( gamescategories.containsKey ( selectedGame.getGameName ()) ){
                    categoryName=gamescategories.get ( selectedGame.getGameName () );
                    selectedGame.setGameCategory(categoryName);
                    System.out.println("The game '" + selectedGame.getGameName() + "' automatically retained its previous category: " + categoryName);
                }
                else {
                    String categoryChoice = "";
                    while (true) {
                        System.out.println ( "GAME'S CATEGORY:" );
                        System.out.println ( "1.FRUIT" );
                        System.out.println ( "2.WHEEL" );
                        System.out.println ( "3.POKER" );
                        System.out.print ( "Select the game's category (from 1 to 3):" );
                        categoryChoice = scanner.nextLine ( ).trim ( );
                        if ( categoryChoice.equals ( "1" ) || categoryChoice.equals ( "2" ) || categoryChoice.equals ( "3" ) )
                            break;
                        else
                            System.out.println ( "Invalid category! Please select 1, 2, or 3." );
                    }
                    switch (categoryChoice) {
                        case "1":
                            categoryName = "FRUIT";
                            break;
                        case "2":
                            categoryName = "WHEEL";
                            break;
                        case "3":
                            categoryName = "POKER";
                            break;
                    }
                    System.out.println ( "The game '" + selectedGame.getGameName ( ) + "' was assigned to category: " + categoryName );
                    selectedGame.setGameCategory ( categoryName );
                    gamescategories.put ( selectedGame.getGameName (),categoryName );
                }
                String imagePath = selectedGame.getGameLogoPath();
                File imageFile = new File(imagePath);

                if (imageFile.exists()) {

                    byte[] imageBytes = Files.readAllBytes(imageFile.toPath());
                    String encodedImage = Base64.getEncoder().encodeToString(imageBytes);
                    selectedGame.setBase64Image(encodedImage);
                    System.out.println("logo of game '"+selectedGame.getGameName()+"' found and encoded successfully.");

                } else {
                    System.out.println("Warning: Logo file not found at '" + imagePath + "'");
                    return;
                }

                MasterMessage message = new MasterMessage(ADD_GAME, selectedGame);

                Gson gson = new GsonBuilder()
                        .disableHtmlEscaping() // to appear normally escape character and not their Unicode sequences
                        .create();

                String jsonMessage = gson.toJson(message);
                JsonClient client = new JsonClient (masterIp, masterPort, jsonMessage);
                client.start();
                client.join();

                String responseToGameAddition = client.getJsonResponse();
                System.out.println(responseToGameAddition);

            } catch(InterruptedException e){
                System.err.println("Thread interrupted at client.join() logic: " + e.getMessage());
            } catch (Exception e) {
                System.err.println("Σφάλμα κατά την επικοινωνία: " + e.getMessage());
            }
        }
    }


    public static void removeGame() {

        // Query for the active games of all workers
       ArrayList<Game> activeGames = getAndDisplayActiveGames();

       if(activeGames == null || activeGames.isEmpty()) return;
       int index = validateGameSelectionInput(activeGames);

       Game gameToDelete = activeGames.get(index);

       MasterMessage removalMessage = new MasterMessage(REMOVE_GAME, gameToDelete);

       Gson gson = new GsonBuilder()
                .disableHtmlEscaping() // to appear normally escape character and not their Unicode sequences
                .create();
       String jsonMessage = gson.toJson(removalMessage);
       JsonClient client = new JsonClient (masterIp, masterPort, jsonMessage);

       client.start();

        try {
            client.join();
            String responseToGameRemoval = (String) client.getJsonResponse();
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
            System.out.println("\n--- Game Modification: " + gameToModify.getGameName() + " ---");
            System.out.println("1. Provider name (Current: " + gameToModify.getProviderName() + ")");
            System.out.println("2. Risk level (Current: " + gameToModify.getRiskLevel() + ")");
            System.out.println("3. Min Bet (Current: " + gameToModify.getMinBet() + ")");
            System.out.println("4. Max Bet (Current: " + gameToModify.getMaxBet() + ")");
            System.out.println("5. Logo Path (Path: " + gameToModify.getGameLogoPath() + ")");
            System.out.println("6. Save changes and return to menu");
            System.out.println("0. Cancel changes and return to menu");
            System.out.print("Select field to modify: ");

            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    System.out.print("New provider name: ");
                    gameToModify.setProviderName(scanner.nextLine());
                    break;

                case "2":
                    System.out.print("New risk level (low, medium, high): ");
                    String newRisk = scanner.nextLine().toLowerCase();
                    if (newRisk.equals("low") || newRisk.equals("medium") || newRisk.equals("high")) {
                        gameToModify.setRiskLevel(newRisk);
                    } else {
                        System.out.println("Invalid risk value.");
                        return;
                    }
                    break;

                case "3":
                    System.out.print("New min bet (e.g. 0.1, 1, 5): ");
                    try {
                        java.math.BigDecimal min = new java.math.BigDecimal(scanner.nextLine());
                        gameToModify.setMinBet(min);
                    } catch (NumberFormatException e) {
                        System.out.println("Non vallid number.");
                        return;
                    }
                    break;

                case "4":
                    System.out.print("New max bet: ");
                    try {
                        gameToModify.setMaxBet(new java.math.BigDecimal(scanner.nextLine()));
                    } catch (NumberFormatException e) {
                        System.out.println("Non valid number.");
                        return;
                    }
                    break;

                case "5":
                    System.out.print("New logo path: ");
                    gameToModify.setGameLogoPath(scanner.nextLine());
                    break;
                case "6":
                    endModify = true;
                    System.out.println("Changes were saved.");
                    break;
                case "0":
                    System.out.println("Changes were canceled.");
                    return;

                default:
                    System.out.println("Wrong choice.");
                    return;
            }
        }

        MasterMessage modificationMessage = new MasterMessage(MODIFY_GAME, gameToModify);

        Gson gson = new GsonBuilder()
                .disableHtmlEscaping() // to appear normally escape character and not their Unicode sequences
                .create();
        String jsonMessage = gson.toJson(modificationMessage);
        JsonClient client = new JsonClient (masterIp, masterPort, jsonMessage);


        client.start();

        String responseToGameModification = null;
        try {
            client.join();
            responseToGameModification = (String) client.getJsonResponse();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println(responseToGameModification);

    }


    public static void profitsPerGame(){
        MasterMessage gameAggregationMessage = new MasterMessage(GAMES_PROFITS,null);

        Gson gson = new GsonBuilder()
                .disableHtmlEscaping() // to appear normally escape character and not their Unicode sequences
                .create();
        String jsonMessage = gson.toJson(gameAggregationMessage);
        JsonClient client = new JsonClient (masterIp, masterPort, jsonMessage);


        client.start();

        String responseGameProfits = null;

        try {
            client.join();
            responseGameProfits =  client.getJsonResponse() ;

        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // type object that describes HashMap<String,BigDecimal>
        Type listType = new TypeToken<HashMap<String,BigDecimal>>(){}.getType();

        // Use of this type to convert JSON String to HashMap<String,BigDecimal>
        HashMap<String,BigDecimal> gameProfits = gson.fromJson(responseGameProfits, listType);


        if(gameProfits != null && !gameProfits.isEmpty()){
            int i=0;
            System.out.println("*******************PROFITS PER GAME*******************");
            System.out.println("------------------------------------------------------");

            String gameFormat = "%-8s | %-20s | %-15s%n";
            System.out.printf(gameFormat, "Number", "Name", "Profit/Loss");

            for (String name : gameProfits.keySet()){
                String profitStr = gameProfits.get(name) + " FUN";
                System.out.printf(gameFormat, ++i, name, profitStr);            }
        }
        else{
            System.out.println("No games were found");
        }


    }


    public static void profitsPerPlayer(String choice){
        MasterMessage playerAggregationMessage = new MasterMessage(PLAYERS_PROFITS,null);

        Gson gson = new GsonBuilder()
                .disableHtmlEscaping() // to appear normally escape character and not their Unicode sequences
                .create();
        String jsonMessage = gson.toJson(playerAggregationMessage);
        JsonClient client = new JsonClient (masterIp, masterPort, jsonMessage);


        client.start();

        String responsePlayersProfits = null;

        try {
            client.join();

            responsePlayersProfits = client.getJsonResponse();

        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        // type object that describes HashMap<String,BigDecimal>
        Type listType = new TypeToken<HashMap<String,BigDecimal>>(){}.getType();

        // Use of this type to convert JSON String to HashMap<String,BigDecimal>
        HashMap<String,BigDecimal> playersProfits = gson.fromJson(responsePlayersProfits, listType);


        if(playersProfits != null && !playersProfits.isEmpty()){
            String format;
            String profitStr;

            // Stats for all Players
            if (choice.equals("6")) {
                int i=0;
                System.out.println("*******************PROFITS/LOSS PER PLAYER*******************");
                System.out.println("--------------------------------------------------------");

                format = "%-7s | %-15s | %-15s%n";
                System.out.printf(format, "Number", "Player Id", "Profit/Loss");

                for (String playerId : playersProfits.keySet()){
                    profitStr = playersProfits.get(playerId) + " FUN";
                    System.out.printf(format, ++i, playerId, profitStr);            }
            }
            // Stats for a particular one
            else if (choice.equals("7")) {
                System.out.print("Please, type player's id: ");
                String playerName = ManagerApp.scanner.nextLine();

                System.out.printf("*******************PROFITS/LOSS for %s*******************\n",playerName);
                System.out.println("--------------------------------------------------------");

                format = "%-15s | %-15s%n";
                System.out.printf(format, "Player Id", "Profit/Loss");

                profitStr = playersProfits.get(playerName) + " FUN";
                System.out.printf(format, playerName, profitStr);
            }
        }

        else{
            System.out.println("No players were found");
        }


    }


    public static void profitsPerProvider(){
        System.out.print("Please, type provider's name: ");
        String providerName = ManagerApp.scanner.nextLine();

        MasterMessage providerAggregationMessage = new MasterMessage(PROVIDER_GAME_PROFITS,providerName);

        Gson gson = new GsonBuilder()
                .disableHtmlEscaping() // to appear normally escape character and not their Unicode sequences
                .create();
        String jsonMessage = gson.toJson(providerAggregationMessage);
        JsonClient client = new JsonClient (masterIp, masterPort, jsonMessage);


        client.start();

        String responseProviderGameProfits = null;

        try {
            client.join();
            responseProviderGameProfits =  client.getJsonResponse() ;

        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        // type object that describes HashMap<String,BigDecimal>
        Type listType = new TypeToken<HashMap<String,BigDecimal>>(){}.getType();

        // Use of this type to convert JSON String to HashMap<String,BigDecimal>
        HashMap<String,BigDecimal> providerGameProfits = gson.fromJson(responseProviderGameProfits, listType);


        if(providerGameProfits != null && !providerGameProfits.isEmpty()){
            int i=0;
            System.out.printf("*******************PROFITS OF PROVIDER \"%s\" *******************\n",providerName);
            System.out.println("--------------------------------------------------------------------");

            String gameFormat = "%-8s | %-20s | %-15s%n";
            System.out.printf(gameFormat, "Number", "Name", "Profit/Loss");

            for (String gameName : providerGameProfits.keySet()){
                String profitStr = providerGameProfits.get(gameName) + " FUN";
                System.out.printf(gameFormat, ++i, gameName, profitStr);
            }
        }
        else{
            System.out.println("No games were found for provider's Name: \"" + providerName +"\"");
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

        Gson gson = new GsonBuilder()
                .disableHtmlEscaping() // to appear normally escape character and not their Unicode sequences
                .create();
        String jsonMessage = gson.toJson(message);
        JsonClient client = new JsonClient (masterIp, masterPort, jsonMessage);

        client.start();

        String responseActiveGames = null;
        // Wait for the client thread to finish remove game before returning to manager's main menu
        try {
            client.join();

            responseActiveGames = client.getJsonResponse();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // type object that describes ArrayList<Game>
        Type listType = new TypeToken<ArrayList<Game>>(){}.getType();

        // Use of this type to convert JSON String to ArrayList<Game>
        ArrayList<Game> activeGames = gson.fromJson(responseActiveGames, listType);


        if (activeGames != null && !activeGames.isEmpty()) {
            System.out.println("\t\t\t\t\t ACTIVE GAMES");
            System.out.println("----------------------------------------------------");

            String format = "%-8s | %-25s | %-20s%n";
            System.out.printf(format, "Number", "Name", "ProviderName");

            for(int i=0;i<activeGames.size();i++){
                Game curGame = activeGames.get(i);

                System.out.printf(format, (i + 1), curGame.getGameName(), curGame.getProviderName());
            }
        }
        else {
            System.out.println("There are no active games to modify/delete");
            return null;
        }


        return activeGames;
    }


    private static ArrayList<Game> getAndDisplayAllGames(){
        MasterMessage message = new MasterMessage(GET_ALL_GAMES, null);

        Gson gson = new GsonBuilder()
                .disableHtmlEscaping() // to appear normally escape character and not their Unicode sequences
                .create();

        String jsonMessage = gson.toJson(message);
        JsonClient client = new JsonClient (masterIp, masterPort, jsonMessage);

        client.start();

        String responseAllGames = null;
        // Wait for the client thread to finish remove game before returning to manager's main menu
        try {
            client.join();

            responseAllGames = client.getJsonResponse();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // type object that describes ArrayList<Game>
        Type listType = new TypeToken<ArrayList<Game>>(){}.getType();

        // Use of this type to convert JSON String to ArrayList<Game>
        ArrayList<Game> allGames = gson.fromJson(responseAllGames, listType);


        if (allGames != null && !allGames.isEmpty()) {
            System.out.println("\t\t\t\t\t ALL GAMES STORED");
            System.out.println("-----------------------------------------------------------------");

            String format = "%-8s | %-22s | %-18s | %-10s%n";
            System.out.printf(format, "Number", "Name", "ProviderName", "Status");

            for(int i=0;i<allGames.size();i++){
                Game curGame = allGames.get(i);
                String status = curGame.isActive()?"active": "inactive";

                System.out.printf(format, (i + 1), curGame.getGameName(), curGame.getProviderName(), status);
            }
        }
        else {
            System.out.println("There are no (active or inactive) games stored");
            return null;
        }

        return allGames;
    }

    public static void main(String[] args) {
        ManagerApp.scanner = new Scanner(System.in);

        // load servers configurations
        ConfigLoader.loadConfig("src/main/resources/system_network_config.conf");

        // Get Master's ip and port
        ManagerApp.masterIp = ConfigLoader.getIp("MASTER");
        ManagerApp.masterPort = ConfigLoader.getPort("MASTER");

        startConsoleMenu ();
    }

}