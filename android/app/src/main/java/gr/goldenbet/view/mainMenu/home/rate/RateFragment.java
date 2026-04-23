package gr.goldenbet.view.mainMenu.home.rate;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import gr.goldenbet.databinding.FragmentRateBinding;

public class RateFragment extends Fragment implements RateView {

    private RatePresenter presenter;
    private FragmentRateBinding binding;
    private float currentstars;
    private String currentGameName;
    private String[] games = {
            "Pharaoh's Dynasty",
            "Dragon's Lair",
            "Gates of Olympus",
            "Pirate's Bounty",
            "High Roller Poker",
            "Fruit Fiesta",
            "Lucky Seven",
            "Pharaoh's Dynasty"
    };

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

        presenter = new RatePresenter(this);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_dropdown_item_1line, games
        );
        binding.autoCompleteGameName.setAdapter(adapter);

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
        Toast.makeText(getContext(), getString(messageResId), Toast.LENGTH_SHORT).show();
    }


    @Override
    public void clearRateForm() {
        currentstars = 0.0f;
        binding.ratingBar.setRating(0.0f);
        binding.autoCompleteGameName.setText("");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}