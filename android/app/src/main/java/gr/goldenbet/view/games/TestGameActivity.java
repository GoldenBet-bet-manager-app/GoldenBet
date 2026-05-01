package gr.goldenbet.view.games;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import gr.goldenbet.R;
import gr.goldenbet.view.games.wheelgame.WheelGameFragment;

public class TestGameActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main_menu);

        if (savedInstanceState == null) {
            WheelGameFragment fragment = WheelGameFragment.newInstance("P123", "Dragon's Lair");
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
        }
    }
}