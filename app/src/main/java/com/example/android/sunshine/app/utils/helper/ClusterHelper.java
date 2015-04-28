package com.example.android.sunshine.app.utils.helper;

import android.animation.ObjectAnimator;
import android.util.Log;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.OvershootInterpolator;

import com.example.android.sunshine.app.ui.WeatherArInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by tlnacl on 22/12/14.
 */
public class ClusterHelper {

    private static final String TAG = ClusterHelper.class.getName();

    private static final float MAX_DISTANCE = 5.0f;
    private static final float WIDTH_HALF = 0.35f;

    public static List<ArCluster> prepareClusters(List<WeatherArInfo> properties) {
        // We have to sort by azimuth
        Collections.sort(properties, new Comparator<WeatherArInfo>() {
            @Override
            public int compare(WeatherArInfo lhs, WeatherArInfo rhs) {
                return (int) (lhs.getAzimuth() - rhs.getAzimuth());
            }
        });

        List<ArCluster> clusters = new ArrayList<>();
        ArCluster currentCluster = null;
        for(WeatherArInfo p : properties) {

            double azimuth = p.getAzimuth();
            double angle = Math.toDegrees(Math.atan( WIDTH_HALF / (MAX_DISTANCE + p.getGlDistance()) ));
            double left = azimuth-angle;
            double right = azimuth+angle;

            if(currentCluster == null ||
                    left > currentCluster.getRight()  ||
                    (right - currentCluster.getLeft() > 12)) {
                currentCluster = createCluster(p, left,right);
                clusters.add(currentCluster);
                continue;
            }

            currentCluster.addItem(p);
            currentCluster.setRight(right);
        }
        return clusters;
    }

    private static ArCluster createCluster(WeatherArInfo p, double left, double right) {
        ArCluster cluster = new ArCluster();
        cluster.addItem(p);
        cluster.setLeft(left);
        cluster.setRight(right);
        return cluster;
    }

    public static class ArCluster {
        private final List<WeatherArInfo> mWeatheres = new ArrayList<>();

        private double mLeft;
        private double mRight;
        private float mGlDistance;
        private float[] mWindowPosition;
//        private int mSaleType = -1;
        private boolean mSelected;
        private ObjectAnimator mObjectAnimator;
        private float mDepth = 1f;

        public void addItem(WeatherArInfo info) {
            mWeatheres.add(info);
            if(mGlDistance == 0) {
                mGlDistance = info.getGlDistance();
            } else {
                mGlDistance = (mGlDistance + info.getGlDistance())/2;
            }
        }

        public double getLeft() {
            return mLeft;
        }

        public void setLeft(double left) {
            mLeft = left;
        }

        public double getRight() {
            return mRight;
        }

        public void setRight(double right) {
            mRight = right;
        }

        public List<WeatherArInfo> getWeatheres() {
            return mWeatheres;
        }

        public int getSize() {
            return mWeatheres.size();
        }

        public float getAzimuth() {
            return (float) ((mRight + mLeft)/2);
        }

        public float getGlDistance() {
            if (mDepth >= 0) {
                return mGlDistance * mDepth;
            } else {
                return mDepth;
            }
        }

        public void setWindowPosition(float[] windowPosition) {
            mWindowPosition = windowPosition;
        }

        public float[] getWindowPosition() {
            return mWindowPosition;
        }

        public boolean isTouched(float x, float y, float radius) {
            if (mWindowPosition != null && mWindowPosition[2] <= 1) {
                if (x > mWindowPosition[0] + radius || x < mWindowPosition[0] - radius) return false;
                if (y > mWindowPosition[1] + radius || y < mWindowPosition[1] - radius) return false;
                Log.d(TAG, String.format("inside (position 0: %s, tap: %s)", mWindowPosition[0], radius));
                Log.d(TAG, String.format("inside (position 1: %s, tap: %s)", mWindowPosition[1], radius));
                return true;
            }
            if(mWindowPosition != null) {
                Log.d(TAG, String.format("outside (position 0: %s, tap: %s)", mWindowPosition[0], radius));
                Log.d(TAG, String.format("outside (position 1: %s, tap: %s)", mWindowPosition[1], radius));
            }
            return false;
        }

        public void setSelected(boolean selected) {
            if(mSelected == selected) return;
            mSelected = selected;
            animateSelection(selected);
        }

        public boolean isSelected() {
            return mSelected;
        }

        private void animateSelection(boolean selected) {
            if (mObjectAnimator != null && mObjectAnimator.isRunning()) {
                mObjectAnimator.cancel();
            }

            mObjectAnimator = ObjectAnimator.ofFloat(this, "offsetDepth",
                    selected ? new float[]{mDepth, -1f} : new float[]{mDepth, 1f});
            mObjectAnimator.setInterpolator(
                    selected ?
                            new OvershootInterpolator() :
                            new AccelerateInterpolator());
            mObjectAnimator.start();
        }

        public void setOffsetDepth(float depth) {
            mDepth = depth;
        }
    }
}
