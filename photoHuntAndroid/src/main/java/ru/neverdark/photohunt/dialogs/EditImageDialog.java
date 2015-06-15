package ru.neverdark.photohunt.dialogs;

import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.widget.EditText;

import ru.neverdark.abs.CancelClickListener;
import ru.neverdark.abs.UfoDialogFragment;
import ru.neverdark.photohunt.R;
import ru.neverdark.photohunt.rest.data.Image;
import ru.neverdark.photohunt.utils.ToastException;

public class EditImageDialog extends UfoDialogFragment {
    public static final String DIALOG_ID = "editImageDialog";
    private EditText mSubject;
    private EditText mDescription;
    private Image mImage;

    public static EditImageDialog getInstance(Context context) {
        EditImageDialog dialog = new EditImageDialog();
        dialog.setContext(context);
        return dialog;
    }

    @Override
    public void setListeners() {
        getAlertDialog().setPositiveButton(R.string.dialog_ok, new PositiveClickListener());
        getAlertDialog().setNegativeButton(R.string.dialog_cancel, new CancelClickListener());
    }

    @Override
    public void bindObjects() {
        setDialogView(View.inflate(getContext(), R.layout.edit_image_dialog, null));
        mSubject = (EditText) getDialogView().findViewById(R.id.edit_image_subject);
        mDescription = (EditText) getDialogView().findViewById(R.id.edit_image_description);
    }

    @Override
    protected void createDialog() {
        super.createDialog();
        getAlertDialog().setTitle(R.string.view_image_info);
        if (mImage.subject != null && mImage.subject.length() > 0) {
            mSubject.setText(mImage.subject);
            mSubject.setVisibility(View.VISIBLE);
        }
        mDescription.setText(mImage.description);
    }

    public void setImage(Image image) {
        mImage = image;
    }

    public interface OnPositiveClickListener {
        public void onPositiveClickHandler(Image image);
    }

    private class PositiveClickListener implements DialogInterface.OnClickListener {

        @Override
        public void onClick(DialogInterface dialog, int which) {
            try {
                String newSubject = null;
                if (mImage.subject != null && mImage.subject.length() > 0) {
                    newSubject = mSubject.getText().toString().trim();
                    if (newSubject.length() == 0) {
                        throw new ToastException(R.string.error_empty_subject);
                    }
                }
                OnPositiveClickListener callback = (OnPositiveClickListener) getCallback();
                if (callback != null) {
                    mImage.subject = newSubject;
                    mImage.description = mDescription.getText().toString().trim();
                    callback.onPositiveClickHandler(mImage);
                }
            } catch (ToastException e) {
                e.show(getContext());
            }
        }
    }

}
