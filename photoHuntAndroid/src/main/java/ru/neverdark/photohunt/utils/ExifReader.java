package ru.neverdark.photohunt.utils;

import android.content.Context;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;

import java.io.IOException;

import ru.neverdark.photohunt.rest.data.Exif;

/**
 * Класс для чтения EXIF-информации из фотографий
 */
public class ExifReader {
    private final Context mContext;
    private final String mFileName;

    public ExifReader(Context context, Uri uri) {
        mContext = context;
        mFileName = uriToFilename(uri);
    }

    public ExifReader(Context context, String fileName) {
        mContext = context;
        mFileName = fileName;
    }

    private String uriToFilename(Uri uri) {
        String path = null;

        if (Build.VERSION.SDK_INT < 11) {
            path = RealPathUtil.getRealPathFromURI_BelowAPI11(mContext, uri);
        } else if (Build.VERSION.SDK_INT < 19) {
            path = RealPathUtil.getRealPathFromURI_API11to18(mContext, uri);
        } else {
            path = RealPathUtil.getRealPathFromURI_API19(mContext, uri);
        }

        Log.variable("path", path);
        return path;
    }

    public Exif getMetadata() {
        Exif exif = null;
        try {
            ExifInterface exifInterface = new ExifInterface(mFileName);
            exif = new Exif();
            if (Build.VERSION.SDK_INT >= 11) {
                exif.aperture = exifInterface.getAttribute(ExifInterface.TAG_APERTURE);
                exif.exposure_time = exifInterface.getAttribute(ExifInterface.TAG_EXPOSURE_TIME);
                exif.iso = exifInterface.getAttribute(ExifInterface.TAG_ISO);
            }

            if (Build.VERSION.SDK_INT >= 8) {
                double focal_length = exifInterface.getAttributeDouble(ExifInterface.TAG_FOCAL_LENGTH, 0);
                if (focal_length > 0) {
                    exif.focal_length = String.valueOf(focal_length);
                }
            }

            exif.datetime = exifInterface.getAttribute(ExifInterface.TAG_DATETIME);
            exif.make = exifInterface.getAttribute(ExifInterface.TAG_MAKE);
            exif.model = exifInterface.getAttribute(ExifInterface.TAG_MODEL);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return exif;
    }
}
