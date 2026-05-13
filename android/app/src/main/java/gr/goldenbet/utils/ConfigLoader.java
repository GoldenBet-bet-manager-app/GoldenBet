package gr.goldenbet.utils;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class ConfigLoader {


    private static final Map<String, String> serverConfigs = new HashMap<>();
    private static boolean isLoaded = false;


    public static synchronized void loadConfig(Context context, String fileName) {

        if (isLoaded) return;

        serverConfigs.clear();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(context.getAssets().open(fileName)))) {
            String line;

            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.startsWith("#") || line.isEmpty()) continue;

                String[] parts = line.split(":");
                if (parts.length >= 3) {
                    String role = parts[0].toUpperCase();
                    String ip = parts[1];
                    int port = Integer.parseInt(parts[2]);

                    //Αποθήκευση ως "IP:PORT"
                    serverConfigs.put(role, ip + ":" + port);
                }
            }
            isLoaded = true;
            Log.d("ConfigLoader", "System configuration loaded successfully.");

        }catch(IOException e){
            Log.e("ConfigLoader", "Critical Error: Could not read config file from assets!", e);
        }
    }

    public static String getIp(String role) {
        String config = serverConfigs.get(role.toUpperCase());
        return (config != null) ? config.split(":")[0] : null;
    }

    public static int getPort(String role) {
        String config = serverConfigs.get(role.toUpperCase());
        return (config != null) ? Integer.parseInt(config.split(":")[1]) : 0;
    }
}