package gr.goldenbet.view.games.fruitgame;

import java.math.BigDecimal;

import gr.goldenbet.domain.FruitGameRules;
import gr.goldenbet.domain.PlayerApp;

public class FruitGamePresenter {
    private FruitGameView view;
    private String gameName;
    private BigDecimal payout,bet,playerBalance;
    private String[] symbols;
    private double multiplier;



    public FruitGamePresenter(FruitGameView view, String gameName) {
        this.view = view;
        this.gameName = gameName;
    }

    private void showPattern(double multiplier) {
        view.clearFruitGrid();
        FruitGameRules rules= FruitGameRules.getFruitGraphics(gameName);
        if(rules.getWinPatterns().containsKey(multiplier)){
            view.setCellsToSymbol(rules.getWinPatterns().get(multiplier),rules.getDefaultSymbol(),rules.getColor());
        }

    }
   public void initializedata(String currentGameName) {
            FruitGameRules rules= FruitGameRules.getFruitGraphics(currentGameName);
            if(rules!=null){
                view.setImageBackground(rules.getLogoResId());
                symbols=rules.getSymbols();
                view.setCellsToSymbol(new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8},rules.getDefaultSymbol(),rules.getColor());
            }



    }
    public void spinClicked(String betamount) {

        if (betamount == null || betamount.trim().isEmpty()) {
            view.showMessage("Παρακαλώ εισάγετε ποσό πονταρίσματος!");
            return;
        }
        view.showSpinResult("Γυρίζουν...🎰...\nΑναμόνη για το αποτέλεσμα!!");

        view.lock(true);
        FruitGameRules rules= FruitGameRules.getFruitGraphics(gameName);
        symbols=rules.getSymbols();
        bet=new BigDecimal(betamount);
        playerBalance=PlayerApp.getInstance().getBalance();
        new Thread(() -> {
            try {
                payout= PlayerApp.getInstance().play(gameName,bet);
                view.showMessage(payout.toString());
                 multiplier=0.0;
                if (payout.compareTo(BigDecimal.ZERO) > 0) {
                    multiplier = payout.doubleValue() / bet.doubleValue();
                }
                for(int i=0;i<40;i++) {
                    view.randomizeGrid(symbols);
                    Thread.sleep(100);
                }
                showPattern(multiplier);
                if (payout.compareTo(BigDecimal.ZERO) > 0) {
                    view.showSpinResult("ΚΕΡΔΙΣΕΣ " + payout + " FUN! 🎉");
                } else {
                    view.showSpinResult("Δεν κέρδισες. Δοκίμασε ξανά!");
                }
                view.showBalance(PlayerApp.getInstance().getBalance());
            } catch (IllegalArgumentException e) {
                view.showMessage(e.getMessage());
            }catch (RuntimeException e){
                view.showSpinResult(e.getMessage());
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

        }).start();

    }
   /* public void initBalance() {
        //BigDecimal currentBalance = PlayerApp.getInstance().getBalance();
        if (currentBalance != null) {
            view.showBalance(currentBalance);
        }
    }*/
}
