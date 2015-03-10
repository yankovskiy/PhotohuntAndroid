package ru.neverdark.photohunt.utils;

import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

public class ButtonBGOnTouchListener implements OnTouchListener {
    @Override
    public boolean onTouch(View view, MotionEvent event) {

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                view.getBackground().setColorFilter(0x77000000, android.graphics.PorterDuff.Mode.SRC_ATOP);
                view.invalidate();
                break;
            }
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL: {
                view.getBackground().clearColorFilter();
                view.invalidate();
                break;
            }
        }

        return false;
    }

}
