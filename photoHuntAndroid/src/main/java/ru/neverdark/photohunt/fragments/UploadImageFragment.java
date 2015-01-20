package ru.neverdark.photohunt.fragments;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.mime.TypedOutput;
import ru.neverdark.abs.UfoFragment;
import ru.neverdark.photohunt.R;
import ru.neverdark.photohunt.dialogs.MessageDialog;
import ru.neverdark.photohunt.rest.CallbackHandler;
import ru.neverdark.photohunt.rest.RestService;
import ru.neverdark.photohunt.utils.Common;
import ru.neverdark.photohunt.utils.ImageOnTouchListener;
import ru.neverdark.photohunt.utils.Log;
import ru.neverdark.photohunt.utils.Settings;
import ru.neverdark.photohunt.utils.ToastException;

@SuppressLint("ValidFragment")
public class UploadImageFragment extends UfoFragment {
    private class OnImageHelpClickListener implements OnClickListener {

        @Override
        public void onClick(View view) {
            MessageDialog dialog = MessageDialog.getInstance(mContext);
            dialog.setMessages(R.string.hint, R.string.subject_help_message);
            dialog.show(getFragmentManager(), MessageDialog.DIALOG_ID);}
    }

    private class ConfirmImageHandler extends CallbackHandler<Void> {

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

        public ConfirmImageHandler(View view) {
            super(view);
        }

    }

    private View mView;
    private Context mContext;
    private Uri mUri;
    private ImageView mImage;
    private long mContestId;
    private EditText mNewSubject;
    private Uri mOutputFileUri;
    private String mFileName;
    private ImageView mSubjectHelp;

    public UploadImageFragment(Uri uri, long contestId) {
        mUri = uri;
        mContestId = contestId;
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
        loadImage();
        return mView;
    }

    private void loadImage() {
        try {
            Bitmap bitmap = Common.resizeBitmap(MediaStore.Images.Media.getBitmap(mContext.getContentResolver(), mUri), 1024);

            int pixels = (int) (mContext.getResources().getDisplayMetrics().density * 32);
            Log.variable("pixels", String.valueOf(pixels));
            int width = mContext.getResources().getDisplayMetrics().widthPixels - pixels;
            Log.variable("width", String.valueOf(width));

            mImage.setImageBitmap(Common.resizeBitmap(MediaStore.Images.Media.getBitmap(mContext.getContentResolver(), mUri), width));
            File file = new File(mFileName);
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
    public void bindObjects() {
        mImage = (ImageView) mView.findViewById(R.id.upload_image);
        mNewSubject = (EditText) mView.findViewById(R.id.upload_new_subject);
        mSubjectHelp = (ImageView) mView.findViewById(R.id.upload_subject_help);
    }

    @Override
    public void setListeners() {
        mSubjectHelp.setOnTouchListener(new ImageOnTouchListener());
        mSubjectHelp.setOnClickListener(new OnImageHelpClickListener());
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
            
            while((bytesRead = mStream.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        }
        
    }

    private void uploadImage() {
        try {
            String newSubject = mNewSubject.getText().toString().trim();
            if (newSubject.length() == 0) {
                throw new ToastException(R.string.error_empty_subject);
            }

            try {
                String user = Settings.getUserId(mContext);
                String pass = Settings.getPassword(mContext);
                RestService service = new RestService(user, pass);
                Output image = new Output(mContext, mOutputFileUri);
                service.getContestApi().addImageToContest(mContestId, newSubject, image,
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
}
