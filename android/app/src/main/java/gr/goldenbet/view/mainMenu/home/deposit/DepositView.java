package gr.goldenbet.view.mainMenu.home.deposit;

public interface DepositView {
    void showSuccessDialog(String amount);
    void showError(int resId);
    void setAmountToField(String amount);
    void navigateBack();
}