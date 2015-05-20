/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.sunshine.app.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.android.sunshine.app.R;
import com.example.android.sunshine.app.data.sharedpreference.SharedPreferenceHelper;
import com.example.android.sunshine.app.models.WeatherDetail;
import com.example.android.sunshine.app.models.WeatherForecast;
import com.example.android.sunshine.app.network.OpenWeatherClient;
import com.example.android.sunshine.app.network.RetrofitHelper;
import com.example.android.sunshine.app.sync.SunshineSyncAdapter;

import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * Encapsulates fetching the forecast and displaying it as a {@link ListView} layout.
 * http://stackoverflow.com/questions/12009895/loader-restarts-on-orientation-change
 */
public class ForecastFragment extends BaseFragment {
    public static final String LOG_TAG = ForecastFragment.class.getSimpleName();
    private ForecastAdapter mForecastAdapter;

    private String mCityId;
    private ListView mListView;
    private int mPosition = ListView.INVALID_POSITION;
    private boolean mUseTodayLayout;

    private static final String SELECTED_KEY = "selected_position";

    private final CompositeSubscription mCompositeSubscription = new CompositeSubscription();

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callback {
        /**
         * DetailFragmentCallback for when an item has been selected.
         */
        public void onItemSelected(WeatherDetail date);
    }

    public ForecastFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Add this line in order for this fragment to handle menu events.
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // The ArrayAdapter will take data from a source and
        // use it to populate the ListView it's attached to.
        mForecastAdapter = new ForecastAdapter(getActivity());

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        // Get a reference to the ListView, and attach this adapter to it.
        mListView = (ListView) rootView.findViewById(R.id.listview_forecast);
        mListView.setAdapter(mForecastAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                List<WeatherDetail> weathers = mForecastAdapter.getWeathers();
                WeatherDetail weather = weathers.get(position);
                if (weather != null) {
                    ((Callback) getActivity())
                            .onItemSelected(weather);
                }
                mPosition = position;
            }
        });

        // If there's instance state, mine it for useful information.
        // The end-goal here is that the user never knows that turning their device sideways
        // does crazy lifecycle related things.  It should feel like some stuff stretched out,
        // or magically appeared to take advantage of room, but data or place in the app was never
        // actually *lost*.
        if (savedInstanceState != null && savedInstanceState.containsKey(SELECTED_KEY)) {
            // The listview probably hasn't even been populated yet.  Actually perform the
            // swapout in onLoadFinished.
            mPosition = savedInstanceState.getInt(SELECTED_KEY);
        }

        mForecastAdapter.setUseTodayLayout(mUseTodayLayout);
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        // If we want to run the same query we use initLoader, if we want to run a different query we use restartLoader.
        if (mCityId == null || mCityId.equals(SharedPreferenceHelper.getPreferredLocation(getActivity()))) {
            //get from store
            final WeatherForecast forecast = SharedPreferenceHelper.getPreferedWeatherForecast();
            if(forecast != null) {
                mForecastAdapter.setWeathers(forecast.getWeather());
                mForecastAdapter.notifyDataSetChanged();
            }else {
                SunshineSyncAdapter.syncImmediately(getActivity());
            }
        }else {
            //get from network
            mCompositeSubscription.add(OpenWeatherClient.getForcastByCityAsync(Integer.parseInt(mCityId))
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.newThread())
            .subscribe(new Subscriber<WeatherForecast>() {
                @Override
                public void onCompleted() {

                }

                @Override
                public void onError(Throwable e) {

                }

                @Override
                public void onNext(WeatherForecast forecast) {
                    mForecastAdapter.setWeathers(forecast.getWeather());
                    mForecastAdapter.notifyDataSetChanged();
                }
            }));
        }
        super.onActivityCreated(savedInstanceState);
    }

    private void updateWeather() {
        SunshineSyncAdapter.syncImmediately(getActivity());
    }

//    private void openPreferredLocationInMap() {
//        // Using the URI scheme for showing a location found on a map.  This super-handy
//        // intent can is detailed in the "Common Intents" page of Android's developer site:
//        // http://developer.android.com/guide/components/intents-common.html#Maps
//        if (null != mForecastAdapter) {
//            Cursor c = mForecastAdapter.getCursor();
//            if (null != c) {
//                c.moveToPosition(0);
//                String posLat = c.getString(COL_COORD_LAT);
//                String posLong = c.getString(COL_COORD_LONG);
//                Uri geoLocation = Uri.parse("geo:" + posLat + "," + posLong);
//
//                Intent intent = new Intent(Intent.ACTION_VIEW);
//                intent.setData(geoLocation);
//
//                if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
//                    startActivity(intent);
//                } else {
//                    Log.d(LOG_TAG, "Couldn't call " + geoLocation.toString() + ", no receiving apps installed!");
//                }
//            }
//
//        }
//    }

//    @Override
//    public void onResume() {
//        super.onResume();
//        if (mCityId != null && !mCityId.equals(Utility.getPreferredLocation(getActivity()))) {
//            getLoaderManager().restartLoader(FORECAST_LOADER, null, this);
//        }
//    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // When tablets rotate, the currently selected list item needs to be saved.
        // When no item is selected, mPosition will be set to Listview.INVALID_POSITION,
        // so check for that before storing.
        if (mPosition != ListView.INVALID_POSITION) {
            outState.putInt(SELECTED_KEY, mPosition);
        }
        super.onSaveInstanceState(outState);
    }

    public void setUseTodayLayout(boolean useTodayLayout) {
        mUseTodayLayout = useTodayLayout;
        if (mForecastAdapter != null) {
            mForecastAdapter.setUseTodayLayout(mUseTodayLayout);
        }
    }

    public void setCityId(String cityId) {
        mCityId = cityId;
    }

    @Override
    public void onDetach() {
        mCompositeSubscription.unsubscribe();
        super.onDetach();
    }
}
