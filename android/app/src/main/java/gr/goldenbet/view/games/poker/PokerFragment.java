package gr.goldenbet.view.games.poker;

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

public class PokerFragment extends Fragment implements PokerView {

    private PokerPresenter presenter;
    private FragmentPokerBinding binding;
    private String playerId, currentGameName, betamount;

    public PokerFragment() {
        // Required empty public constructor
    }

    public static PokerFragment newInstance(String playerId, String gameName) {
        Bundle args = new Bundle();
        args.putString("PLAYER_ID", playerId);
        args.putString("GAME_NAME", gameName);
        PokerFragment fragment = new PokerFragment();
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
        binding = FragmentPokerBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        presenter = new PokerPresenter(this, playerId, currentGameName);

        if (currentGameName != null) {
            binding.tvTitle.setText(currentGameName.toUpperCase());
        }

        binding.btnDeal.setOnClickListener(v -> {
            betamount = binding.etbetAmount.getText().toString();
            binding.btnDeal.setEnabled(false);
            binding.etbetAmount.setEnabled(false);

            presenter.onDealClicked(betamount);
        });
    }

    @Override
    public void showMessage(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        binding.btnDeal.setEnabled(true);
        binding.etbetAmount.setEnabled(true);
    }

    @Override
    public void showBalance(BigDecimal balance) {
        binding.tvbalance.setText("Υπόλοιπο: " + balance);
    }

    @Override
    public void showFlipResult(String result) {
        binding.tvresult.setText(result);
        binding.btnDeal.setEnabled(true);
        binding.etbetAmount.setEnabled(true);
    }

    @Override
    public void showDealtCards(ArrayList<String> hand) {
        ImageView[] cardViews = {
                binding.ivCard1, binding.ivCard2, binding.ivCard3,
                binding.ivCard4, binding.ivCard5
        };

        for (int i = 0; i < hand.size(); i++) {
            ImageView currentCardView = cardViews[i];
            String imageName = hand.get(i);

            int imageResId = getResources().getIdentifier(imageName, "drawable", requireContext().getPackageName());

            // 2. Υπολογίζουμε την καθυστέρηση και αν είναι η τελευταία κάρτα
            long delay = i * 200L;
            boolean isLastCard = (i == hand.size() - 1); // Αν το i είναι 4 (το 5ο φύλλο)

            // 3. Ξεκινάμε το 3D Flip
            currentCardView.animate()
                    .rotationY(90f)
                    .setDuration(150)
                    .setStartDelay(delay)
                    .withEndAction(() -> {
                        // Αλλάζουμε την εικόνα στη μέση της περιστροφής
                        if (imageResId != 0) {
                            currentCardView.setImageResource(imageResId);
                        } else {
                            currentCardView.setImageResource(R.drawable.back_dark);
                        }

                        currentCardView.setRotationY(-90f);
                        currentCardView.animate()
                                .rotationY(0f)
                                .setDuration(150)
                                .withEndAction(() -> {
                                    // ΟΤΑΝ ΟΛΟΚΛΗΡΩΘΕΙ ΤΟ ΓΥΡΙΣΜΑ ΤΟΥ ΤΕΛΕΥΤΑΙΟΥ ΦΥΛΛΟΥ
                                    if (isLastCard) {
                                        presenter.onAnimationFinished();
                                    }
                                })
                                .start();
                    })
                    .start();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}