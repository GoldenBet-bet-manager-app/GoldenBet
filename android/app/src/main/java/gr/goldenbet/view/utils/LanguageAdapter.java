package gr.goldenbet.view.utils;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import gr.goldenbet.R;
import gr.goldenbet.view.settings.LanguageList.LanguageModel;

public class LanguageAdapter extends RecyclerView.Adapter<LanguageAdapter.LanguageViewHolder> {

    private List<LanguageModel> languages;
    private final OnLanguageClickListener listener;

    public interface OnLanguageClickListener {
        void onLanguageClick(LanguageModel language);
    }

    public LanguageAdapter(List<LanguageModel> languages, OnLanguageClickListener listener) {
        this.languages = languages;
        this.listener = listener;
    }

    public void updateData(List<LanguageModel> newLanguages) {
        this.languages = newLanguages;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public LanguageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_language, parent, false);
        return new LanguageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LanguageViewHolder holder, int position) {
        LanguageModel language = languages.get(position);
        holder.txtName.setText(language.getName());
        holder.itemView.setOnClickListener(v -> listener.onLanguageClick(language));
    }

    @Override
    public int getItemCount() {
        return languages.size();
    }

    static class LanguageViewHolder extends RecyclerView.ViewHolder {
        TextView txtName;
        LanguageViewHolder(@NonNull View itemView) {
            super(itemView);
            txtName = itemView.findViewById(R.id.txtLanguageListItemName);
        }
    }
}