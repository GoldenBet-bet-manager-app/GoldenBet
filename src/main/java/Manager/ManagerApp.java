package Manager;


import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import java.nio.file.Files;

import models.Game;
import org.json.JSONException;
import org.json.JSONObject;


public class ManagerApp {

    private static File[] showGamesList(String folderPath){
        File folder = new File(folderPath);

        // Filtering so that only json files are visible
        File[] files = folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".json"));

        System.out.println();
        if (files == null || files.length == 0) {
            System.out.println("Δεν βρέθηκαν αρχεία JSON στον φάκελο " + folderPath);
            return null;
        }

        System.out.println("Διαθέσιμα αρχεία παιχνιδιών:");
        System.out.println("-----------------------------------------");
        for (int i = 0; i < files.length; i++) {
            System.out.println((i + 1) + ". " + files[i].getName());
        }
        System.out.println("-----------------------------------------");
        return files;
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
            String gameChoiceStr;
            File[] jsonFiles;

            switch (choice){
                //**************EXIT***************
                case "0":
                    running=false;
                    break;
                //************ADD GAME*************
                case "1":
                    jsonFiles = showGamesList("games");
                    if(jsonFiles == null) break;

                    System.out.print("Αριθμός παιχνιδιού: ");
                    gameChoiceStr = scanner.nextLine();
                    int index = -1;
                    while (true) {
                        // check whether gameChoiceStr is actually a number
                        if (gameChoiceStr.matches("\\d+")) {
                            int gameChoiceInt = Integer.parseInt(gameChoiceStr);

                            if (gameChoiceInt > 0 && gameChoiceInt <= jsonFiles.length) {
                                index = gameChoiceInt - 1;
                                break;
                            } else {
                                System.out.print("Λάθος αριθμός! Δώστε από 1 έως " + jsonFiles.length + ": ");
                                gameChoiceStr = scanner.nextLine();
                            }
                        } else {
                            System.out.print("Ακυρο! Δώστε μόνο αριθμό (π.χ. 1): ");
                            gameChoiceStr = scanner.nextLine();
                        }
                    }

                    File selectedGameFile = jsonFiles[index];

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

                        System.out.println(selectedGame);

                    } catch (IOException e) {
                        System.out.println("Σφάλμα κατά την ανάγνωση του αρχείου: " + e.getMessage());
                    } catch (JSONException e) {
                        System.out.println("Σφάλμα στη δομή του JSON: " + e.getMessage());
                    }

                    break;
                //************REMOVE GAME*************************
                case "2":

                //************MODIFY GAME*************************
                case "3":

                //************PROFITS/LOSS PER GAME***************
                case "4":

                //************PROFITS/LOSS PER PLAYER*************
                case "5":

                //************PROFITS/LOSS PER PROVIDER***********
                case "6":

            }
        }
        scanner.close();
    }
    public static void main(String[] args) {
        startConsoleMenu ();
    }


}