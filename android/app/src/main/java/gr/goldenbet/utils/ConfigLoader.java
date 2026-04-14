package gr.goldenbet.utils;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class ConfigLoader {

    private static Map<String, String> serverConfigs = new HashMap<>();

    public static synchronized void loadConfig(String fileName) {

        serverConfigs.clear();

        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            String line;

            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.startsWith("#") || line.isEmpty()) continue;

                String[] parts = line.split(":");
                // parts[0] = ROLE
                // parts[1] = IP
                // parts[2] = PORT

                String role = parts[0].toUpperCase();
                String ip = parts[1];
                int port = Integer.parseInt(parts[2]);


                // Αποθήκευση ως "IP:PORT" για να τα χωρίσουμε μετά
                serverConfigs.put(role, ip + ":" + port);

            }
        } catch (IOException e) {
            System.err.println("Critical Error: Could not read config file!");
            System.exit(1);
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

