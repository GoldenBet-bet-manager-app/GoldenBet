package gr.goldenbet.view.games.poker;

import com.google.gson.Gson;

import java.math.BigDecimal;

import gr.goldenbet.domain.Bet;
import gr.goldenbet.domain.Game;
import gr.goldenbet.domain.JsonClient;
import gr.goldenbet.domain.MasterMessage;
import gr.goldenbet.domain.MasterMessageType;
import gr.goldenbet.domain.Player;
import gr.goldenbet.domain.PokerGameRules;

public class PokerPresenter {
    private PokerView view;
    private String winningHandName;
    private Game game;
    private BigDecimal payout, bet;
    private double multiplier;
    Gson gson = new Gson();
    private static String MASTER_IP;
    private static int MASTER_PORT;
    private Player player;

    public PokerPresenter(PokerView view, Game game, String ip, int port, Player player) {
        this.view = view;
        this.game = game;
        this.MASTER_IP = ip;
        this.MASTER_PORT = port;
        this.player = player;
    }


    private void showcards(double multiplier) {
        PokerGameRules deck = new PokerGameRules();
        view.showDealtCards(deck.winpattern(multiplier));
        winningHandName = deck.getHandName();
    }

    public void onDealClicked(String betamount) {
        if (betamount == null || betamount.trim().isEmpty()) {
            view.showMessage("Παρακαλώ εισάγετε ποσό πονταρίσματος!");
            return;
        }
        bet = new BigDecimal(betamount);
        if (bet.compareTo(BigDecimal.ZERO) <= 0) {
            view.showMessage("The bet cannot be negative or zero.");
            return;
        }

        if (bet.compareTo(player.getBalance()) > 0) {
            view.showMessage("You don't have enough FUNs.");
            return;
        }

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
        view.showWinResult("Μοιράζει... 🃟");
        view.showFlipResult("");
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
                    view.showErrorDialog("Όρια Πονταρίσματος", serverMessage);
                    view.showBalance(player.getBalance());
                    view.lock(false);
                    return;
                }

                multiplier = 0.0;
                if (payout.compareTo(BigDecimal.ZERO) > 0) {
                    multiplier = payout.doubleValue() / bet.doubleValue();
                }
                showcards(multiplier);

            } catch (Exception e) {
                view.showMessage(e.getMessage());
                player.addBalance(bet);
                view.showBalance(player.getBalance());
                view.lock(false);
            }

        }).start();

    }


    public void onAnimationFinished() {
        if (payout.compareTo(BigDecimal.ZERO) > 0) {
            view.showWinResult("ΚΕΡΔΙΣΕΣ " + payout + " FUN! \nΠολλαπλασιαστής: x" + multiplier);
            view.playSound(true);
            player.addBalance(payout);
        } else {
            view.showWinResult("Δεν κέρδισες. Δοκίμασε ξανά!");
            view.playSound(false);
        }
        view.showBalance(player.getBalance());
        view.showFlipResult(winningHandName);
    }


    public void initializedata() {
        view.showBalance(player.getBalance());
        view.showErrorDialog("Όρια Πονταρίσματος", "Ποντάρισμα από " + game.getMinBet() + " έως " + game.getMaxBet() + " FUN.");
    }
}









