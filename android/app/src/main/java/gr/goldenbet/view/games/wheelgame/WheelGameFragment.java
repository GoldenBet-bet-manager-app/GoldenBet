package gr.goldenbet.view.games.wheelgame;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.MediaPlayer;
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

import gr.goldenbet.R;
import gr.goldenbet.databinding.FragmentWheelGameBinding;
import gr.goldenbet.domain.Game;
import gr.goldenbet.domain.Player;
import gr.goldenbet.utils.ImageUtils;
import gr.goldenbet.view.mainMenu.MainMenuActivity;
import gr.goldenbet.view.mainMenu.search.SearchFragment;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link WheelGameFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class WheelGameFragment extends Fragment implements WheelGameView {

    private FragmentWheelGameBinding binding;
    private String currentGameName,betamount;
    private Player player;
    private Game game;
    private WheelGamePresenter presenter;

    private final int TOTAL_SLICES = 11;

    public WheelGameFragment() {

    }


    public static WheelGameFragment newInstance( Game game) {
        WheelGameFragment fragment = new WheelGameFragment();
        Bundle args = new Bundle();
        args.putSerializable("GAME_OBJECT", game);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            game = (Game) getArguments().getSerializable("GAME_OBJECT");
            if (game != null) {
                currentGameName = game.getGameName();
            }
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
        player=((MainMenuActivity) getActivity()).getCurrentPlayer();
        String ip = getString(R.string.master_ip);
        int port = getResources().getInteger(R.integer.master_port);
        presenter = new WheelGamePresenter(this, game,ip,port,player);
        binding.tvTitle.setText(currentGameName.toUpperCase());
        presenter.initializedata();

        binding.btnIncreaseBet.setOnClickListener(v -> {
            String currentbet=binding.etbetAmount.getText().toString();
            double currentVal = currentbet.isEmpty() ? 0 : Double.parseDouble(currentbet);
            binding.etbetAmount.setText(String.valueOf(currentVal+5));

        });
        binding.btnDecreaseBet.setOnClickListener(v -> {
            String currentbet=binding.etbetAmount.getText().toString();
            double currentVal = currentbet.isEmpty() ? 0 : Double.parseDouble(currentbet);
            if(currentVal>=5) {
                binding.etbetAmount.setText(String.valueOf(currentVal - 5));
            }
        });
        binding.btnBack.setOnClickListener(v -> {
            getParentFragmentManager().popBackStack();
        });

        binding.spinButton.setOnClickListener(v -> {
            SharedPreferences prefs = requireActivity().getSharedPreferences("GoldenBetPrefs", Context.MODE_PRIVATE);
            prefs.edit().putBoolean("PLAYED_" + currentGameName, true).apply();
            betamount=binding.etbetAmount.getText().toString();
            presenter.spinClicked(betamount);
        });
    }

    @Override
    public void playSound(boolean isWin) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                int soundResource;
                if (isWin)
                    soundResource = R.raw.win_sound;
                else
                    soundResource = R.raw.lose_sound;

                MediaPlayer mediaPlayer = MediaPlayer.create(getContext(), soundResource);
                mediaPlayer.start();
                mediaPlayer.setOnCompletionListener(mp -> mp.release());

            });
        }
    }

    public void setImageBackground(String base64Image) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                if (base64Image != null && !base64Image.trim().isEmpty()) {
                    Bitmap bitmap = ImageUtils.decodeBase64ToBitmap(base64Image);
                    if (bitmap != null) {
                        binding.imageView.setImageBitmap(bitmap);
                    } else {
                        binding.imageView.setImageDrawable(null);
                    }
                } else {
                    binding.imageView.setImageDrawable(null);
                }
            });
        }
    }

    @Override
    public void showMessage(String message) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                lock(false);
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
                lock(false);
            });
        }
    }
    @Override
    public void showErrorDialog(String title, String message) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                new android.app.AlertDialog.Builder(requireContext())
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
    public void lock(Boolean flag) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                binding.spinButton.setEnabled(!flag);
                binding.etbetAmount.setEnabled(!flag);
                if (flag) {
                    binding.spinButton.setColorFilter(android.graphics.Color.argb(128, 0, 0, 0));
                } else {
                    binding.spinButton.clearColorFilter();
                }
            });
        }
    }

    @Override
    public void setWheelImage(int imageId) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                binding.wheel.setImageResource(imageId);
            });
        }
    }

    @Override
    public void spinWheel(int sliceIndex) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                float degreesPerSlice = 360f / TOTAL_SLICES;
                float targetAngle = -(sliceIndex * degreesPerSlice);
                float currentRotation = binding.wheel.getRotation();
                float totalRotation = currentRotation - (360f * 5) + (targetAngle - (currentRotation % 360));

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