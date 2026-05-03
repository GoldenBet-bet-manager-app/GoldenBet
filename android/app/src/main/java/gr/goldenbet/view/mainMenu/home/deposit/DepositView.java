package gr.goldenbet.view.mainMenu.home.deposit;

import java.math.BigDecimal;

public interface DepositView {
    void showSuccessDialog(String amount);
    void showError(String message);
    void setAmountToField(String amount);
    void navigateBack();
    void updateActivityBalance(BigDecimal newBalance);
    String getPlayerUsername();
}