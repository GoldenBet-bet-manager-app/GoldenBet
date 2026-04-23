package gr.goldenbet.view.games.fruitgame;

import android.graphics.Color;
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

import gr.goldenbet.databinding.FragmentFruitGameBinding;


public class FruitGameFragment extends Fragment implements FruitGameView{
    private FruitGamePresenter presenter;
    private FragmentFruitGameBinding binding;
    private String playerId,currentGameName,betamount;


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
        presenter.initializedata(currentGameName);
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


   @Override
    public void randomizeGrid() {
        for (int i = 0; i < binding.slotGrid.getChildCount(); i++) {
            TextView cell = (TextView) binding.slotGrid.getChildAt(i);
            cell.setText(symbols[random.nextInt(symbols.length)]);
        }
    }

    @Override
    public void setImageBackground(int imageBackgroundId) {
        binding.imageView.setImageResource(imageBackgroundId);
    }

    @Override
    public void setCellsToSymbol(int[] indices, String symbol, int textcolor) {
        for (int i : indices) {
            TextView cell = (TextView) binding.slotGrid.getChildAt(i);
            cell.setText(symbol);
            cell.setBackgroundColor(Color.parseColor("#50FFFFFF"));
            cell.setTextSize(50f);
            cell.setTextColor(textcolor);
        }
    }

    @Override
    public void clearFruitGrid() {
        for (int i = 0; i < binding.slotGrid.getChildCount(); i++) {
            TextView cell = (TextView) binding.slotGrid.getChildAt(i);
            cell.setText("");
        }
    }

    @Override
    public void showMessage(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        binding.spinButton.setEnabled(true);
        binding.etbetAmount.setEnabled(true);
    }

    @Override
    public void showBalance(BigDecimal balance) {
        binding.tvbalance.setText("Υπόλοιπο: "+String.valueOf(balance));
    }

    @Override
    public void showSpinResult(String result) {
        binding.tvresult.setText(result);
        binding.spinButton.setEnabled(true);
        binding.etbetAmount.setEnabled(true);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}