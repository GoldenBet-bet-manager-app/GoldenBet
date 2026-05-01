package gr.goldenbet.view.register;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

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

                // Main JSON creation (MasterMessage)
                MasterMessage message = new MasterMessage(MasterMessageType.REGIST, player);
                JSONObject masterJson = new JSONObject();
                masterJson.put("type", message.getType() != null ? message.getType().name() : "null");
                masterJson.put("mapId", message.getMapId());

                // if paylod is type of Player, it is converted into nested JSON
                if (message.getPayload() instanceof Player) {
                    JSONObject playerJson = new JSONObject();

                    playerJson.put("name", player.getName());
                    playerJson.put("surname", player.getSurname());
                    playerJson.put("username", player.getUsername());
                    playerJson.put("password", player.getPassword());
                    playerJson.put("email", player.getEmail());
                    playerJson.put("phone", player.getPhone());
                    playerJson.put("playerId", player.getPlayerId());
                    playerJson.put("isLoggedIn", player.isLoggedIn());

                    // Conversion of object Address of Player
                    JSONObject addressJson = new JSONObject();
                    Address addr = player.getAddress();
                    if (addr != null) {
                        addressJson.put("street", addr.getStreet());
                        addressJson.put("streetNumber", addr.getStreetNumber());
                        addressJson.put("city", addr.getCity());
                        addressJson.put("zipCode", addr.getZipCode());
                    }
                    playerJson.put("address", addressJson);

                    // Place player into MasterMessage JSON
                    masterJson.put("payload", playerJson);
                }

                // Send JSON as String
                out.writeUTF(masterJson.toString());
                out.flush();


                String masterReply = in.readUTF();
                JSONObject response = new JSONObject(masterReply);

                String status = response.getString("status");
                String masterReplyMessage = response.getString("message");


                // Successful registration
                // runOnUiThread called by methods to update UI
                if ("SUCCESS".equals(status)) {
                    // Successful Registration Attempt
                    view.onRegisterSuccess();
                } else {
                    // Failed Registration Attempt
                    view.onRegisterError(masterReplyMessage);
                }

            } catch (Exception e) {

                // runOnUiThread called by method to update UI
                view.onRegisterError("Network Error: " + e.getMessage());
            }
        }).start();



    }
}