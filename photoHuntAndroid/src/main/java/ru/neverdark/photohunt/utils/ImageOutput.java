package ru.neverdark.photohunt.utils;

import android.content.Context;
import android.net.Uri;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import retrofit.mime.TypedOutput;

/**
 * Класс для потоковой передачи изображения на сервер
 */
public class ImageOutput implements TypedOutput {
    private InputStream mStream;

    public ImageOutput(Context context, Uri uri) throws IOException {
        mStream = context.getContentResolver().openInputStream(uri);
    }

    @Override
    public String fileName() {
        return "1.jpg";
    }

    @Override
    public long length() {
        return -1;
    }

    @Override
    public String mimeType() {
        return "image/jpeg";
    }

    @Override
    public void writeTo(OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int bytesRead;

        while((bytesRead = mStream.read(buffer)) != -1) {
            out.write(buffer, 0, bytesRead);
        }
    }
}
