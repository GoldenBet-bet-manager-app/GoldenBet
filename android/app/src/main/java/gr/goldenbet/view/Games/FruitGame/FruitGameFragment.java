package gr.goldenbet.view.Games.FruitGame;

import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.math.BigDecimal;
import java.util.Random;

import gr.goldenbet.R;
import gr.goldenbet.databinding.FragmentFruitGameBinding;
import gr.goldenbet.domain.PlayerApp;


public class FruitGameFragment extends Fragment implements FruitGameView{
    private FruitGamePresenter presenter;
    private FragmentFruitGameBinding binding;
    private String playerId,currentGameName;
    private PlayerApp player;
    private TextView gameTitletext,balancetext,resulttext;
    private EditText betamounttext;
    private String betamount;

    private final String[] games = {"Gates of Olympus", "Fruit Fiesta", "Lucky Seven"};
    private String[] symbols;
    Random random=new Random();
    public FruitGameFragment() {

    }

    public static FruitGameFragment newInstance(String playerId,String gameName) {
        Bundle args = new Bundle();
        args.putString("PLAYER_ID", playerId);
        args.putString("GAME_NAME", gameName);
        FruitGameFragment fragment = new FruitGameFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getArguments()!=null){
            playerId=getArguments().getString("PLAYER_ID");
            currentGameName=getArguments().getString("GAME_NAME");

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
        presenter=new FruitGamePresenter(this,playerId,currentGameName);
        binding.tvTitle.setText(currentGameName.toUpperCase());
        initializedata(currentGameName);
       // presenter.initBalance();
        binding.spinButton.setOnClickListener(v -> {
            betamount=binding.etbetAmount.getText().toString();
            binding.spinButton.setEnabled(false);
            binding.etbetAmount.setEnabled(false);
            binding.tvresult.setText("Γυρίζει... 🎰");
            binding.tvresult.setTextColor(Color.LTGRAY);
            presenter.spinClicked(betamount);
        });

    }

    private void initializedata(String currentGameName) {
        if (currentGameName.equals(games[0])) {
            binding.imageView.setImageResource(R.drawable.gates_of_olympus_logo);
            symbols = new String[]{"⚡", " "};
            setCellsToSymbol(new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8}, "⚡", Color.parseColor("#EDD606"));
        } else if (currentGameName.equals(games[1])) {
            binding.imageView.setImageResource(R.drawable.fruit_fiesta_logo);
            symbols = new String[]{"🍒", "🍋", "🍉", "🍇", "🍓"};
            setCellsToSymbol(new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8}, "🍋", Color.parseColor("#FFFFFF"));
        } else {
            binding.imageView.setImageResource(R.drawable.lucky_seven_logo);
            symbols = new String[]{"7", " "};
            setCellsToSymbol(new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8}, "7", Color.parseColor("#E70614"));
        }
    }
    private void setCellsToSymbol(int[] indices, String symbol, int textcolor) {
        for (int i : indices) {
            TextView cell = (TextView) binding.slotGrid.getChildAt(i);
            cell.setText(symbol);
            cell.setBackgroundColor(Color.parseColor("#50FFFFFF"));
            cell.setTextSize(50f);
            cell.setTextColor(textcolor);
        }
    }
    private void showPattern(double multiplier) {
        String symbol;
        for (int i = 0; i < binding.slotGrid.getChildCount(); i++) {
            TextView cell = (TextView) binding.slotGrid.getChildAt(i);
            cell.setText("");
        }

        if (currentGameName.equals(games[0])) {//Gate of olympus
            symbol = "⚡";
            if (multiplier == 40.0) {
                setCellsToSymbol(new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8}, symbol, Color.parseColor("#EDD606"));
            } else if (multiplier == 6.5) {
                setCellsToSymbol(new int[]{0, 2, 4, 6, 8}, symbol, Color.parseColor("#EDD606"));
            } else if (multiplier == 2.0) {
                setCellsToSymbol(new int[]{1, 3, 4, 5, 7}, symbol, Color.parseColor("#EDD606"));
            } else if (multiplier == 1.0) {
                setCellsToSymbol(new int[]{3, 4, 5}, symbol, Color.parseColor("#EDD606"));
            }

        } else if (currentGameName.equals(games[1])) {//Fruit Fiesta
            symbol = symbols[random.nextInt(symbols.length)];
            int color = Color.WHITE;

            if (multiplier == 10.0) {
                setCellsToSymbol(new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8}, symbol, color);
            } else if (multiplier == 2.5) {
                setCellsToSymbol(new int[]{0, 2, 4, 6, 8}, symbol, color);
            } else if (multiplier == 2.0) {
                setCellsToSymbol(new int[]{1, 3, 4, 5, 7}, symbol, color);
            } else if (multiplier == 1.3) {
                setCellsToSymbol(new int[]{1, 4, 7}, symbol, color);
            } else if (multiplier == 1.1) {
                setCellsToSymbol(new int[]{0, 1, 2}, symbol, color);
            } else if (multiplier == 1.0) {
                setCellsToSymbol(new int[]{3, 4, 5}, symbol, color);
            } else if (multiplier == 0.5) {
                setCellsToSymbol(new int[]{3, 5}, symbol, color);
            } else if (multiplier == 0.1) {
                setCellsToSymbol(new int[]{4}, symbol, color);
            }
        } else {
            symbol = "7️";
            int color = Color.parseColor("#E70614"); // Κόκκινο

            if (multiplier >= 10.0) {
                // JACKPOT (10.0): Γεμίζει όλη η οθόνη!
                setCellsToSymbol(new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8}, symbol, color);

            } else if (multiplier == 2.5) {
                // Κέρδος 2.5: Σχήμα "X" (Διαγώνιες)
                setCellsToSymbol(new int[]{0, 2, 4, 6, 8}, symbol, color);

            } else if (multiplier == 2.0) {
                // Κέρδος 2.0: Σχήμα Σταυρού "+"
                setCellsToSymbol(new int[]{1, 3, 4, 5, 7}, symbol, color);

            } else if (multiplier == 1.3) {
                // Κέρδος 1.3: Μεσαία Κάθετη Γραμμή
                setCellsToSymbol(new int[]{1, 4, 7}, symbol, color);

            } else if (multiplier == 1.1) {
                // Κέρδος 1.1: Πάνω Οριζόντια Γραμμή
                setCellsToSymbol(new int[]{0, 1, 2}, symbol, color);

            } else if (multiplier == 1.0) {
                // Κέρδος 1.0: Μεσαία Οριζόντια Γραμμή (Κλασική γραμμή καζίνο)
                setCellsToSymbol(new int[]{3, 4, 5}, symbol, color);

            } else if (multiplier == 0.5) {
                // Κέρδος 0.5 (Μικρό): Δύο πλαϊνά κουτάκια
                setCellsToSymbol(new int[]{3, 5}, symbol, color);

            } else if (multiplier == 0.1) {
                // Κέρδος 0.1 (Ελάχιστο): Μόνο το κέντρο της οθόνης
                setCellsToSymbol(new int[]{4}, symbol, color);
            }
        }
    }
   @Override
    public void randomizeGrid() {
        for (int i = 0; i < binding.slotGrid.getChildCount(); i++) {
            TextView cell = (TextView) binding.slotGrid.getChildAt(i);
            cell.setText(symbols[random.nextInt(symbols.length)]);
        }
    }
    @Override
    public void showMessage(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showBalance(BigDecimal balance) {
        binding.tvbalance.setText("Υπόλοιπο: "+String.valueOf(balance));
    }

    @Override
    public void showSpinResult(String result) {
        binding.tvresult.setText(result);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}