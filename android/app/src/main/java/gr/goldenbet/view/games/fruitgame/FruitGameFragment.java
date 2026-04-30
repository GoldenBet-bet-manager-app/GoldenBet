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
import java.util.Random;

import gr.goldenbet.databinding.FragmentFruitGameBinding;
import gr.goldenbet.domain.Player;
import gr.goldenbet.domain.PlayerApp;


public class FruitGameFragment extends Fragment implements FruitGameView{
    private FruitGamePresenter presenter;
    private FragmentFruitGameBinding binding;
    private String playerId,currentGameName,betamount;
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
        Player mockPlayer = new Player("Test", "User", "tester", "1234");
        mockPlayer.setPlayerId("P123");
        PlayerApp.initializeApp(mockPlayer);
        PlayerApp.getInstance().addBalance(new BigDecimal("100000.0"));
        presenter=new FruitGamePresenter(this,playerId,currentGameName);
        binding.tvTitle.setText(currentGameName.toUpperCase());
        presenter.initializedata(currentGameName);
       // presenter.initBalance();
        binding.spinButton.setOnClickListener(v -> {
            betamount=binding.etbetAmount.getText().toString();
            presenter.spinClicked(betamount);
        });

    }


   @Override
    public void randomizeGrid(String [] symbols) {
        if(getActivity()!=null) {
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
    public void setCellsToSymbol(int[] indices, String symbol, int textcolor) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                for (int i : indices) {
                    TextView cell = (TextView) binding.slotGrid.getChildAt(i);
                    cell.setText(symbol);
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
    public void lock(Boolean flag) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                binding.spinButton.setEnabled(!flag);
                binding.etbetAmount.setEnabled(!flag);
            });
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}