package gr.goldenbet.view.games.wheelgame;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.Toast;

import java.math.BigDecimal;

import gr.goldenbet.databinding.FragmentWheelGameBinding;
import gr.goldenbet.domain.Player;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link WheelGameFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class WheelGameFragment extends Fragment implements WheelGameView {

    private FragmentWheelGameBinding binding;
    private String playerId, currentGameName,betamount;
    private WheelGamePresenter presenter;

    private final int TOTAL_SLICES = 11;

    public WheelGameFragment() {

    }


    public static WheelGameFragment newInstance(String playerId, String gameName) {
        WheelGameFragment fragment = new WheelGameFragment();
        Bundle args = new Bundle();
        args.putString("PLAYER_ID", playerId);
        args.putString("GAME_NAME", gameName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            playerId = getArguments().getString("PLAYER_ID");
            currentGameName = getArguments().getString("GAME_NAME");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentWheelGameBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        presenter = new WheelGamePresenter(this, currentGameName);
        binding.tvTitle.setText(currentGameName.toUpperCase());
        presenter.initializedata(currentGameName);
        binding.spinButton.setOnClickListener(v -> {
            betamount=binding.etbetAmount.getText().toString();
            presenter.spinClicked(betamount);
        });
    }


    @Override
    public void showMessage(String message) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                binding.spinButton.setEnabled(true);
                binding.etbetAmount.setEnabled(true);
            });
        }
    }

    @Override
    public void showBalance(BigDecimal balance) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                binding.tvbalance.setText("Υπόλοιπο: " + balance);
            });
        }
    }

    @Override
    public void showSpinResult(String result) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                binding.tvresult.setText(result);
                binding.spinButton.setEnabled(true);
                binding.etbetAmount.setEnabled(true);
            });
        }
    }

    @Override
    public void setImageBackground(int imageBackgroundId) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                binding.imageView.setImageResource(imageBackgroundId);
            });
        }
    }

    @Override
    public void spinWheel(int sliceIndex) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {

                float degreesPerSlice = 360f / TOTAL_SLICES;
                float targetAngle = (sliceIndex * degreesPerSlice) + (degreesPerSlice / 2);

                float currentRotation = binding.wheel.getRotation();
                float totalRotation = currentRotation + (360f * 5) + targetAngle - (currentRotation % 360);

                ObjectAnimator animator = ObjectAnimator.ofFloat(binding.wheel, "rotation", currentRotation, totalRotation);
                animator.setDuration(4000);
                animator.setInterpolator(new DecelerateInterpolator());

                animator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        presenter.onAnimationEnd();
                    }
                });

                animator.start();

            });
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}