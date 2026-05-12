package gr.goldenbet.view.games.fruitgame;
import com.google.gson.Gson;
import java.math.BigDecimal;
import gr.goldenbet.domain.Bet;
import gr.goldenbet.domain.FruitGameFactory;
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
    private FruitGameFactory factory;
    private  FruitGameRules rules;


    public FruitGamePresenter(FruitGameFragment view, String currentGameName, String ip, int port, Player player) {
        this.view = view;
        this.gameName = currentGameName;
        this.MASTER_IP=ip;
        this.MASTER_PORT=port;
        this.player=player;
        this.factory=new FruitGameFactory();
    }

    private void showPattern(double multiplier) {
        rules = factory.getFruitGraphics(gameName);
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

    public void initializedata(String currentGameName) {
        rules = factory.getFruitGraphics(currentGameName);
        if (rules != null) {
            view.setImageBackground(rules.getLogoResId());
            symbols = rules.getSymbols();
            view.showFinalGrid(null, null, symbols, rules.getColor());
            view.showBalance(player.getBalance());
        }
        view.showErrorDialog("Όρια Πονταρίσματος","πονταρισμα απο εδω εωσ εκει");
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
        //tha balo alterdialog gia ta oria toy pontarismatos!!!!!!!!!!!!!!
        player.setBalance(player.getBalance().subtract(bet));
        view.showBalance(player.getBalance());
        view.showSpinResult("Γυρίζουν...🎰...\nΑναμόνη για το αποτέλεσμα!!");

        view.lock(true);
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
                    view.showErrorDialog("Όρια Πονταρίσματος",serverMessage);
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
