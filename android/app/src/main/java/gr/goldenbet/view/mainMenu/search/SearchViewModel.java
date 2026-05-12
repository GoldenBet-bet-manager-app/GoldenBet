package gr.goldenbet.view.mainMenu.search;

import android.util.Log;

import androidx.lifecycle.ViewModel;

public class SearchViewModel extends ViewModel {

    private SearchPresenter presenter;

    public SearchViewModel(){
        presenter = new SearchPresenter();
    }

    public SearchPresenter getPresenter(){ return presenter; }

    @Override
    protected void onCleared(){
        super.onCleared();
        Log.d("SearchVM", "on Cleared");

    }
}
