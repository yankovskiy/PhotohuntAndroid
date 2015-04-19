package ru.neverdark.photohunt.utils;

import android.media.ExifInterface;
import android.os.Build;

import java.io.IOException;

import ru.neverdark.photohunt.rest.data.Exif;

/**
 * Класс для чтения EXIF-информации из фотографий
 */
public class ExifReader {
    private final String mFileName;

    public ExifReader(String fileName) {
        mFileName = fileName;
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
