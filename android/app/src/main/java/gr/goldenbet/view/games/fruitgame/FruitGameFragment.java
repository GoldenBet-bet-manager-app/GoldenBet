package gr.goldenbet.view.games.fruitgame;

import android.app.AlertDialog;
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
import android.widget.TextView;
import android.widget.Toast;

import java.math.BigDecimal;
import java.util.Random;

import gr.goldenbet.R;
import gr.goldenbet.databinding.FragmentFruitGameBinding;
import gr.goldenbet.domain.Game;
import gr.goldenbet.domain.Player;
import gr.goldenbet.utils.ImageUtils;
import gr.goldenbet.view.mainMenu.MainMenuActivity;
import gr.goldenbet.view.mainMenu.search.SearchFragment;


public class FruitGameFragment extends Fragment implements FruitGameView {
    private FruitGamePresenter presenter;
    private FragmentFruitGameBinding binding;
    private String currentGameName, betamount;
    private  Game game;
    Random random = new Random();
    private Player player;

    public FruitGameFragment() {

    }

    public static FruitGameFragment newInstance(Game game) {
        Bundle args = new Bundle();
        args.putSerializable("GAME_OBJECT", game);
        FruitGameFragment fragment = new FruitGameFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
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

        binding = FragmentFruitGameBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        player = ((MainMenuActivity) getActivity()).getCurrentPlayer();
        String ip = getString(R.string.master_ip);
        int port = getResources().getInteger(R.integer.master_port);
        presenter = new FruitGamePresenter(this, game, ip, port, player);
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



        binding.spinButton.setOnClickListener(v -> {
            SharedPreferences prefs = requireActivity().getSharedPreferences("GoldenBetPrefs", Context.MODE_PRIVATE);
            prefs.edit().putBoolean("PLAYED_" + currentGameName, true).apply();
            betamount = binding.etbetAmount.getText().toString();
            presenter.spinClicked(betamount);
        });

        binding.btnBack.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().popBackStack();
        });

    }

    @Override
    public void randomizeGrid(String[] symbols) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                for (int i = 0; i < binding.slotGrid.getChildCount(); i++) {
                    TextView cell = (TextView) binding.slotGrid.getChildAt(i);
                    String symbol=symbols[random.nextInt(symbols.length)];
                    cell.setText(symbol);
                    cell.setBackgroundColor(Color.TRANSPARENT);
                    if (symbol.equals("7")) {
                        cell.setTextColor(Color.parseColor("#E70614"));
                    } else {
                        cell.setTextColor(Color.WHITE);
                    }
                    cell.setAlpha(1.0f);
                    cell.setTextSize(40f);
                }
            });
        }
    }

    @Override
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
    public void showFinalGrid(int[] winIndices, String winSymbol, String[] allSymbols, int winColor) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                boolean isWin = (winIndices != null && winIndices.length > 0);
                for (int i = 0; i < binding.slotGrid.getChildCount(); i++) {
                    TextView cell = (TextView) binding.slotGrid.getChildAt(i);
                    boolean isWinningCell = false;
                    if (isWin) {
                        for (int idx : winIndices) {
                            if (idx == i) {
                                isWinningCell = true;
                                break;
                            }
                        }
                    }

                    if (isWinningCell) {
                        cell.setText(winSymbol);
                        cell.setBackgroundColor(Color.parseColor("#80FFFFFF"));
                        if (winSymbol.equals("7")) {
                            cell.setTextColor(Color.parseColor("#E70614"));
                        } else {
                            cell.setTextColor(winColor);
                        }
                        cell.setAlpha(1.0f);
                        cell.setTextSize(50f);

                    } else {
                        String randomSymbol = allSymbols[random.nextInt(allSymbols.length)];
                        while (isWin && randomSymbol.equals(winSymbol)) {
                            randomSymbol = allSymbols[random.nextInt(allSymbols.length)];
                        }
                        cell.setText(randomSymbol);
                        cell.setBackgroundColor(Color.TRANSPARENT);
                        if (randomSymbol.equals("7")) {
                            cell.setTextColor(Color.parseColor("#E70614"));
                        } else {
                            cell.setTextColor(Color.WHITE);
                        }
                        cell.setAlpha(1.0f);
                        cell.setTextSize(40f);
                    }
                }
            });
        }
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