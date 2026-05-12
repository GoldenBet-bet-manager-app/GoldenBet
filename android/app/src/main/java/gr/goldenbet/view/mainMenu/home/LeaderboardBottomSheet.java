package gr.goldenbet.view.mainMenu.home;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import gr.goldenbet.R;
import gr.goldenbet.databinding.BottomSheetLeaderboardBinding;
import gr.goldenbet.view.mainMenu.MainMenuActivity;
import gr.goldenbet.view.utils.LeaderboardAdapter;

public class LeaderboardBottomSheet extends BottomSheetDialogFragment {

    private BottomSheetLeaderboardBinding binding;
    private HomePresenter presenter;
    private String currentUsername;
    private LeaderboardAdapter adapter;
    private List<Map.Entry<String, BigDecimal>> initialData;

    private Thread liveUpdateThread;
    private volatile boolean isRunning = true;

    public LeaderboardBottomSheet(HomePresenter presenter, List<Map.Entry<String, BigDecimal>> initialData) {
        this.presenter = presenter;
        this.initialData = initialData;


        if (getActivity() instanceof MainMenuActivity) {
            MainMenuActivity activity = (MainMenuActivity) getActivity();
            if (activity.getCurrentPlayer() != null) {
                this.currentUsername = activity.getCurrentPlayer().getUsername();
            }
        }
    }

    public LeaderboardBottomSheet() {
    }

    public void setPresenter(HomePresenter presenter) {
        this.presenter = presenter;
    }

    public void setCurrentUsername(String currentUsername) {
        this.currentUsername = currentUsername;
    }

    public void setInitialData(List<Map.Entry<String, BigDecimal>> data) {
        this.initialData = data;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            FrameLayout bottomSheet = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                // Toggle to full screen
                BottomSheetBehavior.from(bottomSheet).setState(BottomSheetBehavior.STATE_EXPANDED);
                bottomSheet.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = BottomSheetLeaderboardBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        adapter = new LeaderboardAdapter(currentUsername);
        binding.rvFullLeaderboard.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvFullLeaderboard.setAdapter(adapter);

        if (initialData != null) {
            updateUI(initialData);
        }

        startManualLoop();
    }

    private void startManualLoop() {
        isRunning = true;
        liveUpdateThread = new Thread(() -> {
            while (isRunning) {
                if (presenter != null) {
                    presenter.fetchLiveLeaderboard();
                }
                try {
                    Thread.sleep(15000); // waiting for 15 seconds
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
        liveUpdateThread.start();
    }

    // Method that is called from HomeFragment for live update
    public void onLeaderboardUpdated(List<Map.Entry<String, BigDecimal>> sortedData) {
        if (getActivity() == null || binding == null) return;
        getActivity().runOnUiThread(() -> updateUI(sortedData));
    }

    // Central method that updates UI
    private void updateUI(List<Map.Entry<String, BigDecimal>> data) {
        if (binding == null || data == null) return;

        // Update of the Podium (Top 3)
        updatePodium(data);

        // Update list (Rank 4-30)
        if (data.size() > 3) {
            adapter.setItems(data.subList(3, Math.min(30, data.size())));
        } else {
            adapter.setItems(new ArrayList<>());
        }
    }

    private void updatePodium(List<Map.Entry<String, BigDecimal>> data) {
        if (data == null || data.isEmpty()) return;

        // first
        binding.txtRank1Name.setText(data.get(0).getKey());
        binding.txtRank1Profit.setText(String.format("%.2f €", data.get(0).getValue()));
        if (data.get(0).getKey().equals(currentUsername)) {
            binding.txtRank1Name.append(" (YOU)");
            binding.txtRank1Name.setTextColor(getResources().getColor(R.color.gold_classic));
        } else {
            binding.txtRank1Name.setTextColor(getResources().getColor(R.color.text_dark));
        }

        // second
        if (data.size() >= 2) {
            binding.txtRank2Name.setText(data.get(1).getKey());
            if (data.get(1).getKey().equals(currentUsername)) binding.txtRank2Name.append(" (YOU)");
        }

        // third
        if (data.size() >= 3) {
            binding.txtRank3Name.setText(data.get(2).getKey());
            if (data.get(2).getKey().equals(currentUsername)) binding.txtRank3Name.append(" (YOU)");
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        isRunning = false;
        if (liveUpdateThread != null) {
            liveUpdateThread.interrupt();
        }
        binding = null; // To avoid memory leaks
    }
}