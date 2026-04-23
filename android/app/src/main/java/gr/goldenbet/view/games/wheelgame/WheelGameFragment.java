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
        presenter = new WheelGamePresenter(this, playerId, currentGameName);
        binding.tvTitle.setText(currentGameName.toUpperCase());
        presenter.initializedata(currentGameName);
        binding.spinButton.setOnClickListener(v -> {
            betamount=binding.etbetAmount.getText().toString();
            binding.spinButton.setEnabled(false);
            binding.etbetAmount.setEnabled(false);
            binding.tvresult.setText("Γυρίζει ο τροχός...");
            binding.tvresult.setTextColor(Color.LTGRAY);
            presenter.spinClicked(betamount);
        });
    }



    @Override
    public void showMessage(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        binding.spinButton.setEnabled(true);
        binding.etbetAmount.setEnabled(true);
    }

    @Override
    public void showBalance(BigDecimal balance) {
        binding.tvbalance.setText("Υπόλοιπο: " + String.valueOf(balance));

    }

    @Override
    public void showSpinResult(String result,int color) {
        binding.tvresult.setText(result);
        binding.tvresult.setTextColor(color);
        binding.spinButton.setEnabled(true);
        binding.etbetAmount.setEnabled(true);
    }

    @Override
    public void setImageBackground(int imageBackgroundId) {
        binding.imageView.setImageResource(imageBackgroundId);
    }

    @Override
    public void spinWheel(int sliceIndex, BigDecimal payout, double multiplier, BigDecimal balance) {
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
                showBalance(balance);
                if (multiplier >= 10.0) {
                    showSpinResult(" 🏆 ΜΕΓΑΛΟ ΤΖΑΚΠΟΤ! ΚΕΡΔΙΣΕΣ " + payout + " FUN! 🏆", Color.parseColor("#FFD700"));
                } else if (payout.compareTo(BigDecimal.ZERO) > 0) {
                    showSpinResult(" Κέρδισες " + payout + " FUN! (x" + multiplier + ") 🎉", Color.parseColor("#4CAF50"));
                } else {
                    showSpinResult(" Δεν κέρδισες.Καλή τύχη στην επόμενη προσπάθεια!!!", Color.parseColor("#F44336"));
                }
            }
        });

        animator.start();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}