/*******************************************************************************
 * Copyright (C) 2014 Artem Yankovskiy (artemyankovskiy@gmail.com).
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package ru.neverdark.photohunt.utils;

import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ImageView;

/**
 * OnTouchListener for ImageView onClick highlights
 */
public class ImageOnTouchListener implements OnTouchListener {
    private final boolean mIsBackground;

    public ImageOnTouchListener() {
        mIsBackground = true;
    }

    public ImageOnTouchListener(boolean isBackground) {
        mIsBackground = isBackground;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                Log.message("down");
                ImageView view = (ImageView) v;
                //overlay is black with transparency of 0x77 (119)
                if (mIsBackground) {
                    view.getBackground().setColorFilter(0x30000000, android.graphics.PorterDuff.Mode.SRC_ATOP);
                }
                view.getDrawable().setColorFilter(0x77000000, android.graphics.PorterDuff.Mode.SRC_ATOP);
                view.invalidate();
                break;
            }
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL: {
                Log.message("up");
                ImageView view = (ImageView) v;
                //clear the overlay
                if (mIsBackground) {
                    view.getBackground().clearColorFilter();
                }
                view.getDrawable().clearColorFilter();
                view.invalidate();
                break;
            }
        }

        return false;
    }
}
