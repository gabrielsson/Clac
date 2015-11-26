package cl.sidan.clac.fragments;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.SparseArray;
import android.view.ViewGroup;

class MyFragmentPagerAdapter extends FragmentPagerAdapter {
    private SparseArray<String> mFragmentTags;
    private FragmentManager mFragmentManager;

    private static final int NUMBER_OF_PAGES = 5;
    final static class FragmentOrder {
        public final static int writeentry = 0;
        public final static int readentry = 1;
        public final static int arr = 2;
        public final static int settings = 3;
        public final static int versionupdate = 4;
    }

    public MyFragmentPagerAdapter(FragmentManager fm) {
        super(fm);
        mFragmentManager = fm;
        mFragmentTags = new SparseArray<>();
    }

    public Fragment getFragment(int position) {
        String tag = mFragmentTags.get(position);
        if (tag == null)
            return null;
        return mFragmentManager.findFragmentByTag(tag);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        Object obj = super.instantiateItem(container, position);
        if (obj instanceof Fragment) {
            // record the fragment tag here.
            Fragment f = (Fragment) obj;
            String tag = f.getTag();
            mFragmentTags.put(position, tag);
        }
        return obj;
    }

    @Override
    public final int getCount() {
        return NUMBER_OF_PAGES;
    }

    @Override
    public final Fragment getItem(int index) {
        switch (index) {
            case FragmentOrder.writeentry:
                return FragmentWrite.newInstance();
            case FragmentOrder.readentry:
                return FragmentReadEntries.newInstance();
            case FragmentOrder.arr:
                return FragmentArr.newInstance();
            case FragmentOrder.settings:
                return FragmentSettings.newInstance();
            case FragmentOrder.versionupdate:
                return FragmentVersionUpdate.newInstance();
            default:
                return null;
        }
    }
}