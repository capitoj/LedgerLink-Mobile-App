package org.applab.digitizingdata;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.TabWidget;

/**
 * Created by Moses on 6/24/13.
 */
public class BeginCycleActivity extends FragmentActivity {
    TabHost tHost;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_begin_cycle);

        tHost = (TabHost) findViewById(android.R.id.tabhost);
        tHost.setup();

        /** Defining Tab Change Listener event. This is invoked when tab is changed */
        TabHost.OnTabChangeListener tabChangeListener = new TabHost.OnTabChangeListener() {

            @Override
            public void onTabChanged(String tabId) {
                //android.support.v4.app.FragmentManager fm =   getSupportFragmentManager();
                FragmentManager fm =   getSupportFragmentManager();
                BeginCycleGroupInfoFrag groupInfoFragment = (BeginCycleGroupInfoFrag) fm.findFragmentByTag("groupInfo");
                BeginCycleMembersFrag membersFragment = (BeginCycleMembersFrag) fm.findFragmentByTag("members");
                BeginCycleInitialShareFrag initialShareFragment = (BeginCycleInitialShareFrag) fm.findFragmentByTag("initialShare");
                FragmentTransaction ft = fm.beginTransaction();

                /** Detaches the groupInfofragment if exists */
                if(groupInfoFragment!=null)
                    ft.detach(groupInfoFragment);

                /** Detaches the membersfragment if exists */
                if(membersFragment!=null)
                    ft.detach(membersFragment);

                /** Detaches the initialSharesfragment if exists */
                if(initialShareFragment!=null)
                    ft.detach(initialShareFragment);

                if(tabId.equalsIgnoreCase("groupInfo")){    /** If current tab is GroupInfo */
                    if(groupInfoFragment==null){
                        /** Create GroupInfoFragment and adding to fragmenttransaction */
                        ft.add(R.id.realtabcontent,new BeginCycleGroupInfoFrag(), "groupInfo");
                    }
                    else{
                        /** Bring to the front, if already exists in the fragmenttransaction */
                        ft.attach(groupInfoFragment);
                    }
                }
                else if(tabId.equalsIgnoreCase("members")){    /** If current tab is Members */
                    if(membersFragment==null){
                        /** Create MembersFragment and adding to fragmenttransaction */
                        ft.add(R.id.realtabcontent,new BeginCycleMembersFrag(), "members");
                    }
                    else{
                        /** Bring to the front, if already exists in the fragmenttransaction */
                        ft.attach(membersFragment);
                    }
                }
                else if(tabId.equalsIgnoreCase("initialShare")){    /** If current tab is Initial Share */
                    if(initialShareFragment==null){
                        /** Create InitialShareFragment and adding to fragmenttransaction */
                        ft.add(R.id.realtabcontent,new BeginCycleInitialShareFrag(), "initialShare");
                    }
                    else{
                        /** Bring to the front, if already exists in the fragmenttransaction */
                        ft.attach(initialShareFragment);
                    }
                }
                ft.commit();
            }
        };

        /** Setting tabchangelistener for the tab */
        tHost.setOnTabChangedListener(tabChangeListener);

        //ADD the TABS
        /**
         * Defining tab builder for GroupInfo
         */
        TabHost.TabSpec tSpecGroupInfo = tHost.newTabSpec("groupInfo");
        tSpecGroupInfo.setIndicator("Group Info",getResources().getDrawable(R.drawable.android));
        tSpecGroupInfo.setContent(new DummyTabContentFactory(getBaseContext()));
        tHost.addTab(tSpecGroupInfo);

        /**
         * Defining tab builder for Members
         */
        TabHost.TabSpec tSpecMembers = tHost.newTabSpec("members");
        tSpecMembers.setIndicator("Members",getResources().getDrawable(R.drawable.android));
        tSpecMembers.setContent(new DummyTabContentFactory(getBaseContext()));
        tHost.addTab(tSpecMembers);

        /**
         * Defining tab builder for Members
         */
        TabHost.TabSpec tSpecInitialShare = tHost.newTabSpec("initialShare");
        tSpecInitialShare.setIndicator("Initial Share",getResources().getDrawable(R.drawable.android));
        tSpecInitialShare.setContent(new DummyTabContentFactory(getBaseContext()));
        tHost.addTab(tSpecInitialShare);

        //Programmatically invoke Scrolling of the Tabs by enclosing the TabWidget in a HorizontalScrollView
        //Adding the HorizontalScrollView on layout fails
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

        //Trying to Control the width of the Tabs to avoid shrinking
        //Can also use a Text View:
        // //tview=new TextView(this); tview.setText("Title here"); QTabHost.addTab(QTabHost.newTabSpec("tab_test2").setIndicator(tview).bla bla....
        for (int i = 0; i < tHost.getTabWidget().getTabCount(); i++) {
            tHost.getTabWidget().getChildAt(i).getLayoutParams().width = 200;
        }
    }
}