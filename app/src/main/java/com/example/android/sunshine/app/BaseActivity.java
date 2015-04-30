package com.example.android.sunshine.app;

import android.app.ActionBar;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;

import com.example.android.sunshine.app.ui.widgets.MultiSwipeRefreshLayout;

/**
 * Created by tlnacl on 30/12/14.
 */
public abstract class BaseActivity extends ActionBarActivity implements MultiSwipeRefreshLayout.CanChildScrollUpCallback {
    private ActionBar mActionBar;

    // SwipeRefreshLayout allows the user to swipe the screen down to trigger a manual refresh
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private int mProgressBarTopWhenActionBarShown;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mActionBar = getActionBar();
        mActionBar.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        trySetupSwipeRefresh();
        updateSwipeRefreshProgressBarTop();
    }

    private void trySetupSwipeRefresh() {
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        if (mSwipeRefreshLayout != null) {
            mSwipeRefreshLayout.setColorScheme(
                    R.color.refresh_progress_1,
                    R.color.refresh_progress_2,
                    R.color.refresh_progress_3,
                    R.color.refresh_progress_4);
            mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    requestDataRefresh();
                }
            });

            if (mSwipeRefreshLayout instanceof MultiSwipeRefreshLayout) {
                MultiSwipeRefreshLayout mswrl = (MultiSwipeRefreshLayout) mSwipeRefreshLayout;
                mswrl.setCanChildScrollUpCallback(this);
            }
        }
    }

    public void setSwipeRefreshEnabled(boolean enabled) {
        if (mSwipeRefreshLayout != null) {
            mSwipeRefreshLayout.setEnabled(enabled);
        }
    }

    protected void requestDataRefresh() {
//        Account activeAccount = AccountUtils.getActiveAccount(this);
//        ContentResolver contentResolver = getContentResolver();
//        if (contentResolver.isSyncActive(activeAccount, ApplicationContract.CONTENT_AUTHORITY)) {
//            LOGD(TAG, "Ignoring manual sync request because a sync is already in progress.");
//            return;
//        }
//        mManualSyncRequest = true;
//        LOGD(TAG, "Requesting manual data refresh.");
    }

    protected void setProgressBarTopWhenActionBarShown(int progressBarTopWhenActionBarShown) {
        mProgressBarTopWhenActionBarShown = progressBarTopWhenActionBarShown;
        updateSwipeRefreshProgressBarTop();
    }

    private void updateSwipeRefreshProgressBarTop() {
        if (mSwipeRefreshLayout == null) {
            return;
        }

//        if (mActionBarShown) {
//            mSwipeRefreshLayout.setProgressBarTop(mProgressBarTopWhenActionBarShown);
//        } else {
//            mSwipeRefreshLayout.setProgressBarTop(0);
//        }
    }

    protected void onRefreshingStateChanged(boolean refreshing) {
        if (mSwipeRefreshLayout != null) {
            mSwipeRefreshLayout.setRefreshing(refreshing);
        }
    }

    protected void enableDisableSwipeRefresh(boolean enable) {
        if (mSwipeRefreshLayout != null) {
            mSwipeRefreshLayout.setEnabled(enable);
        }
    }

    @Override
    public boolean canSwipeRefreshChildScrollUp() {
        return false;
    }
}
