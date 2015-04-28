package com.example.android.sunshine.app.utils.helper;

import android.util.SparseArray;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.maps.android.SphericalUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by tlnacl on 17/12/14.
 */
public final class MarkerHelper<T> {

    public static final float PERCENTAGE_TO_REDUCE_BOUNDS_DEFAULT = 0.1f;

    private final SparseArray<T> mItems;

    public MarkerHelper() {
        mItems = new SparseArray<T>();
    }

    public MarkerState<T> setItems(Collection<T> items) {
        MarkerState<T> state = new MarkerState<T>();
        final Set<Integer> hashes = new HashSet<Integer>();

        // Adding new items
        for(T item : items) {
            int newItemHash = item.hashCode();
            hashes.add(newItemHash);

            if(mItems.indexOfKey(newItemHash) < 0) {
                mItems.put(newItemHash, item);
                state.mNewItems.add(item);
            }
        }

        //Removing not existing items
        final SparseArray<T> itemsClone = mItems.clone();
        for(int i=0; i<itemsClone.size(); ++i) {
            int hashCode = itemsClone.keyAt(i);

            if(!hashes.contains(hashCode)) {
                state.mRemovedItems.add(mItems.get(hashCode));
                mItems.remove(hashCode);
            }
        }
        itemsClone.clear();
        hashes.clear();

        return state;
    }

    public void clear() {
        mItems.clear();
    }

    public static class MarkerState<T> {
        private final List<T> mNewItems;
        private final List<T> mRemovedItems;

        MarkerState() {
            mNewItems = new ArrayList<T>();
            mRemovedItems = new ArrayList<T>();
        }

        public Collection<T> getNewItems() {
            return mNewItems;
        }

        public Collection<T> getRemovedItems() {
            return mRemovedItems;
        }
    }

    public static LatLngBounds reduceBounds(final LatLngBounds bounds, final float percentageToReduce) {
        if(percentageToReduce >= 1.0f || percentageToReduce <= 0) {
            throw new IllegalArgumentException("Percentage to reduce should be between 0.01f (1%) and 0.99f (99%)");
        }

        return new LatLngBounds(SphericalUtil.interpolate(bounds.southwest, bounds.getCenter(), percentageToReduce),
                SphericalUtil.interpolate(bounds.northeast, bounds.getCenter(), percentageToReduce));
    }
}

