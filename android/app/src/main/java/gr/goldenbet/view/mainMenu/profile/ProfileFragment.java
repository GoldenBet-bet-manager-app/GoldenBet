package gr.goldenbet.view.mainMenu.profile;

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
import gr.goldenbet.databinding.FragmentProfileBinding;

public class ProfileFragment extends Fragment implements ProfileView {

    private FragmentProfileBinding binding;
    private ProfilePresenter presenter;

    public ProfileFragment() { }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        presenter = new ProfilePresenter(this);

        // Φόρτωση δεδομένων
        presenter.loadProfile();

        // Listener για το κουμπί Save
        binding.btnProfileSave.setOnClickListener(v -> {
            // Μαζεύουμε όλα τα στοιχεία από τα EditText
            presenter.onSaveClicked(
                    binding.etProfileUsername.getText().toString(),
                    binding.etProfilePassword.getText().toString(),
                    binding.etProfileEmail.getText().toString(),
                    binding.etProfileFirstName.getText().toString(),
                    binding.etProfileLastName.getText().toString(),
                    binding.etProfilePhone.getText().toString(),
                    binding.etProfileStreet.getText().toString(),
                    binding.etProfileStreetNo.getText().toString(),
                    binding.etProfileCity.getText().toString(),
                    binding.etProfileZip.getText().toString()
            );
        });
    }

    @Override
    public void setProfileData(String username, String password, String email, String firstName,
                               String lastName, String phone, String street, String houseNo,
                               String city, String zip) {
        binding.etProfileUsername.setText(username);
        binding.etProfilePassword.setText(password);
        binding.etProfileEmail.setText(email);
        binding.etProfileFirstName.setText(firstName);
        binding.etProfileLastName.setText(lastName);
        binding.etProfilePhone.setText(phone);
        binding.etProfileStreet.setText(street);
        binding.etProfileStreetNo.setText(houseNo);
        binding.etProfileCity.setText(city);
        binding.etProfileZip.setText(zip);
    }

    @Override
    public void showUpdateSuccess() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.profile_update_success_title)
                .setMessage(R.string.profile_update_success_msg)
                .setIcon(R.drawable.ic_check_circle)
                .setPositiveButton(R.string.common_ok, (dialog, which) -> {
                    dialog.dismiss();
                })
                .setNegativeButton(R.string.common_cancel, (dialog, which) -> {
                    dialog.dismiss();
                })
                .show();
    }

    @Override
    public void showUpdateError(String message) {
        // Χρησιμοποιούμε getString για να βάλουμε το δυναμικό μήνυμα λάθους μέσα στο string resource
        String formattedMessage = getString(R.string.profile_update_error_msg, message);

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.profile_update_error_title)
                .setMessage(formattedMessage)
                .setIcon(R.drawable.ic_error)
                .setPositiveButton(R.string.common_ok, (dialog, which) -> {
                    dialog.dismiss();
                })
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}