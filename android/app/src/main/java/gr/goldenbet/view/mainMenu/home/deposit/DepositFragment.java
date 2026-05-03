package gr.goldenbet.view.mainMenu.home.deposit;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.math.BigDecimal;

import gr.goldenbet.R;
import gr.goldenbet.databinding.FragmentDepositBinding;
import gr.goldenbet.domain.Player;
import gr.goldenbet.view.mainMenu.MainMenuActivity;

public class DepositFragment extends Fragment implements DepositView {

    private FragmentDepositBinding binding;
    private DepositPresenter presenter;

    public DepositFragment() { }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentDepositBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Λήψη username από την Activity (ViewModel)
        String playerUsername = "";

        presenter = new DepositPresenter(this);

        binding.depositBtnBack.setOnClickListener(v -> navigateBack());
        binding.cardAmount20.setOnClickListener(v -> presenter.onQuickAmountSelected("20"));
        binding.cardAmount50.setOnClickListener(v -> presenter.onQuickAmountSelected("50"));
        binding.btnMethodCard.setOnClickListener(v -> presenter.onMethodSelected("CARD"));
        binding.btnMethodPaypal.setOnClickListener(v -> presenter.onMethodSelected("PAYPAL"));

        binding.btnConfirmDeposit.setOnClickListener(v -> {
            presenter.onDepositClicked(binding.etDepositAmount.getText().toString());
        });
    }


    @Override
    public void updateActivityBalance(BigDecimal newBalance) {
        FragmentActivity activity = getActivity();

        if (activity instanceof MainMenuActivity) {

            activity.runOnUiThread(() -> {

                if (isAdded()) {
                    MainMenuActivity main = (MainMenuActivity) activity;
                    Player p = main.getCurrentPlayer();
                    if (p != null) {
                        p.setBalance(newBalance);
                        main.updateLocalPlayer(p);
                    }
                }

            });
        }

    }

    @Override
    public void showSuccessDialog(String amount) {
        androidx.fragment.app.FragmentActivity activity = getActivity();

        if (activity != null) {
            activity.runOnUiThread(() -> {
                // Is the Activity alive? (activity != null)
                // Is Fragment connected? (isAdded())
                // Is there the View (binding != null)
                if (isAdded() && binding != null) {

                    String message = getString(R.string.deposit_success_msg, amount);

                    new MaterialAlertDialogBuilder(activity)
                            .setTitle(R.string.deposit_success_title)
                            .setMessage(message)
                            .setIcon(R.drawable.ic_check_circle)
                            .setCancelable(false)
                            .setPositiveButton(R.string.common_ok, (dialog, which) -> {
                                dialog.dismiss();
                                navigateBack(); // Return to previous Fragment
                            })
                            .show();
                }
            });
        }
    }

    @Override
    public void showError(String message) {
        androidx.fragment.app.FragmentActivity activity = getActivity();

        if (activity != null) {

            activity.runOnUiThread(() -> {
                if (isAdded() && binding != null) {
                    Toast.makeText(activity, message, Toast.LENGTH_LONG).show();
                }
            });
        }

    }

    @Override
    public void setAmountToField(String amount) {
        if (binding != null) {
            binding.etDepositAmount.setText(amount);
        }
    }

    @Override
    public void navigateBack() {
        if (getParentFragmentManager().getBackStackEntryCount() > 0) {
            getParentFragmentManager().popBackStack();
        }
    }


    @Override
    public String getPlayerUsername() {
        if (getActivity() instanceof MainMenuActivity) {
            return ((MainMenuActivity) getActivity()).getCurrentPlayer().getUsername();
        }

        return "";
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}