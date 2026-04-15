package gr.goldenbet.view.settings.LanguageList;

import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.List;

public class LanguageListPresenter {
    private final LanguageListView view;
    private final SharedPreferences prefs;
    private final List<LanguageModel> allLanguages;

    public LanguageListPresenter(LanguageListView view, SharedPreferences prefs) {
        this.view = view;
        this.prefs = prefs;

        allLanguages = new ArrayList<>();
        allLanguages.add(new LanguageModel("English", "en"));
        allLanguages.add(new LanguageModel("Ελληνικά", "el"));
    }

    public void initialize() {
        view.showLanguages(allLanguages);
    }

    public void onSearchQueryChanged(String query) {
        if (query.isEmpty()) {
            view.showLanguages(allLanguages);
            return;
        }
        List<LanguageModel> filtered = new ArrayList<>();
        for (LanguageModel lang : allLanguages) {
            if (lang.getName().toLowerCase().contains(query.toLowerCase())) {
                filtered.add(lang);
            }
        }
        view.showLanguages(filtered);
    }

    public void onLanguageSelected(LanguageModel language) {
        prefs.edit().putString("lang", language.getCode()).apply();
        view.applyGlobalLanguage(language.getCode());
        view.navigateBack();
    }
}