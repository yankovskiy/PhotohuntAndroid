package ru.neverdark.photohunt.utils;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import ru.neverdark.abs.UfoFragment;
import ru.neverdark.photohunt.R;

public class Common {

    public static final int PICTURE_REQUEST_CODE = 1;

    public static Uri handleChoosenImage(Intent data, Uri fileUri) {
        final boolean isCamera;
        if (data == null) {
            isCamera = true;
        } else {
            final String action = data.getAction();
            if (action == null) {
                isCamera = false;
            } else {
                isCamera = action.equals(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
            }
        }

        Uri selectedImageUri;
        if (isCamera) {
            selectedImageUri = fileUri;
        } else {
            selectedImageUri = data == null ? null : data.getData();
        }

        return selectedImageUri;
    }

    public static Uri chooseImage(Context context, UfoFragment fragment) {
        final File root = new File(Environment.getExternalStorageDirectory() + File.separator + "Photohunt" + File.separator);
        root.mkdirs();
        final String fname = Common.getUniqueImageFilename();
        final File sdImageMainDirectory = new File(root, fname);
        Uri outputFileUri = Uri.fromFile(sdImageMainDirectory);
        // Camera.
        final List<Intent> cameraIntents = new ArrayList<Intent>();
        final Intent captureIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        final PackageManager packageManager = context.getPackageManager();
        final List<ResolveInfo> listCam = packageManager.queryIntentActivities(captureIntent, 0);
        for (ResolveInfo res : listCam) {
            final String packageName = res.activityInfo.packageName;
            final Intent intent = new Intent(captureIntent);
            intent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
            intent.setPackage(packageName);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
            cameraIntents.add(intent);
        }

        // Filesystem.
        final Intent galleryIntent = new Intent();
        galleryIntent.setType("image/*");
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);

        // Chooser of filesystem options.
        final Intent chooserIntent = Intent.createChooser(galleryIntent, context.getString(R.string.choose_source));

        // Add the camera options.
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, cameraIntents.toArray(new Parcelable[]{}));

        fragment.startActivityForResult(chooserIntent, PICTURE_REQUEST_CODE);

        return outputFileUri;
    }

    /**
     * Представляет дату в читабельном виде (03 мар 2015)
     * @param context контекст приложения
     * @param date дата
     * @return строка содержащая дату
     */
    public static String parseDate(Context context, String date) {
        String dateOnly = date.split(" ")[0];
        String year = dateOnly.split("-")[0];
        int monthIndex = Integer.valueOf(dateOnly.split("-")[1]);
        String day = dateOnly.split("-")[2];

        return String.format("%s %s %s", day, context.getResources().getStringArray(R.array.months_acc)[monthIndex - 1], year);
    }

    public static Bitmap getRoundedCornerBitmap(Context context, Bitmap bitmap) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap
                .getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff000000;//context.getResources().getColor(R.color.grey505050);

        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);
        final float roundPx = context.getResources().getDimension(R.dimen.avatar_border);

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        return output;
    }

    public static void openFragment(Fragment baseFragment, Fragment openFragment, boolean isStack) {
        FragmentTransaction transaction = baseFragment.getFragmentManager().beginTransaction();
        transaction.replace(R.id.main_container, openFragment);
        if (isStack) {
            transaction.addToBackStack(null);
        }
        transaction.commit();
    }

    public static void openFragment(Fragment baseFragment, Fragment openFragment, String tag) {
        FragmentTransaction transaction = baseFragment.getFragmentManager().beginTransaction();
        transaction.replace(R.id.main_container, openFragment);
        transaction.addToBackStack(tag);
        transaction.commit();
    }

    /**
     * Склоняет существительное по числу, например: 1 помидор, 3 помидора, 16 помидоров
     * @param number число по которому осуществить склонение
     * @param words массив из трех слов (помидор, помидора, помидоров)
     * @return существительное в нужном падеже, или null в случае ошибки
     */
    public static String declensionByNumber(int number, String[] words) {
        int wordIndex = 2;

        if (words.length != 3) {
            return null;
        }

        int num = Math.abs(number) % 100;
        int numX = num % 10;

        if (num > 10 && num < 20) {
            wordIndex = 2;
        } else if (numX > 1 && numX < 5) {
            wordIndex = 1;
        } else if (numX == 1) {
            wordIndex = 0;
        }

        return words[wordIndex];
    }

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

        return resizeBitmap(source, targetWidth, targetHeight);
    }

    public static Bitmap resizeBitmap(Bitmap source, int targetWidth, int targetHeight) {
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
