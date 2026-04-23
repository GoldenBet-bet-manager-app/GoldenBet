package gr.goldenbet.view.games.poker;

import gr.goldenbet.domain.Deck;

public class PokerPresenter {
    private PokerView view;
    private String gameName,playerId;

    public PokerPresenter(PokerView view, String playerId, String gameName) {
        this.view = view;
        this.playerId = playerId;
        this.gameName = gameName;
    }

    private void showcards(double multiplier){
        Deck deck=new Deck();
        view.showDealtCards(deck.winpattern(multiplier));
    }

    public void onDealClicked(String betamount) {
        if (betamount == null || betamount.trim().isEmpty()) {
            view.showMessage("Παρακαλώ εισάγετε ποσό πονταρίσματος!");
            return;
        }
    }

    public void onAnimationFinished() {

    }
}
