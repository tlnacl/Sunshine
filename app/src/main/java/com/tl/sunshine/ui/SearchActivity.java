package com.tl.sunshine.ui;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.SearchRecentSuggestions;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

import com.tl.sunshine.Constant;
import com.tl.sunshine.R;
import com.tl.sunshine.data.SuggestionProvider;
import com.tl.sunshine.events.SearchByCityNameEvent;
import com.tl.sunshine.models.CurrentWeather;
import com.tl.sunshine.network.OpenWeatherClient;
import com.squareup.otto.Subscribe;

import java.util.List;

import static com.tl.sunshine.data.SuggestionProvider.AUTHORITY;

/**
 * Created by tomtang on 7/05/15.
 */
public class SearchActivity extends BaseActivity implements SearchView.OnQueryTextListener{
    public static final String TAG = SearchActivity.class.getSimpleName();
    public static final String QUERY_KEY = "query";

    private ListView mSearchListView;
    private SearchResultAdapter mAdapter;
    private OpenWeatherClient mClient;
    private String mquery;
    private boolean mLoading;
    private boolean mQueryChanged;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        mClient = new OpenWeatherClient();
        mSearchListView = (ListView) findViewById(R.id.search_list);

        mLoading = false;
        mQueryChanged = false;
    }

//    @Override
//    protected void onNewIntent(Intent intent) {
//        handleIntent(intent);
//    }

    private void handleQuery(String query) {
        if (!TextUtils.isEmpty(query) && query.length() > 2) {
            mLoading = true;
            mClient.doCityWeatherSearch(query);
        } else {
            //clean list view
        }
    }

    @Subscribe
    public void onSearchByCityNameReturn(SearchByCityNameEvent event){
        mLoading = false;
        if(mQueryChanged){
            handleQuery(mquery);
            mQueryChanged = false;
        }
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
        searchView.setOnQueryTextListener(this);

        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String query) {
            mquery = query;
            if(mLoading){
                mQueryChanged = true;
            }else {
                handleQuery(query);
            }
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
            holder.bind(weather.getCityId(),weather.getCityName(),weather.getCountry(),weather.getTemp());
            return convertView;
        }
    }

    class SearchResultHolder {
        private TextView mSearchResult;

        public SearchResultHolder(View view){
            mSearchResult = (TextView) view.findViewById(R.id.search_result);
        }

        public void bind(final int cityId, String city, String country, float temp){
            mSearchResult.setText(city + "," + country);
            mSearchResult.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //save search history
                    SearchRecentSuggestions suggestions = new SearchRecentSuggestions(SearchActivity.this,
                            AUTHORITY, SuggestionProvider.MODE);
                    suggestions.saveRecentQuery(mquery, null);

                    Intent i = new Intent(SearchActivity.this,MainActivity.class);
                    i.putExtra(Constant.CITY_ID,cityId);
                    startActivity(i);

                }
            });
        }

    }


//    SearchRecentSuggestions suggestions = new SearchRecentSuggestions(this,
//            HelloSuggestionProvider.AUTHORITY, HelloSuggestionProvider.MODE);
//    suggestions.clearHistory();
}
