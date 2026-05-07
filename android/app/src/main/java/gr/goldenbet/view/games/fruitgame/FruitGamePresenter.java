package gr.goldenbet.view.games.fruitgame;

import android.graphics.Color;

import com.google.gson.Gson;

import java.math.BigDecimal;


import gr.goldenbet.domain.Bet;
import gr.goldenbet.domain.FruitGameRules;
import gr.goldenbet.domain.JsonClient;
import gr.goldenbet.domain.MasterMessage;
import gr.goldenbet.domain.MasterMessageType;
import gr.goldenbet.domain.Player;


public class FruitGamePresenter {
    private FruitGameView view;
    private String gameName;
    private BigDecimal payout, bet;
    private  Player player;
    private String[] symbols;
    private double multiplier;

    Gson gson = new Gson();
    private static String MASTER_IP;
    private static int MASTER_PORT;


    public FruitGamePresenter(FruitGameFragment view, String currentGameName, String ip, int port, Player player) {
        this.view = view;
        this.gameName = currentGameName;
        this.MASTER_IP=ip;
        this.MASTER_PORT=port;
        this.player=player;
    }

    private void showPattern(double multiplier) {
        FruitGameRules rules = FruitGameRules.getFruitGraphics(gameName);
        if (multiplier > 0.0) {
            view.clearFruitGrid();
            if (multiplier == 10.0 && gameName.equals("Fruit Fiesta")) {
                view.setCellsToSymbol(new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8}, new String[]{"🍋"}, Color.parseColor("#FFFFFF"));
            } else if (rules.getWinPatterns().containsKey(multiplier)) {
                view.setCellsToSymbol(rules.getWinPatterns().get(multiplier), rules.getSymbols(), rules.getColor());
            }
        }
    }

    public void initializedata(String currentGameName) {
        FruitGameRules rules = FruitGameRules.getFruitGraphics(currentGameName);
        if (rules != null) {
            view.setImageBackground(rules.getLogoResId());
            symbols = rules.getSymbols();
            view.setCellsToSymbol(new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8}, rules.getSymbols(), rules.getColor());
            view.showBalance(player.getBalance());
        }


    }

    public void spinClicked(String betamount) {

        if (betamount == null || betamount.trim().isEmpty()) {
            view.showMessage("Παρακαλώ εισάγετε ποσό πονταρίσματος!");
            return;
        }
        bet = new BigDecimal(betamount);
        if (bet.compareTo(BigDecimal.ZERO) <= 0) {
            view.showErrorDialog("Λάθος Ποντάρισμα", "Το ποντάρισμα δεν μπορεί να είναι μηδέν ή αρνητικό ποσό. Δοκίμασε ξανά!");
            return;
        }

        if (bet.compareTo(player.getBalance()) > 0) {
            view.showErrorDialog("Ανεπαρκές Υπόλοιπο", "Δεν έχεις αρκετά FUNs για αυτό το ποντάρισμα. Το τρέχον υπόλοιπό σου είναι " + player.getBalance() + " FUN.");
            return;
        }
        player.setBalance(player.getBalance().subtract(bet));
        view.showBalance(player.getBalance());
        view.showSpinResult("Γυρίζουν...🎰...\nΑναμόνη για το αποτέλεσμα!!");

        view.lock(true);
        FruitGameRules rules = FruitGameRules.getFruitGraphics(gameName);
        symbols = rules.getSymbols();


        new Thread(() -> {
            try {
                Bet myBet = new Bet(bet, player.getUsername(),player.getPlayerId(), gameName);

                MasterMessage requestMessage = new MasterMessage(MasterMessageType.PLAY, myBet);
                String jsonMessage = gson.toJson(requestMessage);
                JsonClient betClient = new JsonClient(MASTER_IP, MASTER_PORT, jsonMessage);

                betClient.start();
                betClient.join();
                String response = betClient.getJsonResponse();

                if (response == null) {
                    throw new RuntimeException("No response received from the Master.");
                }

                try {
                    payout = gson.fromJson(response, BigDecimal.class);
                } catch (Exception e) {
                    //edo tha balo ton periorismo gia to bet . tha pernao to game sta fragment
                    player.addBalance(bet);
                    String serverMessage = response.replace("\"", "");
                    view.showSpinResult(serverMessage);
                    view.showBalance(player.getBalance());
                    view.lock(false);
                    return;
                }

                multiplier = 0.0;
                if (payout.compareTo(BigDecimal.ZERO) > 0) {
                    multiplier = payout.doubleValue() / bet.doubleValue();
                }
                for (int i = 0; i < 40; i++) {
                    view.randomizeGrid(symbols);
                    Thread.sleep(100);
                }

                showPattern(multiplier);
                if (payout.compareTo(BigDecimal.ZERO) > 0) {
                    player.addBalance(payout);
                    view.showSpinResult("ΚΕΡΔΙΣΕΣ " + payout + " FUN! 🎉\nΠολλαπλασιαστής: x" + multiplier);
                    view.playSound(true);
                } else {
                    view.showSpinResult("Δεν κέρδισες. Δοκίμασε ξανά!");
                    view.playSound(false);
                }
                view.showBalance(player.getBalance());

            } catch (Exception e) {
                view.showMessage(e.getMessage());
                view.showSpinResult("Προέκυψε σφάλμα.");
                player.addBalance(bet);
                view.showBalance(player.getBalance());
                view.lock(false);
            }

        }).start();

    }

}
