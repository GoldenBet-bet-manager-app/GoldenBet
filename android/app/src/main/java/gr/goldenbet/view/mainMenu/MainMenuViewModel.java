package gr.goldenbet.view.mainMenu;

import androidx.lifecycle.ViewModel;
import gr.goldenbet.domain.Player;

public class MainMenuViewModel extends ViewModel {

    private Player currentPlayer;

    public void setCurrentPlayer(Player player) {
        this.currentPlayer = player;
    }

    public Player getCurrentPlayer() {
        return currentPlayer;
    }
}