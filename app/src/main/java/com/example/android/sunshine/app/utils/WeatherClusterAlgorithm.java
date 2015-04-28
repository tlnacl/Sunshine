package com.example.android.sunshine.app.utils;

import com.example.android.sunshine.app.ui.WeatherUI;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.clustering.algo.Algorithm;
import com.google.maps.android.clustering.algo.StaticCluster;
import com.google.maps.android.geometry.Bounds;
import com.google.maps.android.geometry.Point;
import com.google.maps.android.projection.SphericalMercatorProjection;
import com.google.maps.android.quadtree.PointQuadTree;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by tlnacl on 17/12/14.
 * A simple clustering algorithm with O(nlog n) performance. Resulting clusters are not
 * hierarchical.
 * <p/>
 * High level algorithm:<br>
 * 1. Iterate over items in the order they were added (candidate clusters).<br>
 * 2. Create a cluster with the center of the item. <br>
 * 3. Add all items that are within a certain distance to the cluster. <br>
 * 4. Move any items out of an existing cluster if they are closer to another cluster. <br>
 * 5. Remove those items from the list of candidate clusters.
 * <p/>
 * Clusters have the center of the first element (not the centroid of the items within it).
 */
public class WeatherClusterAlgorithm<T extends WeatherUI>  implements Algorithm<T> {

    /**
     * Any modifications should be synchronized on mQuadTree.
     */
    private final Collection<QuadItem<T>> mItems = new ArrayList<QuadItem<T>>();

    /**
     * Any modifications should be synchronized on mQuadTree.
     */
    private final PointQuadTree<QuadItem<T>> mQuadTree = new PointQuadTree<>(0, 1, 0, 1);

    private static final SphericalMercatorProjection PROJECTION = new SphericalMercatorProjection(1);

    @Override
    public void addItem(T item) {
        final QuadItem<T> quadItem = new QuadItem<>(item);
        synchronized (mQuadTree) {
            mItems.add(quadItem);
            mQuadTree.add(quadItem);
        }
    }

    @Override
    public void addItems(Collection<T> items) {
        for (T item : items) {
            addItem(item);
        }
    }

    @Override
    public void clearItems() {
        synchronized (mQuadTree) {
            mItems.clear();
            mQuadTree.clear();
        }
    }

    @Override
    public void removeItem(T item) {
        final QuadItem<T> quadItem = new QuadItem<>(item);
        synchronized (mQuadTree) {
            if(mItems.contains(quadItem)) {
                mItems.remove(quadItem);
                mQuadTree.remove(quadItem);
            }
        }
    }

    @Override
    public Set<? extends Cluster<T>> getClusters(double zoom) {
        final Set<Cluster<T>> results = new HashSet<>();
        final Set<Long> validatedParcelIds = new HashSet<>();

        synchronized (mQuadTree) {
            for (QuadItem<T> candidate : mItems) {
                long parcelId = candidate.mClusterItem.getParcelID();
                if(validatedParcelIds.contains(parcelId)) continue;

                StaticCluster<T> cluster = new StaticCluster<>(candidate.mClusterItem.getPosition());
                for(QuadItem<T> clusterItem : mItems) {
                    long childParcelID = clusterItem.mClusterItem.getParcelID();
                    if(childParcelID == parcelId) {
                        cluster.add(clusterItem.mClusterItem);
                    }
                }
                if(cluster.getSize() == 1) {
                    //No other items in same place
                    results.add(candidate);
                } else {
                    results.add(cluster);
                }

                validatedParcelIds.add(parcelId);
            }
        }
        return results;
    }

    @Override
    public Collection<T> getItems() {
        final List<T> items = new ArrayList<T>();
        synchronized (mQuadTree) {
            for (QuadItem<T> quadItem : mItems) {
                items.add(quadItem.mClusterItem);
            }
        }
        return items;
    }

    private double distanceSquared(Point a, Point b) {
        return (a.x - b.x) * (a.x - b.x) + (a.y - b.y) * (a.y - b.y);
    }

    private Bounds createBoundsFromSpan(Point p, double span) {
        // TODO: Use a span that takes into account the visual size of the marker, not just its
        // LatLng.
        double halfSpan = span / 2;
        return new Bounds(
                p.x - halfSpan, p.x + halfSpan,
                p.y - halfSpan, p.y + halfSpan);
    }

    private static class QuadItem<T extends ClusterItem> implements PointQuadTree.Item, Cluster<T> {
        private final T mClusterItem;
        private final Point mPoint;
        private final LatLng mPosition;
        private Set<T> singletonSet;

        private QuadItem(T item) {
            mClusterItem = item;
            mPosition = item.getPosition();
            mPoint = PROJECTION.toPoint(mPosition);
            singletonSet = Collections.singleton(mClusterItem);
        }

        @Override
        public Point getPoint() {
            return mPoint;
        }

        @Override
        public LatLng getPosition() {
            return mPosition;
        }

        @Override
        public Set<T> getItems() {
            return singletonSet;
        }

        @Override
        public int getSize() {
            return 1;
        }

        @Override
        public boolean equals(Object o) {
            if(o!=null && o instanceof QuadItem) {
                QuadItem obj = (QuadItem) o;
                return mClusterItem.equals(obj.mClusterItem);
            }
            return super.equals(o);
        }
    }
}
