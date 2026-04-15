package gr.goldenbet.view.settings.LanguageList;


import android.content.Context;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.os.LocaleListCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import gr.goldenbet.R;
import gr.goldenbet.view.utils.LanguageAdapter;


public class LanguageListActivity extends AppCompatActivity implements LanguageListView {

    private LanguageListPresenter presenter;
    private LanguageAdapter adapter;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_language_list);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Toolbar toolbar = findViewById(R.id.toolbarLanguage);
        toolbar.setNavigationOnClickListener(v -> navigateBack());

        recyclerView = findViewById(R.id.rvLanguageList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        presenter = new LanguageListPresenter(this, getSharedPreferences("Settings", Context.MODE_PRIVATE));
        presenter.initialize();

        SearchView searchView = findViewById(R.id.searchLanguage);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) { return false; }
            @Override
            public boolean onQueryTextChange(String newText) {
                presenter.onSearchQueryChanged(newText);
                return true;
            }
        });
    }

    @Override
    public void showLanguages(List<LanguageModel> languages) {
        if (adapter == null) {
            adapter = new LanguageAdapter(languages, language -> presenter.onLanguageSelected(language));
            recyclerView.setAdapter(adapter);
        } else {
            adapter.updateData(languages);
        }
    }

    @Override
    public void applyGlobalLanguage(String langCode) {
        LocaleListCompat appLocales = LocaleListCompat.forLanguageTags(langCode);
        AppCompatDelegate.setApplicationLocales(appLocales);
    }

    @Override
    public void navigateBack() {
        finish();
    }
}