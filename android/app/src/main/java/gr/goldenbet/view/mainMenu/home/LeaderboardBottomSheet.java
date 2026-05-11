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
import java.util.List;
import java.util.Map;
import gr.goldenbet.R;
import gr.goldenbet.databinding.BottomSheetLeaderboardBinding;
import gr.goldenbet.databinding.BottomSheetLeaderboardBinding;
import gr.goldenbet.view.utils.LeaderboardAdapter;

public class LeaderboardBottomSheet extends BottomSheetDialogFragment {

    private BottomSheetLeaderboardBinding binding;
    private final HomePresenter presenter;
    private final String currentUsername;
    private LeaderboardAdapter adapter;

    // Χρήση Raw Thread και ενός flag για τον έλεγχο
    private Thread liveUpdateThread;
    private volatile boolean isRunning = true;

    public LeaderboardBottomSheet(HomePresenter presenter, String currentUsername) {
        this.presenter = presenter;
        this.currentUsername = currentUsername;
    }

    @Override
    public void onStart() {
        super.onStart();
        // toggle to Full Screen
        Dialog dialog = getDialog();
        if (dialog != null) {
            FrameLayout bottomSheet = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                BottomSheetBehavior.from(bottomSheet).setState(BottomSheetBehavior.STATE_EXPANDED);
                bottomSheet.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = BottomSheetLeaderboardBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        adapter = new LeaderboardAdapter(currentUsername);
        binding.rvFullLeaderboard.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvFullLeaderboard.setAdapter(adapter);

        startManualLoop();
    }

    private void startManualLoop() {
        isRunning = true;
        liveUpdateThread = new Thread(() -> {
            while (isRunning) {

                // players stats fetch
                presenter.fetchLiveLeaderboard();

                try {
                    // Wait for 15 seconds to update stats again
                    Thread.sleep(15000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
        liveUpdateThread.start();
    }

    public void onLeaderboardUpdated(List<Map.Entry<String, BigDecimal>> sortedData) {
        if (getActivity() == null || binding == null) return;

        getActivity().runOnUiThread(() -> {
            // Update Podium
            updatePodium(sortedData);

            // Update list (Top 4-30)
            if (sortedData.size() > 3) {
                adapter.setItems(sortedData.subList(3, Math.min(30, sortedData.size())));
            }
        });
    }

    private void updatePodium(List<Map.Entry<String, BigDecimal>> data) {
        if (data.isEmpty()) return;

        // First (Gold)
        binding.txtRank1Name.setText(data.get(0).getKey());
        if (data.get(0).getKey().equals(currentUsername)) {
            binding.txtRank1Name.setTextColor(getResources().getColor(R.color.gold_classic));
            binding.txtRank1Name.append(" (YOU)");
        }
        binding.txtRank1Profit.setText(String.format("%.2f €", data.get(0).getValue()));

        // second and third
        if (data.size() >= 2) binding.txtRank2Name.setText(data.get(1).getKey());
        if (data.size() >= 3) binding.txtRank3Name.setText(data.get(2).getKey());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        isRunning = false; // Stops loop
        if (liveUpdateThread != null) liveUpdateThread.interrupt();
        binding = null;
    }
}