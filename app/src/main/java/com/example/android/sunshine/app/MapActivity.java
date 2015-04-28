package com.example.android.sunshine.app;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.sunshine.app.network.RetrofitHelper;
import com.example.android.sunshine.app.network.RetrofitWeatherData;
import com.example.android.sunshine.app.ui.WeatherUI;
import com.example.android.sunshine.app.utils.WeatherClusterRenderer;
import com.example.android.sunshine.app.utils.helper.MarkerHelper;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.VisibleRegion;
import com.google.gson.JsonObject;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.algo.GridBasedAlgorithm;
import com.google.maps.android.clustering.algo.PreCachingAlgorithmDecorator;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


/**
 * Created by tlnacl on 15/12/14.
 */
public class MapActivity extends BaseActivity implements View.OnClickListener{

    public static final float DO_NOT_CHANGE_ZOOM = -1f;
    private static final float START_ZOOM_LEVEL = 11f;

    private static final String CITY_ID = "cityId";

    private ClusterManager<WeatherUI> mClusterManager;

    private final String TAG = MapActivity.class.getSimpleName();
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
        mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
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

                Log.i(TAG,"bounds.northeast:"+bounds.northeast+"  bounds.southwest:"+bounds.southwest);

                final LatLng latLng = mMap.getCameraPosition().target;

                //if task is running only execute recent one
                if(loading == true) {
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

        Map<String,String> options = new HashMap<>();
        options.put("mode", "json");
        options.put("units", "metric");
        options.put("cnt", "10");
        options.put("lat", String.valueOf(latLng.latitude));
        options.put("lon", String.valueOf(latLng.longitude));

        /**
         * an algorithm to prevent fast drag
         *
         * set loading = true when start to load data
         * if loading = true set cameraChanged = true instead of load again
         * after load set loading = false if cameraChanged
         */
        loading = true;

        RetrofitHelper.getServerApi().weatherMapSearch(options, new Callback<JsonObject>() {
            @Override
            public void success(JsonObject jsonObject, Response response) {
                Log.i(TAG,jsonObject.toString());
                onSearchResult(jsonObject);
                onRefreshingStateChanged(false);
                checkCameraChange();
            }

            @Override
            public void failure(RetrofitError error) {
                Log.i(TAG,"###########robospice exception"+error.getMessage());
//            spiceException.printStackTrace();
                onRefreshingStateChanged(false);
                checkCameraChange();
            }
        });
    }

    private void checkCameraChange() {
        loading = false;
        if(cameraChanged == true) {
            cameraChanged = false;
            if(lastLatLng!=null)
            doMapSearch(lastLatLng);
        }

    }

//    @Subscribe
//    public void onMapSearchFinished(MapSearchEvent event){
//        onSearchResult(event.result);
//    }

//    public class ShowWeatherDataOnMapTask extends AsyncTask<LatLng,Void,List<WeatherUI>> {
//        private final String LOG_TAG = ShowWeatherDataOnMapTask.class.getSimpleName();
//
//        @Override
//        protected void onPreExecute() {
//            working = true;
//        }
//
//        @Override
//        protected List<WeatherUI> doInBackground(LatLng... latLngs) {
//            List<WeatherUI> weathers = WeatherData.getWeatherDataByLatLng(latLngs[0]);
//            return  weathers;
//        }
//
//        @Override
//        protected void onPostExecute(List<WeatherUI> weathers) {
//            onSearchResult(weathers);
//            working = false;
//        }
//    }



    public void onSearchResult(JsonObject findResponse) {
        if(findResponse!=null) {
            processWeatherUI(RetrofitWeatherData.parseWeatherData(findResponse));
        } else {
            mMarkerHelper.clear();
            mClusterManager.clearItems();
            mClusterManager.cluster();
        }
    }

    private void processWeatherUI(List<WeatherUI> weatherUIs) {
        Log.i(TAG,"process WeatherUI"+weatherUIs);
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
        //TODO list result city weathers
        Toast.makeText(this, "onClusterClicked", Toast.LENGTH_SHORT).show();
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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.map, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        } else if (id == R.id.action_main) {
            startActivity(new Intent(this, MainActivity.class));
        } else if (id == R.id.action_camera) {
            startActivity(new Intent(this, CameraActivity.class));
        }
        return super.onOptionsItemSelected(item);
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
        Intent intent = new Intent(this,MainActivity.class).putExtra(CITY_ID,cityId);
        startActivity(intent);
    }
}