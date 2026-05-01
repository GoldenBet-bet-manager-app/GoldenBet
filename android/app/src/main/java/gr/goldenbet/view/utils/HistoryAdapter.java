package gr.goldenbet.view.utils;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import gr.goldenbet.R;
import gr.goldenbet.view.mainMenu.home.history.HistoryEntry;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {
    private List<HistoryEntry> items;

    public HistoryAdapter(List<HistoryEntry> items) { this.items = items; }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        HistoryEntry item = items.get(position);
        holder.txtTitle.setText(item.getTitle());
        holder.txtDate.setText(item.getDateTime());

        switch (item.getType()) {
            case WIN:
                holder.imgIcon.setImageResource(R.drawable.ic_trophy); // Βεβαιώσου ότι υπάρχει
                holder.txtAmount.setText("+ " + item.getAmount() + " FUN");
                holder.txtAmount.setTextColor(Color.parseColor("#4CAF50")); // Πράσινο
                break;
            case BET:
                holder.imgIcon.setImageResource(R.drawable.ic_poker_chip);
                holder.txtAmount.setText("- " + item.getAmount() + " FUN");
                holder.txtAmount.setTextColor(Color.parseColor("#D4AF37")); // Χρυσό
                break;
            case DEPOSIT:
                holder.imgIcon.setImageResource(R.drawable.ic_money_bag);
                holder.txtAmount.setText("+ " + item.getAmount() + " FUN");
                holder.txtAmount.setTextColor(Color.WHITE);
                break;
        }
    }

    @Override
    public int getItemCount() { return items.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtTitle, txtDate, txtAmount;
        ImageView imgIcon;
        public ViewHolder(View itemView) {
            super(itemView);
            txtTitle = itemView.findViewById(R.id.txtHistoryTitle);
            txtDate = itemView.findViewById(R.id.txtHistoryDate);
            txtAmount = itemView.findViewById(R.id.txtHistoryAmount);
            imgIcon = itemView.findViewById(R.id.imgHistoryType);
        }
    }
}