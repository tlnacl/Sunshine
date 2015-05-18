package com.example.android.sunshine.app.ui;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.sunshine.app.Constant;
import com.example.android.sunshine.app.R;
import com.example.android.sunshine.app.events.MapSearchEvent;
import com.example.android.sunshine.app.models.CurrentWeather;
import com.example.android.sunshine.app.network.OpenWeatherClient;
import com.example.android.sunshine.app.utils.Utility;
import com.example.android.sunshine.app.utils.WeatherClusterRenderer;
import com.example.android.sunshine.app.utils.helper.MarkerHelper;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.VisibleRegion;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.algo.GridBasedAlgorithm;
import com.google.maps.android.clustering.algo.PreCachingAlgorithmDecorator;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import timber.log.Timber;


/**
 * Created by tlnacl on 15/12/14.
 */
public class WeatherMapActivity extends BaseActivity implements View.OnClickListener{

    public static final float DO_NOT_CHANGE_ZOOM = -1f;
    private static final float START_ZOOM_LEVEL = 11f;

    private ClusterManager<WeatherUI> mClusterManager;
    private GoogleMap mMap;
    private MarkerHelper<WeatherUI> mMarkerHelper;

    private ImageButton mLayerSwitch;
    private ViewGroup mWeatherSummaryView;
    private TextView mWeatherCount;
    private View mSummaryView;
    ForecastAdapter.ViewHolder mViewHolder;
    private int cityId = -1;

    private boolean loading = false;
    private boolean cameraChanged = false;
    private LatLng lastLatLng;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_map);

        this.findViewById(R.id.current_location).setOnClickListener(this);
        mLayerSwitch = (ImageButton) this.findViewById(R.id.layer_switch);
        mLayerSwitch.setOnClickListener(this);

        mWeatherSummaryView = (ViewGroup)this.findViewById(R.id.weather_summary);
        mWeatherSummaryView.setVisibility(View.GONE);
        mSummaryView = mWeatherSummaryView.getChildAt(0);
        if(mSummaryView!=null) {
            mSummaryView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onWeatherSummaryClicked(view);
                }
            });
        }
        mWeatherCount = (TextView)findViewById(R.id.weather_count);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        setSwipeRefreshEnabled(false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private void setUpMapIfNeeded() {
        if (mMap != null) {
            return;
        }
        mMap = ((MapFragment) getFragmentManager().
                findFragmentById(R.id.map)).getMap();
        if (mMap != null) {
            startMap();
        }
        // Initialize map options. For example:
        // mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
    }

    private void startMap() {
        mMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
            @Override
            public void onMyLocationChange(Location location) {
                mMap.setOnMyLocationChangeListener(null);
                animateCameraToMyLocation(location, START_ZOOM_LEVEL);
            }
        });
        mMap.setMyLocationEnabled(true);
        UiSettings uiSettings = mMap.getUiSettings();
        uiSettings.setMyLocationButtonEnabled(false);

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                showWeatherSummary(false);
            }
        });

        mMarkerHelper = new MarkerHelper<>();
        mClusterManager = new ClusterManager<>(this.getApplicationContext(), mMap);
        //comment renderer will use default renderer
        WeatherClusterRenderer<WeatherUI> renderer =
                new WeatherClusterRenderer<WeatherUI>(this.getApplicationContext(), mMap, mClusterManager);
        mClusterManager.setRenderer(renderer);

        mClusterManager.setAlgorithm(
                new PreCachingAlgorithmDecorator<WeatherUI>(new GridBasedAlgorithm<WeatherUI>()));
        mMap.setOnMarkerClickListener(mClusterManager);
        mClusterManager.setOnClusterClickListener(new ClusterManager.OnClusterClickListener<WeatherUI>() {
            @Override
            public boolean onClusterClick(Cluster<WeatherUI> weatherCluster) {
                return onClusterClicked(weatherCluster);
            }
        });
        mClusterManager.setOnClusterItemClickListener(new ClusterManager.OnClusterItemClickListener<WeatherUI>() {
            @Override
            public boolean onClusterItemClick(WeatherUI weather) {
                onWeatherClicked(weather);
                return false;
            }
        });
        mMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
                mClusterManager.onCameraChange(cameraPosition);

                VisibleRegion visibleRegion = mMap.getProjection().getVisibleRegion();
                //The smallest bounding box that includes the visible region defined in this class.
                LatLngBounds bounds = visibleRegion.latLngBounds;

                Timber.i("bounds.northest: %s bounds.southwest: %s", bounds.northeast, bounds.southwest);

                final LatLng latLng = mMap.getCameraPosition().target;

                //if task is running only execute recent one
                if (loading == true) {
                    cameraChanged = true;
                    lastLatLng = latLng;
                    return;
                }
