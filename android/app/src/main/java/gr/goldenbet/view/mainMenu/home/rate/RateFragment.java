package gr.goldenbet.view.mainMenu.home.rate;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import java.util.ArrayList;

import gr.goldenbet.R;
import gr.goldenbet.databinding.FragmentRateBinding;
import gr.goldenbet.domain.Player;


public class RateFragment extends Fragment implements RateView {

    private RatePresenter presenter;
    private FragmentRateBinding binding;
    private float currentstars;
    private String currentGameName;
    public RateFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentRateBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        String ip = getString(R.string.master_ip);
        int port = getResources().getInteger(R.integer.master_port);
        SharedPreferences prefs = requireActivity().getSharedPreferences("GoldenBetPrefs", Context.MODE_PRIVATE);
        presenter = new RatePresenter(this,prefs,ip,port);
        presenter.initializedata();
        binding.btnBack.setOnClickListener(v -> {
            if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                getParentFragmentManager().popBackStack();
            }
        });

        binding.btnSubmitRate.setOnClickListener(v -> {
            currentstars = binding.ratingBar.getRating();
            currentGameName = binding.autoCompleteGameName.getText().toString();
            presenter.onRateClicked(currentGameName, currentstars);
        });
    }

    @Override
    public void showMessage(int messageResId) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                Toast.makeText(getContext(), getString(messageResId), Toast.LENGTH_SHORT).show();
            });
        }
    }

    @Override
    public void showMessage(String message) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
            });
        }
    }
    @Override
    public void showErrorDialog(int titleResId, int messageResId, String gameName) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                String title = getString(titleResId);
                String message = getString(messageResId, gameName);
                new AlertDialog.Builder(requireContext())
                        .setTitle(title)
                        .setMessage(message)
                        .setPositiveButton("ΟΚ", (dialog, which) -> dialog.dismiss())
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setCancelable(false)
                        .show();
            });
        }
    }


    @Override
    public void clearRateForm() {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                binding.ratingBar.setRating(0.0f);
                binding.autoCompleteGameName.setText("");

            });
        }
    }

    @Override
    public void setActiveGames(ArrayList<String> gameNames) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                        android.R.layout.simple_dropdown_item_1line, gameNames
                );
                binding.autoCompleteGameName.setAdapter(adapter);
            });
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}