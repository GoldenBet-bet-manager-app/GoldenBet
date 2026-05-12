package gr.goldenbet.view.mainMenu.search;


import java.util.List;

import gr.goldenbet.domain.Game;

public interface SearchView {

    void showEmptyState();
    void hideEmptyState();
    void showLoading();
    void hideLoading();
    void showMessage(String string);
    void updateGameList(List<Game> gamesToShow);
}
