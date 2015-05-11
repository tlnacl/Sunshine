package com.example.android.sunshine.app.ui;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.SearchRecentSuggestions;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

import com.example.android.sunshine.app.R;
import com.example.android.sunshine.app.data.SuggestionProvider;
import com.example.android.sunshine.app.events.SearchByCityNameEvent;
import com.example.android.sunshine.app.models.CurrentWeather;
import com.example.android.sunshine.app.network.OpenWeatherClient;
import com.squareup.otto.Subscribe;

import java.util.List;

/**
 * Created by tomtang on 7/05/15.
 */
public class SearchActivity extends BaseActivity {
    public static final String QUERY_KEY = "query";

    private ListView mSearchListView;
    private SearchResultAdapter mAdapter;
    private OpenWeatherClient mClient;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        mClient = new OpenWeatherClient();
        mSearchListView = (ListView) findViewById(R.id.search_list);

        if (getIntent() != null) {
            handleIntent(getIntent());
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        handleIntent(intent);
    }

    /**
     * Assuming this activity was started with a new intent, process the incoming information and
     * react accordingly.
     * @param intent
     */
    private void handleIntent(Intent intent) {
        // Special processing of the incoming intent only occurs if the if the action specified
        // by the intent is ACTION_SEARCH.
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            // SearchManager.QUERY is the key that a SearchManager will use to send a query string
            // to an Activity.
            String query = intent.getStringExtra(SearchManager.QUERY);
            if(query.length()<3) return;
            SearchRecentSuggestions suggestions = new SearchRecentSuggestions(this,
                    SuggestionProvider.AUTHORITY, SuggestionProvider.MODE);
            suggestions.saveRecentQuery(query, null);
            mClient.doCityWeatherSearch(query);
        }
    }

    @Subscribe
    public void onSearchByCityNameReturn(SearchByCityNameEvent event){
        if(mAdapter == null){
            mAdapter = new SearchResultAdapter(this,event.result);
            mSearchListView.setAdapter(mAdapter);
        } else {
            mAdapter.setData(event.result);
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.search, menu);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView =
                (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName()));

        return true;
    }

    class SearchResultAdapter extends BaseAdapter {
        private Context mContext;
        private List<CurrentWeather> mCurrentWeathers;

        public SearchResultAdapter(Context context, List<CurrentWeather> weathers) {
            mContext = context;
            mCurrentWeathers = weathers;
        }

        public void setData(List<CurrentWeather> weathers){
            mCurrentWeathers = weathers;
        }

        @Override
        public int getCount() {
            return mCurrentWeathers.size();
        }

        @Override
        public Object getItem(int position) {
            return mCurrentWeathers.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            SearchResultHolder holder;
            if(convertView == null){
                convertView = LayoutInflater.from(mContext).inflate(R.layout.item_search_result,parent,false);
                 holder = new SearchResultHolder(convertView);
                convertView.setTag(holder);
            }else{
                holder = (SearchResultHolder) convertView.getTag();
            }
            final CurrentWeather weather = mCurrentWeathers.get(position);
            holder.bind(weather.getCityName(),weather.getCountry(),weather.getTemp());
            return convertView;
        }
    }

    class SearchResultHolder {
        private TextView mSearchResult;

        public SearchResultHolder(View view){
            mSearchResult = (TextView) view.findViewById(R.id.search_result);
        }

        public void bind(String city, String country, float temp){
            mSearchResult.setText(city + "," + country + ",  " + temp);
        }

    }


//    SearchRecentSuggestions suggestions = new SearchRecentSuggestions(this,
//            HelloSuggestionProvider.AUTHORITY, HelloSuggestionProvider.MODE);
//    suggestions.clearHistory();
}
