package ru.neverdark.photohunt.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;
import ru.neverdark.photohunt.R;
import ru.neverdark.abs.OnCallback;
import ru.neverdark.abs.UfoFragment;
import ru.neverdark.photohunt.dialogs.RulesDialog;
import ru.neverdark.photohunt.dialogs.RulesDialog.OnAcceptRulesListener;
import ru.neverdark.photohunt.utils.ButtonBGOnTouchListener;

public class WelcomeFragment extends UfoFragment {

    private class RulesHandler implements OnCallback,OnAcceptRulesListener {

        @Override
        public void onAcceptRulesHandler() {
            UfoFragment fragment = new RegisterUserFragment();
            openFragment(fragment);
        }
        
    }

    private class ClickListener implements OnClickListener {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
            case R.id.welcome_enter:
                UfoFragment fragment = new LoginUserFragment();
                openFragment(fragment);
                break;
            case R.id.welcome_register:
                RulesDialog dialog = RulesDialog.getInstance(mContext);
                dialog.setCallback(new RulesHandler());
                dialog.show(getFragmentManager(), RulesDialog.DIALOG_ID);
                break;
            }
        }
    }
    
    private void openFragment(UfoFragment fragment) {
        if (fragment != null) {
            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.main_container, fragment);
            transaction.addToBackStack(null);
            transaction.commit();
        }
    }

    private View mView;
    private Context mContext;
    private TextView mRegisterUser;
    private TextView mLoginUser;
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceSate) {
        mView = inflater.inflate(R.layout.welcome_fragment, container, false);
        mContext = mView.getContext();
        
        bindObjects();
        setListeners();
        return mView;
    }

    @Override
    public void bindObjects() {
        mRegisterUser = (TextView) mView.findViewById(R.id.welcome_register);
        mLoginUser = (TextView) mView.findViewById(R.id.welcome_enter);

        mRegisterUser.setText(getString(R.string.register).toUpperCase());
        mLoginUser.setText(getString(R.string.enter).toUpperCase());
    }

    @Override
    public void setListeners() {
        mRegisterUser.setOnClickListener(new ClickListener());
        mLoginUser.setOnClickListener(new ClickListener());
        mRegisterUser.setOnTouchListener(new ButtonBGOnTouchListener());
        mLoginUser.setOnTouchListener(new ButtonBGOnTouchListener());
    }
}
