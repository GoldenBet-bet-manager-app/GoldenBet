package gr.goldenbet.view.mainMenu;

import gr.goldenbet.R;

public class MainMenuPresenter {
    private MainMenuView view;

    public MainMenuPresenter(MainMenuView view) {
        this.view = view;
    }

    public void onNavigationItemSelected(int itemId) {
        if (itemId == R.id.main_menu_home) {
            view.showHome();
        } else if (itemId == R.id.main_menu_search) {
            view.showSearch();
        } else if (itemId == R.id.main_menu_profile) {
            view.showProfile();
        }
    }
}