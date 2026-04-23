package gr.goldenbet.view.games.wheelgame;

import gr.goldenbet.domain.GameRules;

public class WheelGamePresenter {
    private WheelGameView view;
    private String gameName,playerId;
    private final String[] games = {"Pharaoh's Dynasty", "Dragon's Lair", "Pirate's Bounty"};

    public WheelGamePresenter(WheelGameView view, String playerId, String gameName) {
        this.view = view;
        this.playerId = playerId;
        this.gameName = gameName;
    }
    public void initializedata(String currentGameName) {
        GameRules rules=GameRules.getWheelGraphics(currentGameName);
        if(rules!=null) view.setImageBackground(rules.getLogoResId());
    }
    public void spinClicked(String betamount) {
        if (betamount == null || betamount.trim().isEmpty()) {
            view.showMessage("Παρακαλώ εισάγετε ποσό πονταρίσματος!");
            return;
        }
    }
}
