package ru.neverdark.photohunt.dialogs;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import ru.neverdark.abs.CancelClickListener;
import ru.neverdark.abs.UfoDialogFragment;
import ru.neverdark.photohunt.R;
import ru.neverdark.photohunt.rest.data.Contest;
import ru.neverdark.photohunt.utils.ToastException;

public class NewContestDialog extends UfoDialogFragment {
    public static final String DIALOG_ID = "newContestDialog";
    private EditText mSubject;
    private Spinner mRewards;

    public static NewContestDialog getInstance(Context context) {
        NewContestDialog dialog = new NewContestDialog();
        dialog.setContext(context);
        return dialog;
    }

    @Override
    public void bindObjects() {
        setDialogView(View.inflate(getContext(), R.layout.new_contest_dialog, null));
        mSubject = (EditText) getDialogView().findViewById(R.id.new_contest_subject);
        mRewards = (Spinner) getDialogView().findViewById(R.id.new_contest_rewards);
    }

    @Override
    public void setListeners() {
        getAlertDialog().setPositiveButton(R.string.dialog_ok, null);
        getAlertDialog().setNegativeButton(R.string.dialog_cancel, new CancelClickListener());
    }

    @Override
    public void onStart() {
        super.onStart();
        AlertDialog dialog = (AlertDialog) getDialog();
        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(
                new PositiveClickListener());
    }

    @Override
    protected void createDialog() {
        super.createDialog();
        getAlertDialog().setTitle(R.string.create_contest);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.rewards, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mRewards.setAdapter(adapter);
    }

    public interface OnPositiveClickListener {
        public void onPositiveClickHandler(Contest contest);
    }

    private class PositiveClickListener implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            OnPositiveClickListener callback = (OnPositiveClickListener) getCallback();
            if (callback != null) {
                try {
                    String subject = mSubject.getText().toString().trim();
                    if (subject.length() == 0) {
                        throw new ToastException(R.string.error_empty_title);
                    }

                    if (mRewards.getSelectedItemPosition() == 0) {
                        throw new ToastException(R.string.error_empty_reward);
                    }

                    Contest contest = new Contest();
                    contest.subject = subject;
                    contest.rewards = mRewards.getSelectedItemPosition();
                    callback.onPositiveClickHandler(contest);
                    dismiss();
                } catch (ToastException e) {
                    e.show(getContext());
                }
            }
        }
    }
}
