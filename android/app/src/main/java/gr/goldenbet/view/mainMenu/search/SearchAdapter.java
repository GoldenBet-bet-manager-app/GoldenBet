package gr.goldenbet.view.mainMenu.search;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;

import gr.goldenbet.databinding.ItemGameBinding;
import gr.goldenbet.domain.Game;
import gr.goldenbet.utils.ImageUtils;

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.ViewHolder> {


    private List<Game> gameList;
    private SearchListener listener;

    public SearchAdapter(SearchListener listener){

        gameList = new ArrayList<>();
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
        return new ViewHolder(ItemGameBinding.inflate(LayoutInflater.
                from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position){
        Game game = gameList.get(position);

        holder.binding.txtGameName.setText(game.getGameName());
        holder.binding.txtProviderName.setText(game.getProviderName());

        holder.binding.txtRating.setText(String.valueOf(game.getStars()));

        Bitmap gameImage = ImageUtils.decodeBase64ToBitmap(game.getBase64Image());
        if (gameImage != null) {
            holder.binding.imgGameLogo.setImageBitmap(gameImage);
        } else {
            holder.binding.imgGameLogo.setImageDrawable(null);
        }

        holder.binding.getRoot().setOnClickListener(v -> {
            if (listener != null) {
                listener.onGameClicked(game);
            }
        });
    }

    @Override
    public int getItemCount(){ return gameList.size(); }

    public void updateList(List<Game> newGameList) {
        this.gameList.clear();
        if (newGameList != null) {
            this.gameList.addAll(newGameList);
        }
        notifyDataSetChanged();
    }



    public static class ViewHolder extends RecyclerView.ViewHolder{

        ItemGameBinding binding;
        public ViewHolder(ItemGameBinding binding){
            super(binding.getRoot());
            this.binding = binding;
        }
    }


    public interface SearchListener{
        void onGameClicked(Game game);
    }


}
