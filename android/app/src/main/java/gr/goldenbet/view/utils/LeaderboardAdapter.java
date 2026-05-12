package gr.goldenbet.view.utils;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import gr.goldenbet.R;

public class LeaderboardAdapter extends RecyclerView.Adapter<LeaderboardAdapter.LeaderboardViewHolder> {

    private List<Map.Entry<String, BigDecimal>> items = new ArrayList<>();
    private final String currentUsername;

    public LeaderboardAdapter(String currentUsername) {
        this.currentUsername = currentUsername;
    }

    public void setItems(List<Map.Entry<String, BigDecimal>> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public LeaderboardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate το layout του item (item_leaderboard.xml)
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_leaderboard, parent, false);
        return new LeaderboardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LeaderboardViewHolder holder, int position) {
        Map.Entry<String, BigDecimal> entry = items.get(position);
        String username = entry.getKey();
        BigDecimal profit = entry.getValue();


        // Initialize values at views
        holder.txtUsername.setText(username);
        holder.txtProfit.setText(String.format("%.2f FUN", profit));

        // DEFAULT AVATAR SETTINGS
        holder.imgAvatar.setImageResource(R.drawable.ic_person);
        holder.imgAvatar.setBackgroundResource(R.drawable.shape_circle_avatar);
        holder.imgAvatar.setPadding(10, 10, 10, 10); // Δίνει αέρα στο εικονίδιο μέσα στον κύκλο

        // It paints person icon depending on theme (Dark/Light)
        holder.imgAvatar.setImageTintList(android.content.res.ColorStateList.valueOf(
                holder.itemView.getContext().getColor(R.color.text_dark) // ή ?attr/colorOnSurface
        ));

        // rank start from #4 (beacause 1-3 are on Podium)
        holder.txtRank.setText("#" + (position + 4));

        // Λογική Highlighting για τον τρέχοντα παίκτη
        if (username.equals(currentUsername)) {
            holder.card.setStrokeWidth(6); // Εμφάνιση χρυσού περιγράμματος
            holder.txtYouTag.setVisibility(View.VISIBLE);
            holder.card.setCardElevation(12f);
        } else {
            holder.card.setStrokeWidth(0);
            holder.txtYouTag.setVisibility(View.GONE);
            holder.card.setCardElevation(2f);
        }
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }


    static class LeaderboardViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView card;
        TextView txtRank, txtUsername, txtYouTag, txtProfit;
        ImageView imgAvatar;

        LeaderboardViewHolder(@NonNull View itemView) {
            super(itemView);
            card = itemView.findViewById(R.id.cardLeaderboardRow);
            txtRank = itemView.findViewById(R.id.txtRowRank);
            txtUsername = itemView.findViewById(R.id.txtRowUsername);
            txtYouTag = itemView.findViewById(R.id.txtYouTag);
            txtProfit = itemView.findViewById(R.id.txtRowProfit);
            imgAvatar = itemView.findViewById(R.id.imgRowAvatar);
        }
    }
}