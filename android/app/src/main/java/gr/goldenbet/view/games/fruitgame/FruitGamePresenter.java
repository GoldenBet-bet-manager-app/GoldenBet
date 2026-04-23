package gr.goldenbet.view.games.fruitgame;

import java.util.Random;

import gr.goldenbet.domain.GameRules;

public class FruitGamePresenter {
    private FruitGameView view;
    private String gameName,playerId;
    private final String[] games = {"Gates of Olympus", "Fruit Fiesta", "Lucky Seven"};
    private String[] symbols;
   Random random=new Random();

    public FruitGamePresenter(FruitGameView view, String playerId, String gameName) {
        this.view = view;
        this.playerId = playerId;
        this.gameName = gameName;
    }

    private void showPattern(double multiplier) {
        view.clearFruitGrid();
        GameRules rules=GameRules.getFruitGraphics(gameName);
        if(rules.getWinPatterns().containsKey(multiplier)){
            view.setCellsToSymbol(rules.getWinPatterns().get(multiplier),rules.getDefaultSymbol(),rules.getColor());
        }
       /* if (gameName.equals(games[0])) {//Gates of olympus
            symbol = "⚡";
            if (multiplier == 40.0) {
                view.setCellsToSymbol(new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8}, symbol, Color.parseColor("#EDD606"));
            } else if (multiplier == 6.5) {
                view.setCellsToSymbol(new int[]{0, 2, 4, 6, 8}, symbol, Color.parseColor("#EDD606"));
            } else if (multiplier == 2.0) {
                view.setCellsToSymbol(new int[]{1, 3, 4, 5, 7}, symbol, Color.parseColor("#EDD606"));
            } else if (multiplier == 1.0) {
                view.setCellsToSymbol(new int[]{3, 4, 5}, symbol, Color.parseColor("#EDD606"));
            }

        } else if (gameName.equals(games[1])) {//Fruit Fiesta
            symbol = symbols[random.nextInt(symbols.length)];
            int color = Color.WHITE;

            if (multiplier == 10.0) {
                view.setCellsToSymbol(new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8}, symbol, color);
            } else if (multiplier == 2.5) {
                view.setCellsToSymbol(new int[]{0, 2, 4, 6, 8}, symbol, color);
            } else if (multiplier == 2.0) {
                view.setCellsToSymbol(new int[]{1, 3, 4, 5, 7}, symbol, color);
            } else if (multiplier == 1.3) {
                view.setCellsToSymbol(new int[]{1, 4, 7}, symbol, color);
            } else if (multiplier == 1.1) {
                view.setCellsToSymbol(new int[]{0, 1, 2}, symbol, color);
            } else if (multiplier == 1.0) {
                view.setCellsToSymbol(new int[]{3, 4, 5}, symbol, color);
            } else if (multiplier == 0.5) {
                view.setCellsToSymbol(new int[]{3, 5}, symbol, color);
            } else if (multiplier == 0.1) {
                view.setCellsToSymbol(new int[]{4}, symbol, color);
            }
        } else {
            symbol = "7️";
            int color = Color.parseColor("#E70614"); // Κόκκινο

            if (multiplier >= 10.0) {
                // JACKPOT (10.0): Γεμίζει όλη η οθόνη!
                view.setCellsToSymbol(new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8}, symbol, color);

            } else if (multiplier == 2.5) {
                // Κέρδος 2.5: Σχήμα "X" (Διαγώνιες)
                view.setCellsToSymbol(new int[]{0, 2, 4, 6, 8}, symbol, color);

            } else if (multiplier == 2.0) {
                // Κέρδος 2.0: Σχήμα Σταυρού "+"
                view.setCellsToSymbol(new int[]{1, 3, 4, 5, 7}, symbol, color);

            } else if (multiplier == 1.3) {
                // Κέρδος 1.3: Μεσαία Κάθετη Γραμμή
                view.setCellsToSymbol(new int[]{1, 4, 7}, symbol, color);

            } else if (multiplier == 1.1) {
                // Κέρδος 1.1: Πάνω Οριζόντια Γραμμή
                view.setCellsToSymbol(new int[]{0, 1, 2}, symbol, color);

            } else if (multiplier == 1.0) {
                // Κέρδος 1.0: Μεσαία Οριζόντια Γραμμή (Κλασική γραμμή καζίνο)
                view.setCellsToSymbol(new int[]{3, 4, 5}, symbol, color);

            } else if (multiplier == 0.5) {
                // Κέρδος 0.5 (Μικρό): Δύο πλαϊνά κουτάκια
                view.setCellsToSymbol(new int[]{3, 5}, symbol, color);

            } else if (multiplier == 0.1) {
                // Κέρδος 0.1 (Ελάχιστο): Μόνο το κέντρο της οθόνης
                view.setCellsToSymbol(new int[]{4}, symbol, color);
            }
        }*/
    }
   public void initializedata(String currentGameName) {
            GameRules rules=GameRules.getFruitGraphics(currentGameName);
            if(rules!=null){
                view.setImageBackground(rules.getLogoResId());
                symbols=rules.getSymbols();
                view.setCellsToSymbol(new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8},rules.getDefaultSymbol(),rules.getColor());
            }

           /*if (currentGameName.equals(games[0])) {
               view.setImageBackground(R.drawable.gates_of_olympus_logo);
               symbols = new String[]{"⚡", " "};
               view.setCellsToSymbol(new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8}, "⚡", Color.parseColor("#EDD606"));
           } else if (currentGameName.equals(games[1])) {
               view.setImageBackground(R.drawable.fruit_fiesta_logo);
               symbols = new String[]{"🍒", "🍋", "🍉", "🍇", "🍓"};
               view.setCellsToSymbol(new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8}, "🍋", Color.parseColor("#FFFFFF"));
           } else {
               view.setImageBackground(R.drawable.lucky_seven_logo);
               symbols = new String[]{"7", " "};
               view.setCellsToSymbol(new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8}, "7", Color.parseColor("#E70614"));
           }
       } */

    }
    public void spinClicked(String betamount) {
        if (betamount == null || betamount.trim().isEmpty()) {
            view.showMessage("Παρακαλώ εισάγετε ποσό πονταρίσματος!");
            return;
            // edo tha kalo showpattern kai randomizegrid
        }

    }
   /* public void initBalance() {
        //BigDecimal currentBalance = PlayerApp.getInstance().getBalance();
        if (currentBalance != null) {
            view.showBalance(currentBalance);
        }
    }*/
}
