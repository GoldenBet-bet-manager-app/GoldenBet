package gr.goldenbet.view.Games.FruitGame;

import java.math.BigDecimal;

import gr.goldenbet.domain.PlayerApp;

public class FruitGamePresenter {
    private FruitGameView view;
    private String gameName,playerId;

    public FruitGamePresenter(FruitGameView view, String playerId, String gameName) {
        this.view = view;
        this.playerId = playerId;
        this.gameName = gameName;
    }


    public void spinClicked(String betamount) {
        if (betamount == null || betamount.trim().isEmpty()) {
            view.showMessage("Παρακαλώ εισάγετε ποσό πονταρίσματος!");
            return;
        }

    }// na milhso me giannh gia tin apothikeysi paikton
   /* public void initBalance() {
        //BigDecimal currentBalance = PlayerApp.getInstance().getBalance();
        if (currentBalance != null) {
            view.showBalance(currentBalance);
        }
    }*/
}
