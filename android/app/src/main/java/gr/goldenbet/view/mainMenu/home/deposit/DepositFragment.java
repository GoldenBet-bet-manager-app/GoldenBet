package gr.goldenbet.view.mainMenu.home.deposit;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import gr.goldenbet.R;
import gr.goldenbet.databinding.FragmentDepositBinding;

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
        presenter = new DepositPresenter(this);

        // Back Button
        binding.depositBtnBack.setOnClickListener(v -> navigateBack());

        // Quick Amounts
        binding.cardAmount20.setOnClickListener(v -> presenter.onQuickAmountSelected("20"));
        binding.cardAmount50.setOnClickListener(v -> presenter.onQuickAmountSelected("50"));

        // Payment Methods
        binding.btnMethodCard.setOnClickListener(v -> presenter.onMethodSelected("CARD"));
        binding.btnMethodPaypal.setOnClickListener(v -> presenter.onMethodSelected("PAYPAL"));

        // Confirm Button
        binding.btnConfirmDeposit.setOnClickListener(v -> {
            String amount = binding.etDepositAmount.getText().toString();
            presenter.onDepositClicked(amount);
        });
    }

    @Override
    public void showSuccessDialog(String amount) {
        // Χρήση του format string από τα strings.xml
        String message = getString(R.string.deposit_success_msg, amount);

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.deposit_success_title)
                .setMessage(message)
                .setIcon(R.drawable.ic_check_circle) // Βεβαιώσου ότι έχεις αυτό το εικονίδιο
                .setPositiveButton(R.string.common_ok, (dialog, which) -> {
                    dialog.dismiss();
                    navigateBack(); // Επιστροφή στο Home μετά την επιτυχία
                })
                .show();
    }

    @Override
    public void showError(int resId) {
        Toast.makeText(requireContext(), resId, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void setAmountToField(String amount) {
        binding.etDepositAmount.setText(amount);
    }

    @Override
    public void navigateBack() {
        if (getParentFragmentManager().getBackStackEntryCount() > 0) {
            getParentFragmentManager().popBackStack();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}