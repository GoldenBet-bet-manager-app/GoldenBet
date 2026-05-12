package gr.goldenbet.view.games.fruitgame;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.math.BigDecimal;
import java.util.Random;

import gr.goldenbet.R;
import gr.goldenbet.databinding.FragmentFruitGameBinding;
import gr.goldenbet.domain.Player;
import gr.goldenbet.view.mainMenu.MainMenuActivity;
import gr.goldenbet.view.mainMenu.search.SearchFragment;


public class FruitGameFragment extends Fragment implements FruitGameView {
    private FruitGamePresenter presenter;
    private FragmentFruitGameBinding binding;
    private String currentGameName, betamount;
    Random random = new Random();
    private Player player;

    public FruitGameFragment() {

    }

    public static FruitGameFragment newInstance(String gameName) {
        Bundle args = new Bundle();
        args.putString("GAME_NAME", gameName);
        FruitGameFragment fragment = new FruitGameFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            currentGameName = getArguments().getString("GAME_NAME");

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentFruitGameBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        player = ((MainMenuActivity) getActivity()).getCurrentPlayer();
        String ip = getString(R.string.master_ip);
        int port = getResources().getInteger(R.integer.master_port);
        presenter = new FruitGamePresenter(this, currentGameName, ip, port, player);
        binding.tvTitle.setText(currentGameName.toUpperCase());
        presenter.initializedata(currentGameName);

        binding.btnBack.setOnClickListener(v -> {
            //requireActivity().getSupportFragmentManager().beginTransaction()
                   // .replace(R.id.fragment_container, SearchFragment.newInstance())
                   // .commit();
        });

        binding.spinButton.setOnClickListener(v -> {
            SharedPreferences prefs = requireActivity().getSharedPreferences("GoldenBetPrefs", Context.MODE_PRIVATE);
            prefs.edit().putBoolean("PLAYED_" + currentGameName, true).apply();
            betamount = binding.etbetAmount.getText().toString();
            presenter.spinClicked(betamount);
        });

    }

    @Override
    public void randomizeGrid(String[] symbols) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                for (int i = 0; i < binding.slotGrid.getChildCount(); i++) {
                    TextView cell = (TextView) binding.slotGrid.getChildAt(i);
                    cell.setText(symbols[random.nextInt(symbols.length)]);
                }
            });
        }
    }

    @Override
    public void setImageBackground(int imageBackgroundId) {
        binding.imageView.setImageResource(imageBackgroundId);
    }

    @Override
    public void showErrorDialog(String title, String message) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
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
                binding.tvbalance.setText("Υπόλοιπο: " + balance + " FUN");
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
    public void setCellsToSymbol(int[] indices, String[] symbols, int textcolor) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                int randomIndex = random.nextInt(symbols.length);
                String randomsymbol = symbols[randomIndex];
                for (int i : indices) {
                    TextView cell = (TextView) binding.slotGrid.getChildAt(i);
                    cell.setText(randomsymbol);
                    cell.setBackgroundColor(Color.parseColor("#50FFFFFF"));
                    cell.setTextSize(50f);
                    cell.setTextColor(textcolor);
                }
            });
        }
    }

    @Override
    public void clearFruitGrid() {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                for (int i = 0; i < binding.slotGrid.getChildCount(); i++) {
                    TextView cell = (TextView) binding.slotGrid.getChildAt(i);
                    cell.setText("");
                }
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
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}