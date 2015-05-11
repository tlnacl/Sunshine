package com.example.android.sunshine.app.test;

import android.app.Activity;

import com.example.android.sunshine.app.BuildConfig;
import com.example.android.sunshine.app.ui.MainActivity;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;
import static org.junit.Assert.*;

/**
 * Created by tomtang on 11/05/15.
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class)
public class RobolectricSample {

    @Test
    public void titleIsCorrect() throws Exception {
        Activity activity = Robolectric.setupActivity(MainActivity.class);
        assertTrue(activity.getTitle().toString().equals("Deckard"));

    }
}
