package gr.goldenbet.view.mainMenu.home.deposit;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.math.BigDecimal;

import gr.goldenbet.R;
import gr.goldenbet.domain.JsonClient;
import gr.goldenbet.domain.MasterMessage;
import gr.goldenbet.domain.MasterMessageType;
import gr.goldenbet.domain.Transaction;
import gr.goldenbet.view.mainMenu.home.HomeFragment;

public class DepositPresenter {
    private final DepositView view;
    private String selectedMethod = "";

    private final String MASTER_IP;
    private final int MASTER_PORT;

    public DepositPresenter(DepositView view) {
        this.view = view;

        MASTER_IP = ((DepositFragment)view).getResources().getString(R.string.master_ip);
        MASTER_PORT = ((DepositFragment)view).getResources().getInteger(R.integer.master_port);
    }

    public void onQuickAmountSelected(String amount) {
        view.setAmountToField(amount);
    }

    public void onMethodSelected(String method) {
        this.selectedMethod = method;
    }


    public void onDepositClicked(String amountStr) {

        // No amount entered
        if (amountStr.isEmpty()) {
            String errMsg = ((DepositFragment)view).getResources().getString(R.string.deposit_error_invalid_amount);
            view.showError(errMsg);
            return;
        }

        // Parsing amountStr into BigDecimal
        BigDecimal depositAmount;
        try {
            depositAmount = new BigDecimal(amountStr);
            if (depositAmount.compareTo(BigDecimal.ZERO) <= 0) {
                String errMsg = ((DepositFragment)view).getResources().getString(R.string.deposit_error_invalid_amount);
                view.showError(errMsg);
                return;
            }
        } catch (NumberFormatException e) {
            String errMsg = ((DepositFragment)view).getResources().getString(R.string.deposit_error_invalid_amount);
            view.showError(errMsg);
            return;
        }


        // No payment method selected
        if (selectedMethod == null || selectedMethod.isEmpty()) {
            String errMsg = ((DepositFragment)view).getResources().getString(R.string.deposit_error_no_method);
            view.showError(errMsg);
            return;
        }

        String playerUsername = view.getPlayerUsername();

        Transaction depositTx = new Transaction(
                playerUsername,
                "DEPOSIT",
                depositAmount,
                "Deposit via " + selectedMethod
        );

        Gson gson = new Gson();


        MasterMessage depositReq = new MasterMessage(MasterMessageType.DEPOSIT, depositTx);
        String jsonRequest = gson.toJson(depositReq);

        // Execution in background Thread
        new Thread(() -> {
            try {
                JsonClient client = new JsonClient(MASTER_IP, MASTER_PORT, jsonRequest);
                client.start();
                client.join(); // wait for client to join

                String response = client.getJsonResponse();
                if (response != null) {
                    JsonObject responseJson = gson.fromJson(response, JsonObject.class);
                    String status = responseJson.get("status").getAsString();

                    if ("SUCCESS".equals(status)) {

                        if (responseJson.has("new_balance")) {
                            String newBalance = responseJson.get("new_balance").getAsString();
                            view.updateActivityBalance(new BigDecimal(newBalance));
                        }

                        view.showSuccessDialog(depositAmount + " FUN");
                    } else {

                        String msg = responseJson.has("message") ? responseJson.get("message").getAsString() : "Transaction rejected";
                        view.showError(msg);
                    }
                } else {
                    // Αν ο Master δεν απαντήσει (null response)
                    view.showError("No response from server");
                }

            } catch (InterruptedException e) {
                e.printStackTrace();
                view.showError("Connection interrupted");
            }
        }).start();

    }

}