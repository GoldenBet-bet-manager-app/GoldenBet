package gr.goldenbet.view.mainMenu.home.deposit;

import gr.goldenbet.R;

public class DepositPresenter {
    private final DepositView view;
    private String selectedMethod = "";

    public DepositPresenter(DepositView view) {
        this.view = view;
    }

    public void onQuickAmountSelected(String amount) {
        view.setAmountToField(amount);
    }

    public void onMethodSelected(String method) {
        this.selectedMethod = method;
    }

    public void onDepositClicked(String amount) {
        if (amount.isEmpty() || Double.parseDouble(amount) < 10) {
            view.showError(R.string.deposit_error_invalid_amount);
            return;
        }

        if (selectedMethod.isEmpty()) {
            view.showError(R.string.deposit_error_no_method);
            return;
        }

        view.showSuccessDialog(amount + " €");
    }
}