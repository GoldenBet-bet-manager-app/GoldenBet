package gr.goldenbet.view.games.wheelgame;

import java.math.BigDecimal;

import gr.goldenbet.domain.PlayerApp;
import gr.goldenbet.domain.WheelGameRules;

public class WheelGamePresenter {
    private WheelGameView view;
    private String gameName;
    private BigDecimal payout, bet, playerBalance;
    private double multiplier;
    private final String[] games = {"Pharaoh's Dynasty", "Dragon's Lair", "Pirate's Bounty"};

    public WheelGamePresenter(WheelGameView view, String gameName) {
        this.view = view;
        this.gameName = gameName;
    }

    public void initializedata(String currentGameName) {
        WheelGameRules rules = WheelGameRules.getWheelGraphics(currentGameName);
        if (rules != null) view.setImageBackground(rules.getLogoResId());
    }

    public void spinClicked(String betamount) {
        if (betamount == null || betamount.trim().isEmpty()) {
            view.showMessage("Παρακαλώ εισάγετε ποσό πονταρίσματος!");
            return;
        }
        view.showSpinResult("Γυρίζει ο τροχός...");
        bet = new BigDecimal(betamount);
        playerBalance = PlayerApp.getInstance().getBalance();
        new Thread(() -> {
            try {
                payout = PlayerApp.getInstance().play(gameName, bet);
                view.showMessage(payout.toString());
                multiplier = 0.0;
                if (payout.compareTo(BigDecimal.ZERO) > 0) {
                    multiplier = payout.doubleValue() / bet.doubleValue();
                }
                WheelGameRules rules = WheelGameRules.getWheelGraphics(gameName);
                int sliceIndex = rules.determineSliceIndex(multiplier);
                view.spinWheel(sliceIndex);
            } catch (Exception e) {
               view.showSpinResult(e.getMessage());
            }

        }).start();
    }
   public void onAnimationEnd(){
       if (payout.compareTo(BigDecimal.ZERO) > 0) {
           view.showSpinResult("ΚΕΡΔΙΣΕΣ " + payout + " FUN! 🎉");
       } else {
           view.showSpinResult("Δεν κέρδισες. Δοκίμασε ξανά!");
       }
       view.showBalance(playerBalance);
    }
}
