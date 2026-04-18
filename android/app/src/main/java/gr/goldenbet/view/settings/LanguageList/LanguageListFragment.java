package gr.goldenbet.view.settings.LanguageList;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SearchView;
import androidx.core.os.LocaleListCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.List;

import gr.goldenbet.databinding.FragmentLanguageListBinding;
import gr.goldenbet.view.utils.LanguageAdapter;

public class LanguageListFragment extends Fragment implements LanguageListView {

    private FragmentLanguageListBinding binding;
    private LanguageListPresenter presenter;
    private LanguageAdapter adapter;

    public LanguageListFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentLanguageListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Setup Toolbar
        binding.toolbarLanguage.setNavigationOnClickListener(v -> navigateBack());

        // Setup RecyclerView
        binding.rvLanguageList.setLayoutManager(new LinearLayoutManager(requireContext()));

        // Setup Presenter
        presenter = new LanguageListPresenter(this,
                requireActivity().getSharedPreferences("Settings", Context.MODE_PRIVATE));

        presenter.initialize();

        // Setup SearchView
        binding.searchLanguage.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
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
            binding.rvLanguageList.setAdapter(adapter);
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
        // Επιστροφή στο προηγούμενο Fragment (SettingsMenuFragment)
        if (getParentFragmentManager().getBackStackEntryCount() > 0) {
            getParentFragmentManager().popBackStack();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}