//               new OpenWeatherClient().doMapSearch(latLng);
//                new ShowWeatherDataOnMapTask().execute(latLng);
                doMapSearch(latLng);
            }
        });
    }

    public void doMapSearch(LatLng latLng) {
        onRefreshingStateChanged(true);

        /**
         * an algorithm to prevent fast drag
         *
         * set loading = true when start to load data
         * if loading = true set cameraChanged = true instead of load again
         * after load set loading = false
         * if cameraChanged load and set camerachanged = false
         */
        loading = true;

        OpenWeatherClient openWeatherClient = new OpenWeatherClient();
        openWeatherClient.doMapSearch(latLng);
    }

    @Subscribe
    public void onMapSearchReturn(MapSearchEvent event){
        if(event.result != null) {
            onSearchResult(event.result);
        }
        onRefreshingStateChanged(false);
        checkCameraChange();
    }

    private void checkCameraChange() {
        loading = false;
        if(cameraChanged == true) {
            cameraChanged = false;
            if(lastLatLng!=null)
            doMapSearch(lastLatLng);
        }

    }

    public void onSearchResult(List<CurrentWeather> findResponse) {
        if(findResponse!=null) {
            List<WeatherUI> weatherUIs = new ArrayList<>();
            for(CurrentWeather forecast : findResponse){
                weatherUIs.add(new WeatherUI(forecast));
            }
            processWeatherUI(weatherUIs);
        } else {
            mMarkerHelper.clear();
            mClusterManager.clearItems();
            mClusterManager.cluster();
        }
    }

    private void processWeatherUI(List<WeatherUI> weatherUIs) {
        Timber.i("process WeatherUI %s", weatherUIs);
        MarkerHelper.MarkerState<WeatherUI> markerState = mMarkerHelper.setItems(weatherUIs);
        for(WeatherUI removedWeatherItem : markerState.getRemovedItems()) {
            mClusterManager.removeItem(removedWeatherItem);
        }
//        mClusterManager.clearItems();
        mClusterManager.addItems(markerState.getNewItems());
        mClusterManager.onCameraChange(mMap.getCameraPosition());
        mClusterManager.cluster();
    }

//    private void showCurrentPlace() {
//        Log.i(TAG, "Latitude:" + mCurrentLocation.getLatitude());
//        final LatLng currentLatLng = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
//        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 10));
//
//        mClusterManager = new ClusterManager<>(this, mMap);
//        WeatherUI myItem = new WeatherUI(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
//        mClusterManager.addItem(myItem);
//        mMap.setOnCameraChangeListener(mClusterManager);
//
//
//        //TODO getItem from open weather
//        new ShowWeatherDataOnMapTask().execute(currentLatLng);
//
//    }

    public void animateCameraToMyLocation(Location location, float zoom) {
        if (location != null) {
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(
                    new LatLng(location.getLatitude(), location.getLongitude()),
                    zoom != DO_NOT_CHANGE_ZOOM ? zoom : mMap.getCameraPosition().zoom
            );
            mMap.animateCamera(cameraUpdate);
        }
    }

    public void onWeatherClicked(WeatherUI weatherUI) {
        //TODO show weather detail
        cityId = weatherUI.getCityId();
        renderSummaryView(weatherUI);
        showWeatherSummary(true);
//        Toast.makeText(this, weatherUI.getCityName() + " weather is:" + weatherUI.getDescription(), Toast.LENGTH_SHORT).show();
        Timber.i("%s weater is: %s", weatherUI.getCityName(),weatherUI.getDescription());
    }

    private void renderSummaryView(WeatherUI weatherUI) {
        mViewHolder = new ForecastAdapter.ViewHolder(mSummaryView);
        mViewHolder.iconView.setImageResource(Utility.getIconResourceForWeatherCondition(
                weatherUI.getWeatherId()));
        String dateString = new Date().toString();
        mViewHolder.dateView.setText(dateString);

        String description = weatherUI.getDescription();
        mViewHolder.descriptionView.setText(description);

        mViewHolder.iconView.setContentDescription(description);

        boolean isMetric = Utility.isMetric(this);

        double high = weatherUI.getHigh();
        mViewHolder.highTempView.setText(Utility.formatTemperature(this, high));

        double low = weatherUI.getLow();
        mViewHolder.lowTempView.setText(Utility.formatTemperature(this, low));
    }

    private boolean onClusterClicked(Cluster<WeatherUI> weatherCluster) {
        //TODO list result city weathers here can find by multiple city id api
        Toast.makeText(this, "onClusterClicked", Toast.LENGTH_SHORT).show();
        Timber.i("on cluster clicked");
        return true;
    }


    private boolean showWeatherSummary(final boolean show) {
        int viewState = show ? View.VISIBLE : View.GONE;
        if(viewState == mWeatherSummaryView.getVisibility()) {
            return false;
        } else {
            Animation animation = AnimationUtils.loadAnimation(this,
                    show ? R.anim.in_bottom : R.anim.out_bottom);
            mWeatherSummaryView.clearAnimation();
            animation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    if(show) mWeatherSummaryView.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    if(!show) mWeatherSummaryView.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {}
            });
            mWeatherSummaryView.startAnimation(animation);
            return true;
        }
    }



    @Override
    public void onClick(View view) {
        switch(view.getId()) {
            case R.id.current_location:
                animateCameraToMyLocation(mMap.getMyLocation(), DO_NOT_CHANGE_ZOOM);
                break;

            case R.id.layer_switch:
                switchLayers();
                break;
        }
    }

    private void switchLayers() {
        int mapType = mMap.getMapType();
        boolean isSatellite = mapType == GoogleMap.MAP_TYPE_SATELLITE;
        mMap.setMapType(isSatellite ?
                        GoogleMap.MAP_TYPE_NORMAL :
                        GoogleMap.MAP_TYPE_SATELLITE
        );
        mLayerSwitch.setImageResource(isSatellite ?
                        R.drawable.ic_map_control_satellite :
                        R.drawable.ic_map_controll_standard

        );
    }

    private void onWeatherSummaryClicked(View view) {
        Intent intent = new Intent(this,MainActivity.class).putExtra(Constant.CITY_ID,cityId);
        startActivity(intent);
    }
}