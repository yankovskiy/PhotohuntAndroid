package ru.neverdark.photohunt.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;

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

    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        Log.variable("sampleSize", String.valueOf(inSampleSize));
        return inSampleSize;
    }

    public static Bitmap decodeSampledBitmapFromUri(Context context, Uri uri,
                                                         int targetDimen) {
        try {
            InputStream stream = context.getContentResolver().openInputStream(uri);

            // First decode with inJustDecodeBounds=true to check dimensions
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(stream, null, options);

            // Calculate inSampleSize
            options.inSampleSize = calculateInSampleSize(options, targetDimen, targetDimen);
            // Decode bitmap with inSampleSize set
            options.inJustDecodeBounds = false;
            stream.close();

            stream  = context.getContentResolver().openInputStream(uri);
            Bitmap bitmap =  BitmapFactory.decodeStream(stream, null, options);
            stream.close();
            return  bitmap;
        } catch (IOException e) {
            Log.message(e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}
