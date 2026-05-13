package gr.goldenbet.view.games.poker;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import java.math.BigDecimal;
import java.util.ArrayList;

import gr.goldenbet.R;
import gr.goldenbet.databinding.FragmentPokerBinding;
import gr.goldenbet.domain.Game;
import gr.goldenbet.domain.Player;
import gr.goldenbet.view.mainMenu.MainMenuActivity;


public class PokerFragment extends Fragment implements PokerView {

    private PokerPresenter presenter;
    private FragmentPokerBinding binding;
    private String  currentGameName, betamount;
    private Game game;
    private Player player;

    public PokerFragment() {
        // Required empty public constructor
    }

    public static PokerFragment newInstance(Game game){

        Bundle args = new Bundle();
        args.putSerializable("GAME_OBJECT", game);
        PokerFragment fragment = new PokerFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState){

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
        binding = FragmentPokerBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);
        player = ((MainMenuActivity) getActivity()).getCurrentPlayer();

        presenter = new PokerPresenter(this,game ,player);
        binding.tvTitle.setText(currentGameName.toUpperCase());
        presenter.initializedata();

        binding.btnBack.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().popBackStack();
        });

        binding.btnIncreaseBet.setOnClickListener(v -> {
            String currentbet = binding.etbetAmount.getText().toString();
            double currentVal = currentbet.isEmpty() ? 0 : Double.parseDouble(currentbet);
            binding.etbetAmount.setText(String.valueOf(currentVal+5));

        });
        binding.btnDecreaseBet.setOnClickListener(v -> {
            String currentbet = binding.etbetAmount.getText().toString();
            double currentVal = currentbet.isEmpty() ? 0 : Double.parseDouble(currentbet);
            if(currentVal>=5) {
                binding.etbetAmount.setText(String.valueOf(currentVal - 5));
            }
        });
        binding.btnDeal.setOnClickListener(v -> {
            SharedPreferences prefs = requireActivity().getSharedPreferences("GoldenBetPrefs", Context.MODE_PRIVATE);
            prefs.edit().putBoolean("PLAYED_" + currentGameName, true).apply();
            betamount = binding.etbetAmount.getText().toString();
            binding.btnDeal.setEnabled(false);
            binding.etbetAmount.setEnabled(false);
            presenter.onDealClicked(betamount);
        });
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
                binding.btnDeal.setEnabled(!flag);
                binding.etbetAmount.setEnabled(!flag);
                if (flag) {
                    binding.btnDeal.setAlpha(0.5f);
                } else {
                    binding.btnDeal.setAlpha(1.0f);
                }
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

    @Override
    public void showWinResult(String result) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                binding.tvresult.setText(result);
                lock(false);
            });
        }
    }

    @Override
    public void showFlipResult(String result) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                binding.tvHandName.setText(result);
            });
        }
    }


    @Override
    public void showDealtCards(ArrayList<String> hand) {
        if(getActivity()!=null) {
            getActivity().runOnUiThread(() -> {
            ImageView[] cardViews = {
                    binding.ivCard1, binding.ivCard2, binding.ivCard3,
                    binding.ivCard4, binding.ivCard5
            };

            for (int i = 0; i < hand.size(); i++) {
                ImageView currentCardView = cardViews[i];
                String imageName = hand.get(i);
                int imageResId = getResources().getIdentifier(imageName, "drawable", requireContext().getPackageName());

                long delay = i * 400L;
                boolean isLastCard = (i == hand.size() - 1);

                currentCardView.animate()
                        .rotationY(90f)
                        .setDuration(250)
                        .setStartDelay(delay)
                        .withEndAction(() -> {
                            if (imageResId != 0) {
                                currentCardView.setImageResource(imageResId);
                            } else {
                                currentCardView.setImageResource(R.drawable.back_dark);
                            }

                            currentCardView.setRotationY(-90f);
                            currentCardView.animate()
                                    .rotationY(0f)
                                    .setDuration(250)
                                    .withEndAction(() -> {
                                        if (isLastCard) {
                                            presenter.onAnimationFinished();
                                        }
                                    })
                                    .start();
                        })
                        .start();
            }
        });
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}