package gr.goldenbet.view.mainMenu.search;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import gr.goldenbet.R;
import gr.goldenbet.view.games.fruitgame.FruitGameFragment;
import gr.goldenbet.view.games.poker.PokerFragment;
import gr.goldenbet.view.games.wheelgame.WheelGameFragment;

public class SearchFragment extends Fragment implements SearchView {

    public SearchFragment() {

    }

    public static SearchFragment newInstance() {
        return new SearchFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (savedInstanceState == null) {
            FruitGameFragment fragment = FruitGameFragment.newInstance( "Fruit Fiesta");
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
        }
    }
}