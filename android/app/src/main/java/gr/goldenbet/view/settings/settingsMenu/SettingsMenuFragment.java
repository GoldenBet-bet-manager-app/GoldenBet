package gr.goldenbet.view.settings.settingsMenu;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;
import androidx.fragment.app.Fragment;

import gr.goldenbet.R;
import gr.goldenbet.databinding.FragmentSettingsMenuBinding;
import gr.goldenbet.view.settings.LanguageList.LanguageListFragment;

public class SettingsMenuFragment extends Fragment implements SettingsMenuView {

    private FragmentSettingsMenuBinding binding;
    private SettingsMenuPresenter presenter;

    public SettingsMenuFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSettingsMenuBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        presenter = new SettingsMenuPresenter(this,
                requireActivity().getSharedPreferences("Settings", Context.MODE_PRIVATE));

        // Listeners
        binding.SettingsswitchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            presenter.onThemeToggled(isChecked);
        });

        binding.SettingsCardLanguage.setOnClickListener(v -> {
            presenter.onLanguageCardClicked();
        });

        // initialize fragment data
        presenter.start();
    }

    @Override
    public void onResume() {
        super.onResume();
        presenter.start();
    }

    @Override
    public void showTheme(boolean isDark) {
        if (binding != null) {
            binding.SettingsswitchDarkMode.setChecked(isDark);
        }
    }

    @Override
    public void showLanguageName(int resId) {
        if (binding != null) {
            binding.txtCurrentLanguage.setText(getString(resId));
        }
    }

    @Override
    public void applyThemeChange(boolean isDark) {
        AppCompatDelegate.setDefaultNightMode(isDark ?
                AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
    }

    @Override
    public void applyLanguageChange(String langCode) {
        LocaleListCompat appLocales = LocaleListCompat.forLanguageTags(langCode);
        AppCompatDelegate.setApplicationLocales(appLocales);
    }

    @Override
    public void navigateToLanguageSelection() {
        /*getParentFragmentManager().beginTransaction()
                // animations
                .setCustomAnimations(
                        R.anim.slide_in_right,  // enter
                        R.anim.slide_out_left,  // exit
                        R.anim.slide_in_left,   // popEnter
                        R.anim.slide_out_right  // popExit
                )
                .replace(R.id.settings_fragment_container, new LanguageListFragment())
                .addToBackStack(null)
                .commit();*/
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}