package gr.goldenbet.view.games.poker;

import java.math.BigDecimal;

import gr.goldenbet.domain.PokerGameRules;

public class PokerPresenter {
    private PokerView view;
    private String gameName,winningHandName;
    private BigDecimal payout, bet, playerBalance;
    private double multiplier;

    public PokerPresenter(PokerView view, String gameName) {
        this.view = view;
        this.gameName = gameName;
    }

    private void showcards(double multiplier) {
        PokerGameRules deck = new PokerGameRules();
        view.showDealtCards(deck.winpattern(multiplier));
        winningHandName=deck.getHandName();
    }

    public void onDealClicked(String betamount) {
        if (betamount == null || betamount.trim().isEmpty()) {
            view.showMessage("Παρακαλώ εισάγετε ποσό πονταρίσματος!");
            return;
        }
        view.showWinResult("Μοιράζει... 🃟");
        bet = new BigDecimal(betamount);
        view.showFlipResult("");

        new Thread(() -> {
            try {
               // payout = PlayerApp.getInstance().play(gameName, bet);
                view.showMessage(payout.toString());
                multiplier = 0.0;
                if (payout.compareTo(BigDecimal.ZERO) > 0) {
                    multiplier = payout.doubleValue() / bet.doubleValue();
                }
                showcards(multiplier);
            } catch (Exception e) {
                view.showWinResult(e.getMessage());
            }

        }).start();
    }

    public void onAnimationFinished() {
        if (payout.compareTo(BigDecimal.ZERO) > 0) {
            view.showWinResult("ΚΕΡΔΙΣΕΣ " + payout + " FUN! 🎉");
        } else {
            view.showWinResult("Δεν κέρδισες. Δοκίμασε ξανά!");
        }
        view.showBalance(playerBalance);
        view.showFlipResult(winningHandName);
    }

}
