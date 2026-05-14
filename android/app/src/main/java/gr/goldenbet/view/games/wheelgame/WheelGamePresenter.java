package gr.goldenbet.view.games.wheelgame;

import com.google.gson.Gson;

import java.math.BigDecimal;


import gr.goldenbet.domain.Bet;
import gr.goldenbet.domain.Game;
import gr.goldenbet.domain.JsonClient;
import gr.goldenbet.domain.MasterMessage;
import gr.goldenbet.domain.MasterMessageType;
import gr.goldenbet.domain.Player;
import gr.goldenbet.domain.WheelGameRules;
import gr.goldenbet.utils.ConfigLoader;

public class WheelGamePresenter {
    private WheelGameView view;

    private BigDecimal payout, bet;
    private double multiplier;
    Gson gson = new Gson();
    private Game game;

    private Player player;
    private final String MASTER_IP;
    private final int MASTER_PORT;
    private  WheelGameRules rules;

    public WheelGamePresenter(WheelGameView view, Game game, Player player) {
        this.view = view;
        this.game = game;
        this.player=player;
        MASTER_IP = ConfigLoader.getIp("MASTER");
        MASTER_PORT = ConfigLoader.getPort("MASTER");
    }

    public void initializedata( ) {
        view.setImageBackground(game.getBase64Image());
        rules = WheelGameRules.getWheelGraphics(game);
        if (rules != null) view.setWheelImage(rules.getLogoResId());
        view.showBalance(player.getBalance());
        view.showErrorDialog("Όρια Πονταρίσματος", "Ποντάρισμα από " + game.getMinBet() + " έως " + game.getMaxBet() + " FUN.");
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
        if (bet.compareTo(game.getMinBet()) < 0 || bet.compareTo(game.getMaxBet()) > 0) {
            view.showErrorDialog("Λάθος Ποντάρισμα", "Το ποντάρισμα πρέπει να είναι από " + game.getMinBet() + " έως " + game.getMaxBet() + " FUN.");
            return;
        }

        if (bet.compareTo(player.getBalance()) > 0) {
            view.showErrorDialog("Ανεπαρκές Υπόλοιπο", "Δεν έχεις αρκετά FUNs για αυτό το ποντάρισμα. Το τρέχον υπόλοιπό σου είναι " + player.getBalance() + " FUN.");
            return;
        }

        player.setBalance(player.getBalance().subtract(bet));
        view.showBalance(player.getBalance());
        view.showSpinResult("Γυρίζει ο τροχός...");
        view.lock(true);


        new Thread(() -> {
            try {
                Bet myBet = new Bet(bet, player.getUsername(), player.getPlayerId(), game.getGameName());

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
                    player.addBalance(bet);
                    String serverMessage = response.replace("\"", "");
                    view.showErrorDialog("ΠΡΟΕΙΟΔΟΠΙΣΗ", serverMessage);
                    view.showBalance(player.getBalance());
                    return;
                }

                multiplier = 0.0;
                if (payout.compareTo(BigDecimal.ZERO) > 0) {
                    multiplier = payout.doubleValue() / bet.doubleValue();
                }
                int targetSliceIndex = rules.determineSliceIndex(multiplier);
                view.spinWheel(targetSliceIndex);
            } catch (Exception e) {
                view.showMessage(e.getMessage());
                player.addBalance(bet);
                view.showBalance(player.getBalance());
            }

        }).start();
    }
   public void onAnimationEnd(){
       if (payout.compareTo(BigDecimal.ZERO) > 0) {
           player.addBalance(payout);
           view.showSpinResult("ΚΕΡΔΙΣΕΣ " + payout + " FUN! \nΠολλαπλασιαστής: x" + multiplier);
           view.playSound(true);
       } else {
           view.showSpinResult("Δεν κέρδισες. Δοκίμασε ξανά!");
           view.playSound(false);
       }
       view.showBalance(player.getBalance());
       view.lock(false);
    }
}
