package ru.neverdark.photohunt.dialogs;

import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.widget.EditText;

import ru.neverdark.abs.CancelClickListener;
import ru.neverdark.abs.UfoDialogFragment;
import ru.neverdark.photohunt.R;
import ru.neverdark.photohunt.rest.RestService;
import ru.neverdark.photohunt.utils.Log;
import ru.neverdark.photohunt.utils.ToastException;

public class EditImageDialog extends UfoDialogFragment {
    private EditText mSubject;
    private RestService.Image mImage;

    private class PositiveClickListener implements DialogInterface.OnClickListener {

        @Override
        public void onClick(DialogInterface dialog, int which) {
            try {
                String newSubject = mSubject.getText().toString().trim();
                if (newSubject.length() == 0) {
                    throw new ToastException(R.string.error_empty_subject);
                }

                OnPositiveClickListener callback = (OnPositiveClickListener) getCallback();
                if (callback != null) {
                    mImage.subject = newSubject;
                    callback.onPositiveClickHandler(mImage);
                }
            } catch (ToastException e) {
                e.show(getContext());
            }
        }
    }

    public interface OnPositiveClickListener {
        public void onPositiveClickHandler(RestService.Image image);
    }

    @Override
    public void setListeners() {
        getAlertDialog().setPositiveButton(R.string.dialog_ok, new PositiveClickListener());
        getAlertDialog().setNegativeButton(R.string.dialog_cancel, new CancelClickListener());
    }

    public static final String DIALOG_ID = "editImageDialog";

    @Override
    public void bindObjects() {
        setDialogView(View.inflate(getContext(), R.layout.edit_image_dialog, null));
        mSubject = (EditText) getDialogView().findViewById(R.id.edit_image_subject);
    }

    public static EditImageDialog getInstance(Context context) {
        EditImageDialog dialog = new EditImageDialog();
        dialog.setContext(context);
        return dialog;
    }

    @Override
    protected void createDialog() {
        super.createDialog();
        getAlertDialog().setTitle(R.string.edit_image_subject_title);
        mSubject.setText(mImage.subject);
    }

    public void setImage(RestService.Image image) {
        mImage = image;
    }

}
