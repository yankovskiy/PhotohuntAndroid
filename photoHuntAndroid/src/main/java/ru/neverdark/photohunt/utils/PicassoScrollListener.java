package ru.neverdark.photohunt.utils;

import android.content.Context;
import android.widget.AbsListView;

import com.squareup.picasso.Picasso;

public class PicassoScrollListener implements AbsListView.OnScrollListener {
  private final Context mContext;

  public PicassoScrollListener(Context context) {
    this.mContext = context;
  }

  @Override
  public void onScrollStateChanged(AbsListView view, int scrollState) {
    final Picasso picasso = Picasso.with(mContext);
    if (scrollState == SCROLL_STATE_IDLE || scrollState == SCROLL_STATE_TOUCH_SCROLL) {
      picasso.resumeTag(mContext);
    } else {
      picasso.pauseTag(mContext);
    }
  }

  @Override
  public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
                       int totalItemCount) {
    // Do nothing.
  }
}
