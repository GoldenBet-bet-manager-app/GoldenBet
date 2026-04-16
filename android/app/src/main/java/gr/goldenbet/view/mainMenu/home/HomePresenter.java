package gr.goldenbet.view.mainMenu.home;

public class HomePresenter {
    private HomeView view;

    public HomePresenter(HomeView view) {
        this.view = view;
    }

    public void initData() {
        view.setPlayerName("Player Name");
        view.setBalance("2.500,00 €");
    }

    public void onDepositClicked() { view.navigateToDeposit(); }
    public void onHistoryClicked() { view.navigateToHistory(); }
    public void onStatsClicked() { view.navigateToStats(); }
    public void onRatingsClicked() { view.navigateToRatings(); }
}