package cl.sidan.clac.interfaces;

import android.animation.ObjectAnimator;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ListView;

import java.util.Dictionary;
import java.util.Hashtable;

public class ScrollListener implements AbsListView.OnScrollListener
{
    private static Dictionary<Integer, Integer> itemHeights = new Hashtable<Integer, Integer>();

    private final ViewType viewType;
    private final View headerView;
    private final View footerView;
    private final int minimumHeaderTranslation;
    private final int minimumFooterTranslation;
    private final boolean isSnappable; // Snap into place or not.

    private int previousScrollY = 0;
    private int totalHeaderDiff = 0;
    private int totalFooterDiff = 0;

    private ScrollListener(Builder builder)
    {
        viewType = builder.viewType;
        headerView = builder.header;
        minimumHeaderTranslation = builder.minHeaderTranslation;
        footerView = builder.footer;
        minimumFooterTranslation = builder.minFooterTranslation;
        isSnappable = builder.isSnappable;
    }

    public enum ViewType {
        HEADER,
        FOOTER,
        BOTH
    }

    public static int getScrollY(ListView lv) {
        View c = lv.getChildAt(0);
        if (c == null) {
            return 0;
        }
        return -c.getTop() + lv.getFirstVisiblePosition() * c.getHeight();
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
        if (scrollState == SCROLL_STATE_IDLE && isSnappable)
        {
            switch (viewType)
            {
                case HEADER:
                    totalHeaderDiff = slideHeader();
                    break;

                case FOOTER:
                    totalFooterDiff = slideFooter();
                    break;

                case BOTH:
                    totalHeaderDiff = slideHeader();
                    totalFooterDiff = slideFooter();
                    break;

                default:
                    break;
            }
        }
    }

    private int slideHeader()
    {
        int midHeader = -minimumHeaderTranslation / 2;

        if (-totalHeaderDiff > 0 && -totalHeaderDiff < midHeader) {
            ObjectAnimator anim = ObjectAnimator.ofFloat(headerView, "translationY", headerView.getTranslationY(), 0);
            anim.setDuration(100);
            anim.start();
            return 0;
        } else if (-totalHeaderDiff < -minimumHeaderTranslation && -totalHeaderDiff >= midHeader) {
            ObjectAnimator anim = ObjectAnimator.ofFloat(headerView, "translationY", headerView.getTranslationY(), minimumHeaderTranslation);
            anim.setDuration(100);
            anim.start();
            return minimumHeaderTranslation;
        }
        return totalHeaderDiff;
    }

    private int slideFooter()
    {
        int midFooter = minimumFooterTranslation / 2;

        if (-totalFooterDiff > 0 && -totalFooterDiff < midFooter) { // slide up
            ObjectAnimator anim = ObjectAnimator.ofFloat(footerView, "translationY", footerView.getTranslationY(), 0);
            anim.setDuration(100);
            anim.start();
            return 0;
        } else if (-totalFooterDiff < minimumFooterTranslation && -totalFooterDiff >= midFooter) { // slide down
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
        int scrollY = getScrollY(listview);
        int diff = previousScrollY - scrollY;

        if (diff != 0) {
            switch (viewType) {
                case HEADER:
                    totalHeaderDiff = newHeaderDiff(diff);
                    headerView.setTranslationY(totalHeaderDiff);
                    break;

                case FOOTER:
                    totalFooterDiff = newFooterDiff(diff);
                    footerView.setTranslationY(-totalFooterDiff);
                    break;

                case BOTH:
                    totalHeaderDiff = newHeaderDiff(diff);
                    totalFooterDiff = newFooterDiff(diff);
                    headerView.setTranslationY(totalHeaderDiff);
                    footerView.setTranslationY(-totalFooterDiff);
                    break;

                default:
                    break;
            }
        }

        previousScrollY = scrollY;
    }

    private int newHeaderDiff(int diff)
    {
        if (diff < 0) { // scrolling down
            return Math.max(totalHeaderDiff + diff, minimumHeaderTranslation);
        } else { // scrolling up
            return Math.min(Math.max(totalHeaderDiff + diff, minimumHeaderTranslation), 0);
        }
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
        private final ViewType viewType;

        private View header = null;
        private int minHeaderTranslation = 0;
        private View footer = null;
        private int minFooterTranslation = 0;
        private boolean isSnappable = false;

        public Builder(ViewType viewType) {
            this.viewType = viewType;
        }

        public Builder header(View header) {
            this.header = header;
            return this;
        }

        public Builder minHeaderTranslation(int minHeaderTranslation) {
            this.minHeaderTranslation = minHeaderTranslation;
            return this;
        }

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
    }
}
