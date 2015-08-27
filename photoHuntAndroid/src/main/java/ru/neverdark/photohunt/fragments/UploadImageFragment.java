package ru.neverdark.photohunt.fragments;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.mime.TypedOutput;
import ru.neverdark.abs.UfoFragment;
import ru.neverdark.photohunt.MainActivity;
import ru.neverdark.photohunt.R;
import ru.neverdark.photohunt.rest.CallbackHandler;
import ru.neverdark.photohunt.rest.RestService;
import ru.neverdark.photohunt.rest.data.Contest;
import ru.neverdark.photohunt.rest.data.Exif;
import ru.neverdark.photohunt.utils.Common;
import ru.neverdark.photohunt.utils.ExifReader;
import ru.neverdark.photohunt.utils.Log;
import ru.neverdark.photohunt.utils.Settings;
import ru.neverdark.photohunt.utils.ToastException;

public class UploadImageFragment extends UfoFragment {

    private View mView;
    private Context mContext;
    private Uri mUri;
    private EditText mNewSubject;
    private Uri mOutputFileUri;
    private String mFileName;
    private Exif mExif;
    private Contest mContest;
    private EditText mDescription;
    private View mUploadHint;

    public static UploadImageFragment getInstance(Uri uri, Contest contest) {
        UploadImageFragment fragment = new UploadImageFragment();
        fragment.mUri = uri;
        fragment.mContest = contest;
        return fragment;
    }

    public static Bitmap rotateBitmap(Bitmap bitmap, int orientation) {

        Matrix matrix = new Matrix();
        switch (orientation) {
            case ExifInterface.ORIENTATION_NORMAL:
                return bitmap;
            case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                matrix.setScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                matrix.setRotate(180);
                break;
            case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                matrix.setRotate(180);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_TRANSPOSE:
                matrix.setRotate(90);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_90:
                matrix.setRotate(90);
                break;
            case ExifInterface.ORIENTATION_TRANSVERSE:
                matrix.setRotate(-90);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                matrix.setRotate(-90);
                break;
            default:
                return bitmap;
        }
        try {
            Bitmap bmRotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            bitmap.recycle();
            return bmRotated;
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void onDestroyView() {
        ((MainActivity) getActivity()).getActionBarLayout(false);
        super.onDestroyView();
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * android.support.v4.app.Fragment#onCreateView(android.view.LayoutInflater,
     * android.view.ViewGroup, android.os.Bundle)
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceSate) {
        mView = inflater.inflate(R.layout.upload_image_fragment, container, false);
        mContext = mView.getContext();
        setHasOptionsMenu(true);
        bindObjects();
        setListeners();
        updateActionBar(loadImage());
        setVisible(mContest.is_user_contest == 0);
        return mView;
    }

    private void updateActionBar(Bitmap image) {
        View view = ((MainActivity) getActivity()).getActionBarLayout(true);
        TextView title = (TextView) view.findViewById(R.id.custom_actionbar_title);
        TextView contestSubject = (TextView) view.findViewById(R.id.custom_actionbar_subtitle);
        ImageView imageThumb = (ImageView) view.findViewById(R.id.custom_actionbar_image);

        title.setText(R.string.image_upload);
        contestSubject.setText(mContest.subject);
        imageThumb.setImageBitmap(image);
    }

    private Bitmap loadImage() {
        Bitmap bmp = null;
        try {
            File file = new File(mFileName);
            if (!file.exists()) {
                byte[] buffer = new byte[1024];
                int bytesRead;

                InputStream in = mContext.getContentResolver().openInputStream(mUri);
                OutputStream out = new FileOutputStream(file);

                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }

                in.close();
                out.close();
            }

            mExif = new ExifReader(mFileName).getMetadata();

            Bitmap bitmap = Common.resizeBitmap(Common.decodeSampledBitmapFromUri(mContext, mUri, 1024), 1024);
            bitmap = rotateBitmap(bitmap, mExif.orientation);

            int pixels = (int) (mContext.getResources().getDisplayMetrics().density * 32);
            Log.variable("pixels", String.valueOf(pixels));
            int width = (int) mContext.getResources().getDimension(R.dimen.min_avatar_size);
            Log.variable("width", String.valueOf(width));

            bmp = Common.resizeBitmap(Common.decodeSampledBitmapFromUri(mContext, mUri, width), width);
            bmp = rotateBitmap(bmp, mExif.orientation);

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

        return bmp;
    }

    @Override
    public void bindObjects() {
        mUploadHint = mView.findViewById(R.id.upload_hint);
        mNewSubject = (EditText) mView.findViewById(R.id.upload_new_subject);
        mDescription = (EditText) mView.findViewById(R.id.upload_image_description);
    }

    @Override
    public void setListeners() {

    }

    private void setVisible(boolean isVisible) {
        if (isVisible) {
            mUploadHint.setVisibility(View.VISIBLE);
            mNewSubject.setVisibility(View.VISIBLE);
        } else {
            mUploadHint.setVisibility(View.GONE);
            mNewSubject.setVisibility(View.GONE);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.upload_image, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.upload_image_done:
                uploadImage();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void uploadImage() {
        try {
            String newSubject = "";

            if (mContest.is_user_contest == 0) {
                newSubject = mNewSubject.getText().toString().trim();
                if (newSubject.length() == 0) {
                    throw new ToastException(R.string.error_empty_subject);
                }
            }

            String description = mDescription.getText().toString();

            try {
                String user = Settings.getUserId(mContext);
                String pass = Settings.getPassword(mContext);
                RestService service = new RestService(user, pass);
                Output image = new Output(mContext, mOutputFileUri);
                service.getContestApi().addImageToContest(mContest.id, newSubject, image, mExif, description,
                        new ConfirmImageHandler(mView));
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } catch (ToastException e) {
            e.show(mContext);
        }
    }

    public void setFileName(String fileName) {
        mFileName = fileName;
    }

    private class ConfirmImageHandler extends CallbackHandler<Void> {

        public ConfirmImageHandler(View view) {
            super(view, R.id.upload_hide_when_loading, R.id.upload_loading_progress);
        }

        @Override
        public void failure(RetrofitError error) {
            super.failure(error);

            Response response = error.getResponse();
            try {
                if (response == null) {
                    throw new ToastException(R.string.error_network_problem);
                }

                if (response.getStatus() == 401) {
                    throw new ToastException(R.string.error_wrong_password);
                }

                RestService.ErrorData err = (RestService.ErrorData) error.getBodyAs(RestService.ErrorData.class);

                throw new ToastException(err.error);
            } catch (ToastException e) {
                e.show(mContext);
            }

        }

        @Override
        public void success(Void data, Response response) {
            super.success(data, response);
            getActivity().getSupportFragmentManager().popBackStack();
            Common.showMessage(mContext, R.string.upload_successfully);
        }

    }

    private class Output implements TypedOutput {
        private InputStream mStream;

        public Output(Context context, Uri uri) throws IOException {
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

            while ((bytesRead = mStream.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        }

    }
}
