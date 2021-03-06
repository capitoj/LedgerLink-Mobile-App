package org.applab.ledgerlink;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.TabHost.TabContentFactory;
import android.widget.TabWidget;

 import android.support.v7.app.ActionBarActivity;

import org.applab.ledgerlink.fontutils.RobotoTextStyleExtractor;
import org.applab.ledgerlink.fontutils.TypefaceManager;

import java.util.HashMap;

/**
 * Created by Moses on 6/24/13.
 */
public class MeetingTabHostActivity extends ActionBarActivity implements TabHost.OnTabChangeListener {
    private TabHost mTabHost;
    private final HashMap<String, TabInfo> mapTabInfo = new HashMap<String, MeetingTabHostActivity.TabInfo>();
    private TabInfo mLastTab = null;
    /**
     *
     * @author mwho
     *
     */
    private class TabInfo {
        private final String tag;
        private final Class<?> clss;
        private final Bundle args;
        private Fragment fragment;
        TabInfo(String tag, Class<?> clazz, Bundle args) {
            this.tag = tag;
            this.clss = clazz;
            this.args = args;
        }

    }
    /**
     *
     * @author mwho
     *
     */
    class TabFactory implements TabContentFactory {

        private final Context mContext;

        /**
         * @param context
         */
        public TabFactory(Context context) {
            mContext = context;
        }

        /** (non-Javadoc)
         * @see android.widget.TabHost.TabContentFactory#createTabContent(java.lang.String)
         */
        public View createTabContent(String tag) {
            View v = new View(mContext);
            v.setMinimumWidth(0);
            v.setMinimumHeight(0);
            return v;
        }

    }
    /** (non-Javadoc)
     * @see android.support.v4.app.FragmentActivity#onCreate(android.os.Bundle)
     */
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TypefaceManager.addTextStyleExtractor(RobotoTextStyleExtractor.getInstance());

        setContentView(R.layout.activity_meeting);
        initialiseTabHost(savedInstanceState);
        if (savedInstanceState != null) {
            mTabHost.setCurrentTabByTag(savedInstanceState.getString(getString(R.string.tab))); //set the tab as per the saved state
        }
    }

    /** (non-Javadoc)
     * @see android.support.v4.app.FragmentActivity#onSaveInstanceState(android.os.Bundle)
     */
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(getString(R.string.tab), mTabHost.getCurrentTabTag()); //save the tab selected
        super.onSaveInstanceState(outState);
    }

    /**
     * Initialise the Tab Host
     */
    private void initialiseTabHost(Bundle args) {
        mTabHost = (TabHost)findViewById(android.R.id.tabhost);
        mTabHost.setup();
        TabInfo tabInfo = null;
        MeetingTabHostActivity.addTab(this, this.mTabHost, this.mTabHost.newTabSpec(getString(R.string.summary)).setIndicator(getString(R.string.summary), getResources().getDrawable(R.drawable.android)), (tabInfo = new TabInfo(getString(R.string.summary), MeetingSummaryFrag.class, args)));
        this.mapTabInfo.put(tabInfo.tag, tabInfo);
        MeetingTabHostActivity.addTab(this, this.mTabHost, this.mTabHost.newTabSpec(getString(R.string.roll_call)).setIndicator(getString(R.string.roll_call), getResources().getDrawable(R.drawable.android)), (tabInfo = new TabInfo(getString(R.string.roll_call), MeetingRollCallFrag.class, args)));
        this.mapTabInfo.put(tabInfo.tag, tabInfo);
        MeetingTabHostActivity.addTab(this, this.mTabHost, this.mTabHost.newTabSpec(getString(R.string.savings)).setIndicator(getString(R.string.savingschema), getResources().getDrawable(R.drawable.android)), (tabInfo = new TabInfo(getString(R.string.savings), MeetingSavingsFrag.class, args)));
        this.mapTabInfo.put(tabInfo.tag, tabInfo);
        MeetingTabHostActivity.addTab(this, this.mTabHost, this.mTabHost.newTabSpec(getString(R.string.loansrepaid)).setIndicator(getString(R.string.loans_repaid), getResources().getDrawable(R.drawable.android)), (tabInfo = new TabInfo(getString(R.string.loansrepaid), MeetingLoansRepaidFrag.class, args)));
        this.mapTabInfo.put(tabInfo.tag, tabInfo);
        MeetingTabHostActivity.addTab(this, this.mTabHost, this.mTabHost.newTabSpec(getString(R.string.loanissued)).setIndicator(getString(R.string.loanissued), getResources().getDrawable(R.drawable.android)), (tabInfo = new TabInfo(getString(R.string.loanissued), MeetingLoansIssuedFrag.class, args)));
        this.mapTabInfo.put(tabInfo.tag, tabInfo);

        //
        mTabHost.setOnTabChangedListener(this);

        //Add a HorizontalScrollView
        TabWidget tw = (TabWidget) findViewById(android.R.id.tabs);
        LinearLayout ll = (LinearLayout) tw.getParent();
        HorizontalScrollView hs = new HorizontalScrollView(this);
        hs.setLayoutParams(new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT));
        ll.addView(hs, 0);
        ll.removeView(tw);
        hs.addView(tw);
        hs.setHorizontalScrollBarEnabled(false);
        hs.setFillViewport(true);

        //Set Tab Width
        for (int i = 0; i < mTabHost.getTabWidget().getTabCount(); i++) {
            mTabHost.getTabWidget().getChildAt(i).getLayoutParams().width = 200;
        }

        // Default to first tab
        this.onTabChanged(getString(R.string.savings));

    }

    /**
     * @param activity
     * @param tabHost
     * @param tabSpec

     */
    private static void addTab(MeetingTabHostActivity activity, TabHost tabHost, TabHost.TabSpec tabSpec, TabInfo tabInfo) {
        // Attach a Tab view factory to the spec
        tabSpec.setContent(activity.new TabFactory(activity));
        String tag = tabSpec.getTag();

        // Check to see if we already have a fragment for this tab, probably
        // from a previously saved state.  If so, deactivate it, because our
        // initial state is that a tab isn't shown.
        tabInfo.fragment = activity.getSupportFragmentManager().findFragmentByTag(tag);
        if (tabInfo.fragment != null && !tabInfo.fragment.isDetached()) {
            FragmentTransaction ft = activity.getSupportFragmentManager().beginTransaction();
            ft.detach(tabInfo.fragment);
            ft.commit();
            activity.getSupportFragmentManager().executePendingTransactions();
        }

        tabHost.addTab(tabSpec);
    }

    /** (non-Javadoc)
     * @see android.widget.TabHost.OnTabChangeListener#onTabChanged(java.lang.String)
     */
    public void onTabChanged(String tag) {
        TabInfo newTab = this.mapTabInfo.get(tag);
        if (mLastTab != newTab) {
            FragmentTransaction ft = this.getSupportFragmentManager().beginTransaction();
            if (mLastTab != null) {
                if (mLastTab.fragment != null) {
                    ft.detach(mLastTab.fragment);
                }
            }
            if (newTab != null) {
                if (newTab.fragment == null) {
                    newTab.fragment = Fragment.instantiate(this, newTab.clss.getName(), newTab.args);
                    ft.add(R.id.realtabcontent, newTab.fragment, newTab.tag);
                }
                else {
                    ft.attach(newTab.fragment);
                }
            }

            mLastTab = newTab;
            ft.commit();
            this.getSupportFragmentManager().executePendingTransactions();
        }
    }
}