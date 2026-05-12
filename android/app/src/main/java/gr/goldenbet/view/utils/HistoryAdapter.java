package gr.goldenbet.view.utils;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import gr.goldenbet.R;
import gr.goldenbet.view.mainMenu.home.history.HistoryEntry;
import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    private List<HistoryEntry> items;

    public HistoryAdapter(List<HistoryEntry> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_history, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        HistoryEntry item = items.get(position);

        holder.txtTitle.setText(item.getTitle());
        holder.txtDate.setText(item.getDateTime());

        String sign = (item.getType() == HistoryEntry.Type.BET) ? "" : "+";
        holder.txtAmount.setText(sign + item.getAmount().toPlainString() + " FUN");

        // UI Styling based on Type
        if (item.getType() == HistoryEntry.Type.BET) {
            holder.imgIcon.setImageResource(R.drawable.ic_poker_chip);
            holder.txtAmount.setTextColor(Color.parseColor("#FF5252")); // Red
        } else if (item.getType() == HistoryEntry.Type.WIN) {
            holder.imgIcon.setImageResource(R.drawable.ic_trophy);
            holder.txtAmount.setTextColor(Color.parseColor("#D4AF37")); // Gold
        } else {
            holder.imgIcon.setImageResource(R.drawable.ic_money_bag);
            holder.txtAmount.setTextColor(Color.parseColor("#D4AF37")); // Gold
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtTitle, txtDate, txtAmount;
        ImageView imgIcon;

        ViewHolder(View v) {
            super(v);
            txtTitle = v.findViewById(R.id.txtHistoryTitle);
            txtDate = v.findViewById(R.id.txtHistoryDate);
            txtAmount = v.findViewById(R.id.txtHistoryAmount);
            imgIcon = v.findViewById(R.id.imgHistoryType);
        }
    }
}