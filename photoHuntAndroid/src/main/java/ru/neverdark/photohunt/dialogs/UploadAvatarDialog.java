package ru.neverdark.photohunt.dialogs;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.net.Uri;
import android.view.View;
import android.widget.ImageView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import ru.neverdark.abs.CancelClickListener;
import ru.neverdark.abs.UfoDialogFragment;
import ru.neverdark.photohunt.R;
import ru.neverdark.photohunt.utils.Common;
import ru.neverdark.photohunt.utils.Log;

public class UploadAvatarDialog extends UfoDialogFragment{
    public static final String DIALOG_ID = "uploadAvatarDialog";
    private Uri mSelectedImageUri;
    private String mPath;
    private ImageView mAvatarImage;
    private Uri mOutputFileUri;

    @Override
    public void bindObjects() {
        setDialogView(View.inflate(getContext(), R.layout.upload_avatar_dialog, null));
        mAvatarImage = (ImageView) getDialogView().findViewById(R.id.upload_avatar_image);
    }

    @Override
    protected void createDialog() {
        super.createDialog();
        getAlertDialog().setTitle(R.string.is_load_avatar);
        loadImage();
    }

    private void loadImage() {
        try {
            Bitmap bitmap = Common.resizeBitmap(Common.decodeSampledBitmapFromUri(getContext(), mSelectedImageUri, 512), 512, 512);

            int width = (int) getContext().getResources().getDimension(R.dimen.avatar_size);
            Log.variable("width", String.valueOf(width));

            Bitmap imgBitmap = Common.resizeBitmap(Common.decodeSampledBitmapFromUri(getContext(), mSelectedImageUri, width), width, width);
            mAvatarImage.setImageBitmap(imgBitmap);

            File file = new File(mPath);
            mOutputFileUri = Uri.fromFile(file);
            OutputStream outStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 60, outStream);

            outStream.flush();
            outStream.close();

        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public void setListeners() {
        getAlertDialog().setPositiveButton(R.string.dialog_ok, new PositiveClickListener());
        getAlertDialog().setNegativeButton(R.string.dialog_cancel, new CancelClickListener());
    }

    public static UploadAvatarDialog getInstance(Context context) {
        UploadAvatarDialog dialog = new UploadAvatarDialog();
        dialog.setContext(context);
        return dialog;
    }

    public void setData(Uri selectedImageUri, String path) {
        mSelectedImageUri = selectedImageUri;
        mPath = path;
    }

    public interface OnPositiveClickListener {
        public void onPositiveClickHandler(Uri outputFileUri);
    }

    private class PositiveClickListener implements DialogInterface.OnClickListener {

        @Override
        public void onClick(DialogInterface dialog, int which) {
            OnPositiveClickListener callback = (OnPositiveClickListener) getCallback();
            if (callback != null) {
                callback.onPositiveClickHandler(mOutputFileUri);
            }
        }
    }
}
