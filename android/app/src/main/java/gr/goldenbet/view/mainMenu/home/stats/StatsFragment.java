package gr.goldenbet.view.mainMenu.home.stats;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import java.math.BigDecimal;
import java.util.Map;
import gr.goldenbet.R;
import gr.goldenbet.domain.PlayerStats;
import gr.goldenbet.view.mainMenu.MainMenuActivity;

public class StatsFragment extends Fragment implements StatsView {

    private StatsPresenter presenter;
    private TextView txtProfit, txtWinRate, txtPpa, txtLpl, txtPpw, txtBiggest, txtYMax, txtYMin;
    private View[] bars = new View[7];

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_stats, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        txtProfit = view.findViewById(R.id.txtTotalProfit);
        txtWinRate = view.findViewById(R.id.txtWinRateValue);
        txtPpa = view.findViewById(R.id.txtProfitAttemptValue);
        txtLpl = view.findViewById(R.id.txtLossPerLossValue);
        txtPpw = view.findViewById(R.id.txtProfitPerWinValue);
        txtBiggest = view.findViewById(R.id.txtBiggestWinRecord);
        txtYMax = view.findViewById(R.id.txtYMax);
        txtYMin = view.findViewById(R.id.txtYMin);

        bars[0] = view.findViewById(R.id.barMon);
        bars[1] = view.findViewById(R.id.barTue);
        bars[2] = view.findViewById(R.id.barWed);
        bars[3] = view.findViewById(R.id.barThu);
        bars[4] = view.findViewById(R.id.barFri);
        bars[5] = view.findViewById(R.id.barSat);
        bars[6] = view.findViewById(R.id.barSun);

        presenter = new StatsPresenter(this);

        if (getActivity() instanceof MainMenuActivity) {
            String playerId = ((MainMenuActivity) getActivity()).getCurrentPlayer().getPlayerId();
            String playerUsername = ((MainMenuActivity) getActivity()).getCurrentPlayer().getUsername();
            presenter.loadPlayerStats(playerId,playerUsername);
        }
    }

    @Override
    public void showStats(PlayerStats stats) {
        if (getActivity() == null) return;

        getActivity().runOnUiThread(() -> {
            BigDecimal total = stats.getTotalProfit();
            String sign = total.signum() >= 0 ? "+" : "-";
            txtProfit.setText(sign + stats.getTotalProfit() + " FUN");

            // Use of  colors.xml to change label colors depending on Win/Loss
            int colorWin = ContextCompat.getColor(requireContext(), R.color.stats_win);
            int colorLoss = ContextCompat.getColor(requireContext(), R.color.stats_loss);

            txtProfit.setTextColor(total.signum() >= 0 ? colorWin : colorLoss);

            TextView mostFun = getView().findViewById(R.id.txtMostFun);
            if (mostFun != null) {
                mostFun.setTextColor(total.signum() >= 0 ? colorWin : colorLoss);
            }


            txtWinRate.setText(String.format("%.1f%%", stats.getWinRate()));
            txtPpa.setText(stats.getProfitPerAttempt().toString());
            txtLpl.setText(stats.getLossPerLoss().toString());
            txtPpw.setText(stats.getProfitPerWin().toString());
            txtBiggest.setText(getString(R.string.stats_biggest_win) + ": " + stats.getBiggestWin() + " FUN");
            ((TextView)getView().findViewById(R.id.txtMostTime)).setText(getString(R.string.stats_most_time) + " " + stats.getMostPlayedGame());
            ((TextView)getView().findViewById(R.id.txtMostFun)).setText(getString(R.string.stats_most_fun) + " " + stats.getTotalProfit() + " FUN");
            ((TextView)getView().findViewById(R.id.txtBiggestWinRecord)).setText(getString(R.string.stats_biggest_win)+ " " + stats.getBiggestWin() + " FUN");

            renderChart(stats.getWeeklyActivity());
        });
    }

    private void renderChart(Map<String, BigDecimal> data) {
        float maxVal = 0.01f;
        String[] dayKeys = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};

        for (BigDecimal val : data.values()) {
            float absVal = Math.abs(val.floatValue());
            if (absVal > maxVal) maxVal = absVal;
        }

        txtYMax.setText("+" + (int)maxVal);
        txtYMin.setText("-" + (int)maxVal);


        float density = getResources().getDisplayMetrics().density;
        int maxBarHeightPx = (int) (140 * density);

        // Colors for each occasion
        int colorWin = ContextCompat.getColor(requireContext(), R.color.stats_win);
        int colorLoss = ContextCompat.getColor(requireContext(), R.color.stats_loss);
        int colorNeutral = ContextCompat.getColor(requireContext(), R.color.stats_neutral);

        for (int i = 0; i < 7; i++) {
            float currentVal = data.getOrDefault(dayKeys[i], BigDecimal.ZERO).floatValue();
            float absValue = Math.abs(currentVal);

            int calculatedHeight = (int) ((currentVal / maxVal) * maxBarHeightPx);

            View bar = bars[i];
            if (bar != null) {
                ViewGroup.LayoutParams params = bar.getLayoutParams();

                // Set bar height
                int minHeight = (currentVal > 0) ? (int)(8 * density) : (int)(4 * density);
                params.height = Math.max(calculatedHeight, minHeight);
                bar.setLayoutParams(params);

                // Set bar color depending on result (Win/Loss or no activity on a certain day)
                if (currentVal > 0) {
                    bar.setBackgroundColor(colorWin);
                } else if (currentVal < 0) {
                    bar.setBackgroundColor(colorLoss);
                } else {
                    bar.setBackgroundColor(colorNeutral);
                }
            }

            // DAY LABEL UPDATE
            int labelId = getResources().getIdentifier("txtLabel" + dayKeys[i], "id", getActivity().getPackageName());
            TextView txtLabel = getView().findViewById(labelId);
            if (txtLabel != null) {
                // ... visibility logic ...
                txtLabel.setTextColor(currentVal > 0 ? colorWin : colorLoss);
            }
        }
    }
    @Override public void showLoading() { }

    @Override
    public void showError(String message) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() ->
                    Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show());
        }
    }
}