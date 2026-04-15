package gr.goldenbet.view.settings.LanguageList;

import java.util.List;

public interface LanguageListView {
    void showLanguages(List<LanguageModel> languages);
    void applyGlobalLanguage(String langCode);
    void navigateBack();
}