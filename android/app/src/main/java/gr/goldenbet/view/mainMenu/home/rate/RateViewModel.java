package gr.goldenbet.view.mainMenu.home.rate;

import androidx.lifecycle.ViewModel;

import java.util.ArrayList;

public class RateViewModel extends ViewModel {
    private ArrayList<String> GameNames;

    public void setGameNames(ArrayList<String> names) { this.GameNames = names; }
    public ArrayList<String> getGameNames() { return GameNames; }
}
