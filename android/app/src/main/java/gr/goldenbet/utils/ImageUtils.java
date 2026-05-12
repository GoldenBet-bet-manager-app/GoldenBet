package gr.goldenbet.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;

public class ImageUtils {


    /**
    *  Δέχεται String και επιστρέφει Bitmap
    */
    public static Bitmap decodeBase64ToBitmap(String base64Str) {

        if (base64Str == null || base64Str.trim().isEmpty()) {
            return null;
        }

        try {
            //Μετατροπή κειμένου σε πινακα bytes
            byte[] decodedBytes = Base64.decode(base64Str, Base64.DEFAULT);

            //Μετατροπή bytes σε Bitmap
            return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);

        } catch (IllegalArgumentException e) {
            Log.e("ImageUtils", "Αποτυχία αποκωδικοποίησης της εικόνας Base64", e);
            return null;
        }
    }
}
