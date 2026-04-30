package gr.goldenbet.view.mainMenu.home.stats;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.math.BigDecimal;
import java.util.Map;

import gr.goldenbet.R;
import gr.goldenbet.domain.PlayerStats;
import gr.goldenbet.view.mainMenu.home.stats.StatsPresenter;

public class StatsFragment extends Fragment implements StatsView {
    private StatsPresenter presenter;
    private TextView txtTotalProfit, txtWinRate, txtBiggestWin; // κλπ
    private View[] bars = new View[7];

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        txtTotalProfit = view.findViewById(R.id.txtTotalProfit);

        bars[0] = view.findViewById(R.id.barMon).findViewById(R.id.barView);
        bars[1] = view.findViewById(R.id.barTue).findViewById(R.id.barView);
        // ... και για τις 7 ημέρες ...

        presenter = new StatsPresenter(this);
        presenter.loadPlayerStats("user123");
    }

    @Override
    public void showStats(PlayerStats stats) {
        txtTotalProfit.setText(stats.totalProfit + " FUN");
        // Ενημέρωση των υπόλοιπων KPIs...

        // Δυναμική αλλαγή ύψους μπάρας
        updateBarGraph(stats.weeklyActivity);
    }

    private void updateBarGraph(Map<String, BigDecimal> activity) {
        // Υπολογισμός μέγιστης τιμής για scaling
        float maxVal = 0;
        for (BigDecimal val : activity.values()) {
            if (val.floatValue() > maxVal) maxVal = val.floatValue();
        }

        // Ενημέρωση ύψους (Layout Weight ή Height σε pixels)
        // Εδώ χρησιμοποιούμε την προσέγγιση με LayoutParams
        for (int i = 0; i < 7; i++) {
            float currentVal = activity.getOrDefault(getDayKey(i), BigDecimal.ZERO).floatValue();
            int newHeight = (int) ((currentVal / maxVal) * 400); // 400px max height

            ViewGroup.LayoutParams params = bars[i].getLayoutParams();
            params.height = Math.max(newHeight, 10); // Minimum height 10px για να φαίνεται
            bars[i].setLayoutParams(params);
        }
    }

    private String getDayKey(int i) {
        String[] days = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
        return days[i];
    }

    @Override public void showLoading() { /* Εμφάνιση ProgressBar */ }
    @Override public void showError(String message) { /* Toast.makeText... */ }
}