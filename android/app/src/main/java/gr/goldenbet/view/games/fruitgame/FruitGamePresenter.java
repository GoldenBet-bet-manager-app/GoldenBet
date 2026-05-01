package gr.goldenbet.view.games.fruitgame;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;

import java.math.BigDecimal;
import gr.goldenbet.domain.Bet;
import gr.goldenbet.domain.FruitGameRules;
import gr.goldenbet.domain.JsonClient;
import gr.goldenbet.domain.MasterMessage;
import gr.goldenbet.domain.MasterMessageType;

public class FruitGamePresenter {
    private FruitGameView view;
    private String gameName,playerId;
    private BigDecimal payout,bet,playerBalance;
    private String[] symbols;
    private double multiplier;
    private static final String MASTER_IP = "10.0.2.2";
    private static final int MASTER_PORT = 43210;
    private SharedPreferences prefs;
    Gson gson=new Gson();

    public FruitGamePresenter(FruitGameView view, String gameName, String playerId, SharedPreferences prefs) {
        this.view = view;
        this.gameName = gameName;
        this.playerId = playerId;
        this.prefs = prefs;
    }

    private void showPattern(double multiplier) {
        view.clearFruitGrid();
        FruitGameRules rules= FruitGameRules.getFruitGraphics(gameName);
        if(rules.getWinPatterns().containsKey(multiplier)){
            view.setCellsToSymbol(rules.getWinPatterns().get(multiplier),rules.getDefaultSymbol(),rules.getColor());
        }

    }
   public void initializedata(String currentGameName) {
            FruitGameRules rules= FruitGameRules.getFruitGraphics(currentGameName);
            if(rules!=null){
                view.setImageBackground(rules.getLogoResId());
                symbols=rules.getSymbols();
                view.setCellsToSymbol(new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8},rules.getDefaultSymbol(),rules.getColor());
            }



    }
    public void spinClicked(String betamount) {

        if (betamount == null || betamount.trim().isEmpty()) {
            view.showMessage("Παρακαλώ εισάγετε ποσό πονταρίσματος!");
            return;
        }
        bet=new BigDecimal(betamount);
        if(bet.compareTo ( BigDecimal.ZERO ) <= 0){
            view.showMessage("The bet cannot be negative or zero.");
            return;
        }
//        playerBalance=PlayerApp.getInstance().getBalance();
//        if ( amount.compareTo ( this.balance) > 0 )
//            throw new RuntimeException ( "You don't have enough FUNs." );

        view.showSpinResult("Γυρίζουν...🎰...\nΑναμόνη για το αποτέλεσμα!!");

        view.lock(true);
        FruitGameRules rules= FruitGameRules.getFruitGraphics(gameName);
        symbols=rules.getSymbols();


        new Thread(() -> {
            try {
                Bet myBet = new Bet(bet, playerId, gameName);

                MasterMessage requestMessage = new MasterMessage(MasterMessageType.PLAY, myBet);
                String jsonMessage=gson.toJson(requestMessage);
                JsonClient betClient = new JsonClient(MASTER_IP, MASTER_PORT, jsonMessage);

                betClient.start();
                betClient.join();
                String response = betClient.getJsonResponse();
                if (response == null) {
                    throw new RuntimeException("No response received from the Master.");
                }
                if ( response instanceof String ) {
                    view.showMessage(response);
                    return;
                }
                try {
                    payout = gson.fromJson(response, BigDecimal.class);
                    prefs.edit().putBoolean("PLAYED_" + gameName, true).apply();
                    //  this.balance = this.balance.subtract ( amount );
//                    if ( payout.compareTo ( BigDecimal.ZERO ) > 0 ) {
//                        addBalance(payout);
//                    }
                } catch (Exception e) {
                    view.showMessage(e.getMessage());
                }

                view.showMessage(payout.toString());
                 multiplier=0.0;
                if (payout.compareTo(BigDecimal.ZERO) > 0) {
                    multiplier = payout.doubleValue() / bet.doubleValue();
                }
                for(int i=0;i<40;i++) {
                    view.randomizeGrid(symbols);
                    Thread.sleep(100);
                }

                showPattern(multiplier);
                if (payout.compareTo(BigDecimal.ZERO) > 0) {
                    view.showSpinResult("ΚΕΡΔΙΣΕΣ " + payout + " FUN! 🎉");
                } else {
                    view.showSpinResult("Δεν κέρδισες. Δοκίμασε ξανά!");
                }
                //view.showBalance(PlayerApp.getInstance().getBalance());

            } catch (Exception e) {
                view.showMessage(e.getMessage());
                view.showSpinResult("Προέκυψε σφάλμα.");
            }

        }).start();

    }
    public void initBalance() {

    }
}
