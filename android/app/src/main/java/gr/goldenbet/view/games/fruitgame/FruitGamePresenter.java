package gr.goldenbet.view.games.fruitgame;
import com.google.gson.Gson;
import java.math.BigDecimal;
import gr.goldenbet.domain.Bet;
import gr.goldenbet.domain.FruitGameFactory;
import gr.goldenbet.domain.FruitGameRules;
import gr.goldenbet.domain.Game;
import gr.goldenbet.domain.JsonClient;
import gr.goldenbet.domain.MasterMessage;
import gr.goldenbet.domain.MasterMessageType;
import gr.goldenbet.domain.Player;
import gr.goldenbet.utils.ConfigLoader;

public class FruitGamePresenter {

    private FruitGameView view;
    private BigDecimal payout, bet;
    private  Player player;
    private String[] symbols;
    private Game game;
    private double multiplier;
    Gson gson = new Gson();
    private final String MASTER_IP;
    private final int MASTER_PORT;
    private FruitGameFactory factory;
    private  FruitGameRules rules;




    public FruitGamePresenter(FruitGameFragment view, Game game, Player player){

        this.view = view;
        this.game = game;
        this.player = player;
        MASTER_IP = ConfigLoader.getIp("MASTER");
        MASTER_PORT = ConfigLoader.getPort("MASTER");
        this.factory = new FruitGameFactory();
    }

    private void showPattern(double multiplier){

        rules = factory.getFruitGraphics(game.getGameName());
        String[] allSymbols = rules.getSymbols();

        if (multiplier > 0.0) {
            int[] winIndices = rules.getWinPatterns().get(multiplier);

            if (winIndices != null) {
                view.showFinalGrid(winIndices, allSymbols[0], allSymbols, rules.getColor());
            } else {
                view.showFinalGrid(null, null, allSymbols, rules.getColor());
            }
        } else {
            view.showFinalGrid(null, null, allSymbols, rules.getColor());
        }
    }


    public void initializedata(){
        view.setImageBackground(game.getBase64Image());
        rules = factory.getFruitGraphics(game.getGameName());
        if (rules != null) {
            symbols = rules.getSymbols();
            view.showFinalGrid(null, null, symbols, rules.getColor());
            view.showBalance(player.getBalance());
        }
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
        view.showSpinResult("Γυρίζουν...🎰...\nΑναμόνη για το αποτέλεσμα!!");

        view.lock(true);
        symbols = rules.getSymbols();


        new Thread(() -> {
            try {
                Bet myBet = new Bet(bet, player.getUsername(),player.getPlayerId(), game.getGameName());

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
                finalResult();

            } catch (Exception e) {
                view.showMessage(e.getMessage());
                view.showSpinResult("Προέκυψε σφάλμα.");
                player.addBalance(bet);
                view.showBalance(player.getBalance());
                view.lock(false);
            }

        }).start();

    }

    private void finalResult() {
        if (payout.compareTo(BigDecimal.ZERO) > 0) {
            player.addBalance(payout);
            view.showSpinResult("ΚΕΡΔΙΣΕΣ " + payout + " FUN! \nΠολλαπλασιαστής: x" + multiplier);
            view.playSound(true);
        } else {
            view.showSpinResult("Δεν κέρδισες. Δοκίμασε ξανά!");
            view.playSound(false);
        }
        view.showBalance(player.getBalance());
    }

}
