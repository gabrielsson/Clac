package cl.sidan.clac.interfaces;

import android.animation.ObjectAnimator;
import android.support.v7.app.ActionBar;
import android.view.View;
import android.widget.AbsListView;

import java.util.Dictionary;
import java.util.Hashtable;

public class ScrollListener implements AbsListView.OnScrollListener
{
    private static Dictionary<Integer, Integer> itemHeights = new Hashtable<Integer, Integer>();

    private final View footerView;
    private final int minimumFooterTranslation;
    private final boolean isSnappable; // Snap into place or not.
    private final ActionBar actionbar;

    private boolean isScrolling = false;
    private int mLastFirstVisibleItem = 0;

    private int previousScrollY = 0;
    private int totalFooterDiff = 0;

    private ScrollListener(Builder builder)
    {
        footerView = builder.footer;
        actionbar = builder.actionbar;
        minimumFooterTranslation = builder.minFooterTranslation;
        isSnappable = builder.isSnappable;
    }

    public static int getScrollY(AbsListView lv) {
        View c = lv.getChildAt(0);
        if (c == null) {
            return 0;
        }

        int firstVisiblePosition = lv.getFirstVisiblePosition();
        itemHeights.put(firstVisiblePosition, c.getHeight());

        int scrollY = -c.getTop();
        if (scrollY < 0) {
            scrollY = 0;
        }

        for (int i = 0; i < firstVisiblePosition; ++i) {
            if (itemHeights.get(i) != null) {
                scrollY += itemHeights.get(i);
            }
        }

        return scrollY;
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState)
    {
        if ( scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE ) {
            isScrolling = false;
        }

        if (scrollState == SCROLL_STATE_IDLE && isSnappable)
        {
            totalFooterDiff = slideFooter();
        }
    }

    private int slideFooter()
    {
        int midFooter = minimumFooterTranslation / 2;

        if (-totalFooterDiff >= 0 && -totalFooterDiff < midFooter) { // slide up
            ObjectAnimator anim = ObjectAnimator.ofFloat(footerView, "translationY", footerView.getTranslationY(), 0);
            anim.setDuration(100);
            anim.start();
            return 0;
        } else if (-totalFooterDiff <=  minimumFooterTranslation && -totalFooterDiff >= midFooter) { // slide down
            ObjectAnimator anim = ObjectAnimator.ofFloat(footerView, "translationY", footerView.getTranslationY(), minimumFooterTranslation);
            anim.setDuration(100);
            anim.start();
            return -minimumFooterTranslation;
        }
        return totalFooterDiff;
    }

    @Override
    public void onScroll(AbsListView listview, int firstVisibleItem, int visibleItemCount, int totalItemCount)
    {
        final int currentFirstVisibleItem = listview.getFirstVisiblePosition();

        if (currentFirstVisibleItem > mLastFirstVisibleItem && !isScrolling && null != actionbar) {
            isScrolling = true;
            actionbar.hide();
        } else if (currentFirstVisibleItem < mLastFirstVisibleItem && !isScrolling && null != actionbar) {
            isScrolling = true;
            actionbar.show();
        }

        mLastFirstVisibleItem = currentFirstVisibleItem;

        int scrollY = getScrollY(listview);
        int diff = previousScrollY - scrollY;

        if (diff != 0) {
            totalFooterDiff = newFooterDiff(diff);
            footerView.setTranslationY(-totalFooterDiff);
        }

        previousScrollY = scrollY;
    }

    private int newFooterDiff(int diff)
    {
        if (diff < 0) { // scrolling down
            return Math.max(totalFooterDiff + diff, -minimumFooterTranslation);
        } else { // scrolling up
            return Math.min(Math.max(totalFooterDiff + diff, -minimumFooterTranslation), 0);
        }
    }

    public static class Builder
    {
        private View footer = null;
        private int minFooterTranslation = 0;
        private boolean isSnappable = false;
        private ActionBar actionbar;

        public Builder footer(View footer) {
            this.footer = footer;
            return this;
        }

        public Builder minFooterTranslation(int minFooterTranslation) {
            this.minFooterTranslation = minFooterTranslation;
            return this;
        }

        public Builder isSnappable(boolean isSnappable) {
            this.isSnappable = isSnappable;
            return this;
        }

        public ScrollListener build() {
            return new ScrollListener(this);
        }

        public Builder actionbar(ActionBar ab) {
            this.actionbar = ab;
            return this;
        }
    }
}