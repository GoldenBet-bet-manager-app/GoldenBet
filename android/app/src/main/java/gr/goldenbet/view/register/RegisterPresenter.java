package gr.goldenbet.view.register;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.json.JSONObject;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import gr.goldenbet.R;
import gr.goldenbet.domain.MasterMessage;
import gr.goldenbet.domain.MasterMessageType; // Υποθέτουμε ότι υπάρχει το REGIST
import gr.goldenbet.domain.Player;

import gr.goldenbet.utils.Address;
import gr.goldenbet.view.utils.ConfigLoader;

public class RegisterPresenter {
    private RegisterView view;
    private final String MASTER_IP;
    private final int MASTER_PORT;

    public RegisterPresenter(RegisterView view) {
        this.view = view;

        // Set master configurations
        MASTER_IP = ((RegisterActivity)view).getResources().getString(R.string.master_ip);
        MASTER_PORT = ((RegisterActivity)view).getResources().getInteger(R.integer.master_port);


    }

    public void onRegisterClick( String username, String password, String name, String surname
    ,String email, String phone, String street, String streetNumber, String city, String zip) {
        if (username.isEmpty() || password.isEmpty() || name.isEmpty() || surname.isEmpty() ||
        email.isEmpty() || phone.isEmpty()) {
            view.onRegisterError(((RegisterActivity)view).getString(R.string.common_necessary_fields_msg));
            return;
        }


        Player newPlayer = new Player(name, surname, username, password);

        view.showConfirmDialog(newPlayer);
    }

    public void sendRegistrationToMaster(Player player) {

        new Thread(() -> {

            try (Socket socket = new Socket(MASTER_IP, MASTER_PORT);
                 ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                 ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

                Gson gson = new Gson();

                MasterMessage message = new MasterMessage(MasterMessageType.REGIST, player);
                String jsonToSend = gson.toJson(message);

                out.writeObject(jsonToSend);
                out.flush();

                String masterReply = (String) in.readObject();
                JsonObject response = gson.fromJson(masterReply, JsonObject.class);

                String status = response.get("status").getAsString();
                String masterReplyMessage = response.get("message").getAsString();

                if ("SUCCESS".equals(status)) {
                    view.onRegisterSuccess();
                } else {
                    view.onRegisterError(masterReplyMessage);
                }

            } catch (Exception e) {

                // runOnUiThread called by method to update UI
                view.onRegisterError("Network Error: " + e.getMessage());
            }
        }).start();
    }


}