package gr.goldenbet.view.games;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import gr.goldenbet.R;




import java.math.BigDecimal;
import gr.goldenbet.domain.Player;
import gr.goldenbet.domain.PlayerApp;
import gr.goldenbet.view.games.fruitgame.FruitGameFragment;

public class TestGameActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main_menu);

        // 1. FAKE LOGIN ΓΙΑ ΤΟ ΤΕΣΤ




        if (savedInstanceState == null) {
            FruitGameFragment fragment = FruitGameFragment.newInstance("P123", "Gates of Olympus");
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
        }
    }
}