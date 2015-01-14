package ru.neverdark.photohunt.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.widget.Toast;

public class Common {
    public static boolean isValidEmail(String target) {
        if (target == null || target.length() == 0) {
            return false;
        } else {
            return android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
        }
    }
    
    public static void showMessage(Context context, int messageId) {
        Toast.makeText(context, messageId, Toast.LENGTH_LONG).show();
    }
    
    public static void showMessage(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }

    public static String getUniqueImageFilename() {
        return java.util.UUID.randomUUID().toString().concat(".jpg");
    }
    
    public static Bitmap resizeBitmap(Bitmap source, int targetWidth) {
        Log.variable("width", String.valueOf(source.getWidth()));
        Log.variable("height", String.valueOf(source.getHeight()));
        Log.variable("targetWidth", String.valueOf(targetWidth));
        double aspectRatio = (double) source.getHeight() / (double) source.getWidth();
        int targetHeight = (int) (targetWidth * aspectRatio);
        Bitmap result = Bitmap.createScaledBitmap(source, targetWidth, targetHeight, false);
        if (result != source) {
            // Same bitmap is returned if sizes are the same
            source.recycle();
        }
        
        return result;
    }
}
