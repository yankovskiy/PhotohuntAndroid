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
                view.setTextColor(0xff607d8b);
                view.invalidate();
                break;
            }
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL: {
                TextView view = (TextView) v;
                //clear the overlay
                view.setTextColor(mCurrentTextColor);
                view.invalidate();
                break;
            }
        }

        return false;
    }

}
