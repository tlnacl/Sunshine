package com.tl.sunshine.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.tl.sunshine.R;
import com.tl.sunshine.ui.WeatherUI;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.google.maps.android.ui.IconGenerator;

import java.util.Locale;

/**
 * Created by tlnacl on 17/12/14.
 */
public class WeatherClusterRenderer<T extends WeatherUI> extends DefaultClusterRenderer<T> {

    private static final int SEARCH_TYPE_MIXED = 0;
    private static final int MIN_CLUSTER_SIZE = 5;

    public static final int SEARCH_TYPE_SALE            = 0b0001;

    private final IconGenerator mIconGenerator;

    private SparseArray<View> mClusterViews = new SparseArray<View>();
    /**
     * Icons for each bucket.
     */
    private SparseArray<BitmapDescriptor> mIcons = new SparseArray<BitmapDescriptor>();

    public WeatherClusterRenderer(Context context, GoogleMap map, ClusterManager<T> clusterManager) {
        super(context, map, clusterManager);
        mIconGenerator = new IconGenerator(context);
        mIconGenerator.setTextAppearance(R.style.ClusterIcon_TextAppearance);
        mIconGenerator.setBackground(null);
        mIconGenerator.setTextAppearance(R.style.Marker_CusterText);

//        mClusterViews.put(SEARCH_TYPE_MIXED, makeContentView(context, SEARCH_TYPE_MIXED));
        mClusterViews.put(SEARCH_TYPE_SALE, makeContentView(context, SEARCH_TYPE_SALE));
//        mClusterViews.put(SEARCH_TYPE_RECENTLY_SALE, makeContentView(context, SEARCH_TYPE_RECENTLY_SALE));
//        mClusterViews.put(SEARCH_TYPE_PAST_SALE, makeContentView(context, SEARCH_TYPE_PAST_SALE));
    }

    @Override
    protected void onBeforeClusterItemRendered(T item, MarkerOptions markerOptions) {
//        final int type = item.getSaleType();
//        switch (type) {
//            default:
//            case SearchCriteria.SEARCH_TYPE_SALE:
                markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.map_pin));
//                break;
//            case SEARCH_TYPE_RECENTLY_SALE:
//                markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.map_pin_recent));
//                break;
//            case SEARCH_TYPE_PAST_SALE:
//                markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.map_pin_past));
//                break;
//        }
    }

    @Override
    protected void onBeforeClusterRendered(Cluster<T> cluster, MarkerOptions markerOptions) {
        int overallType = -1;
        for(T item : cluster.getItems()) {
//            int itemType = item.getSaleType();
//            if(overallType < 0) {
//                overallType = itemType;
//                continue;
//            }
//
//            if(overallType!=itemType) {
//                overallType = SEARCH_TYPE_MIXED;
//                break;
//            }

            overallType = SEARCH_TYPE_SALE;
        }

        int bucket = getBucket(cluster);
        int hashCode = String.format(Locale.ENGLISH, "%d-%d", overallType, bucket).hashCode();

        BitmapDescriptor descriptor = mIcons.get(hashCode);
        if (descriptor == null) {
            mIconGenerator.setContentView(mClusterViews.get(overallType));
            descriptor = BitmapDescriptorFactory.fromBitmap(mIconGenerator.makeIcon(getClusterText(bucket)));
            mIcons.put(hashCode, descriptor);
        }
        markerOptions.icon(descriptor);
    }

    @SuppressLint("InflateParams")
    private View makeContentView(Context context, int searchType) {
        View view = LayoutInflater.from(context).inflate(R.layout.layout_marker, null);
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        view.setLayoutParams(layoutParams);

        ImageView image = (ImageView) view.findViewById(R.id.image);
        final int drawableResId = R.drawable.map_pin_multiple_sale;
        image.setImageResource(drawableResId);

        return view;
    }

    @Override
    protected boolean shouldRenderAsCluster(Cluster<T> cluster) {
        return (cluster.getSize() > MIN_CLUSTER_SIZE);
    }

    //    @Override
//    protected int getBucket(Cluster<T> cluster) {
//        return cluster.getSize();
//    }
//
//    @Override
//    protected String getClusterText(int bucket) {
//        return String.valueOf(bucket);
//    }
}
