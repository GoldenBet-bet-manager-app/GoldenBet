package gr.goldenbet.view.mainMenu.home.stats;

import android.os.Handler;
import android.os.Looper;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import gr.goldenbet.domain.MasterMessage;
import gr.goldenbet.domain.MasterMessageType;
import gr.goldenbet.domain.PlayerStats;

public class StatsPresenter {
    private StatsView view;
    private String masterIp = "10.0.2.2"; // IP για Android Emulator
    private int masterPort = 1234;

    public StatsPresenter(StatsView view) {
        this.view = view;
    }

    public void loadPlayerStats(String playerName) {
        view.showLoading();

        new Thread(() -> {
            try (Socket socket = new Socket(masterIp, masterPort);
                 ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                 ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

//                MasterMessage request = new MasterMessage(MasterMessageType.PLAYER_STATS, playerName);
//                out.writeObject(request);
//                out.flush();

                // Λήψη αποτελέσματος (Το socket παραμένει ανοιχτό [cite: 153])
                PlayerStats stats = (PlayerStats) in.readObject();

                // Ενημέρωση UI στο Main Thread
                new Handler(Looper.getMainLooper()).post(() -> view.showStats(stats));

            } catch (Exception e) {
                new Handler(Looper.getMainLooper()).post(() -> view.showError("Σφάλμα σύνδεσης με Master"));
            }
        }).start();
    }
}