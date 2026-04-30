package gr.goldenbet.view.register;

import gr.goldenbet.domain.Player;

public interface RegisterView {
    void showConfirmDialog(Player player);
    void onRegisterSuccess();
    void onRegisterError(String message);
}