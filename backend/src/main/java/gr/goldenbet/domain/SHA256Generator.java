package gr.goldenbet.domain;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public class SHA256Generator {

    public static String computeSHA256(String number) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");

            byte[] hashBytes  = md.digest(number.getBytes( StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();

            for (byte b : hashBytes ) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();

        }catch(Exception e){
            throw new RuntimeException("SHA-256 algorithm not found!", e);
        }
    }

}