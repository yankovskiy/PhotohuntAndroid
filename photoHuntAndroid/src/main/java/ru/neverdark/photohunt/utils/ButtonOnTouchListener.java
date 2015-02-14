package ru.neverdark.photohunt.utils;

import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.TextView;

public class ButtonOnTouchListener implements OnTouchListener {
    private int mCurrentTextColor;
    @Override
    public boolean onTouch(View v, MotionEvent event) {

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                TextView view = (TextView) v;
                //overlay is black with transparency of 0x77 (119)
                mCurrentTextColor = view.getCurrentTextColor();
                view.getBackground().setColorFilter(0x30000000, android.graphics.PorterDuff.Mode.SRC_ATOP);
                view.setTextColor(0xff455a64);
                view.invalidate();
                break;
            }
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL: {
                TextView view = (TextView) v;
                //clear the overlay
                view.setTextColor(mCurrentTextColor);
                view.getBackground().clearColorFilter();
                view.invalidate();
                break;
            }
        }

        return false;
    }

}
