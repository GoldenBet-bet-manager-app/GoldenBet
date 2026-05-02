package gr.goldenbet.view.mainMenu.profile;

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

import gr.goldenbet.R;
import gr.goldenbet.databinding.FragmentProfileBinding;
import gr.goldenbet.domain.Player;
import gr.goldenbet.view.mainMenu.MainMenuActivity;

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

        String playerUsername = ((MainMenuActivity) getActivity()).getPlayerUsername();

        presenter.loadProfile(playerUsername);

        binding.btnProfileSave.setOnClickListener(v -> {

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
    public void updateProfileInfo(String username, String password, String email, String firstName,
                                  String lastName, String phone, String street, String houseNo,
                                  String city, String zip) {

        FragmentActivity activity = getActivity();

        if (activity != null && binding != null) {

            activity.runOnUiThread(() -> {
                if (binding != null) {
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
            });

        }

    }

    @Override
    public void updateProfileInfo(Player p){

        FragmentActivity activity = getActivity();

        if (activity != null && binding != null) {

            activity.runOnUiThread(() -> {
                if (binding != null) {
                    binding.etProfileUsername.setText(p.getUsername());
                    binding.etProfilePassword.setText(p.getPassword());
                    binding.etProfileEmail.setText(p.getEmail());
                    binding.etProfileFirstName.setText(p.getName());
                    binding.etProfileLastName.setText(p.getSurname());
                    binding.etProfilePhone.setText(p.getPhone());
                    binding.etProfileStreet.setText(p.getAddress().getStreet());
                    binding.etProfileStreetNo.setText(p.getAddress().getStreetNumber());
                    binding.etProfileCity.setText(p.getAddress().getCity());
                    binding.etProfileZip.setText(p.getAddress().getZipCode());
                }
            });

        }

    }


    @Override
    public void showSaveConfirmation(Player player) {
        androidx.fragment.app.FragmentActivity activity = getActivity();

        if (activity != null && isAdded()) {
            new MaterialAlertDialogBuilder(activity)
                    .setTitle(R.string.profile_confirm_save_title)
                    .setMessage(R.string.profile_confirm_save_msg)
                    .setIcon(R.drawable.ic_save)
                    .setPositiveButton(R.string.common_ok, (dialog, which) -> {

                        presenter.performSave(player);
                    })
                    .setNegativeButton(R.string.common_cancel, (dialog, which) -> dialog.dismiss())
                    .show();
        }
    }

    @Override
    public void showUpdateSuccess() {
        FragmentActivity activity = getActivity();

        if (activity != null && isAdded()) {
            activity.runOnUiThread(() -> {
                new MaterialAlertDialogBuilder(activity) // Χρήση της ασφαλούς activity
                        .setTitle(R.string.profile_update_success_title)
                        .setMessage(R.string.profile_update_success_msg)
                        .setIcon(R.drawable.ic_check_circle)
                        .setPositiveButton(R.string.common_ok, (dialog, which) -> dialog.dismiss())
                        .show();
            });
        }
    }

    @Override
    public void showUpdateError(String message) {
        androidx.fragment.app.FragmentActivity activity = getActivity();
        if (activity != null && isAdded()) {
            activity.runOnUiThread(() -> {
                String formattedMessage = getString(R.string.profile_update_error_msg, message);
                new MaterialAlertDialogBuilder(activity)
                        .setTitle(R.string.profile_update_error_title)
                        .setMessage(formattedMessage)
                        .setIcon(R.drawable.ic_error)
                        .setPositiveButton(R.string.common_ok, (dialog, which) -> dialog.dismiss())
                        .show();
            });
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}