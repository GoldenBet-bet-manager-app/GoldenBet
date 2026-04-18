package gr.goldenbet.view.Games.WheelGame;

public class WheelGamePresenter {
    private WheelGameView view;
    private String gameName,playerId;

    public WheelGamePresenter(WheelGameView view, String playerId, String gameName) {

    }

    public void spinClicked(String betamount) {
        if (betamount == null || betamount.trim().isEmpty()) {
            view.showMessage("Παρακαλώ εισάγετε ποσό πονταρίσματος!");
            return;
        }
    }
}
