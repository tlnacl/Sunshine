package com.example.android.sunshine.app.ui;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.provider.SearchRecentSuggestions;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.sunshine.app.Constant;
import com.example.android.sunshine.app.R;
import com.example.android.sunshine.app.data.SuggestionProvider;
import com.example.android.sunshine.app.models.CurrentWeather;
import com.example.android.sunshine.app.network.OpenWeatherClient;
import com.example.android.sunshine.app.ui.widgets.Truss;
import com.example.android.sunshine.app.utils.Utility;

import java.util.List;

import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static com.example.android.sunshine.app.data.SuggestionProvider.AUTHORITY;

/**
 * Created by tomtang on 7/05/15.
 */
public class SearchActivity extends BaseActivity implements SearchView.OnQueryTextListener{
    public static final String TAG = SearchActivity.class.getSimpleName();
    public static final String QUERY_KEY = "query";

    private ListView mSearchListView;
    private SearchResultAdapter mAdapter;
    private String mquery;
    private boolean mLoading;
    private boolean mQueryChanged;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
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
            mCompositeSubscription.add(OpenWeatherClient.doCityWeatherSearch(query)
                    .subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(new Subscriber<List<CurrentWeather>>() {
                @Override
                public void onCompleted() {

                }

                @Override
                public void onError(Throwable e) {

                }

                @Override
                public void onNext(List<CurrentWeather> weathers) {
                    onSearchByCityNameReturn(weathers);
                }
            }));
        } else {
            //clean list view
        }
    }

    private void onSearchByCityNameReturn(List<CurrentWeather> weathers){
        mLoading = false;
        if(mQueryChanged){
            handleQuery(mquery);
            mQueryChanged = false;
        }
        if(mAdapter == null){
            mAdapter = new SearchResultAdapter(this,weathers);
            mSearchListView.setAdapter(mAdapter);
        } else {
            mAdapter.setData(weathers);
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
            final String tempString = Utility.formatTemperature(SearchActivity.this, temp);

            Truss truss = new Truss();
            truss.pushSpan(new StyleSpan(Typeface.BOLD));
            truss.append(city).append(",");
            truss.popSpan();
            truss.pushSpan(new ForegroundColorSpan(Color.BLUE));
            truss.append(country).append(",");

            truss.pushSpan(new RelativeSizeSpan(1.5f));
            truss.append(tempString);
            truss.popSpan();

//            ClickableSpan clickfor = new ClickableSpan() {
//
//                @Override
//                public void onClick(View widget) {
//                    Toast.makeText(SearchActivity.this, "hello clicked for", Toast.LENGTH_LONG).show();
//                }
//            };
//            text.setSpan(clickfor,city.length()+country.length()+2,text.length(),Spanned.SPAN_INCLUSIVE_INCLUSIVE);
            mSearchResult.setText(truss.build());

            mSearchResult.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //save search history
                    SearchRecentSuggestions suggestions = new SearchRecentSuggestions(SearchActivity.this,
                            AUTHORITY, SuggestionProvider.MODE);
                    suggestions.saveRecentQuery(mquery, null);

                    Intent i = new Intent(SearchActivity.this, MainActivity.class);
                    i.putExtra(Constant.CITY_ID, cityId);
                    startActivity(i);

                }
            });
        }

    }


//    SearchRecentSuggestions suggestions = new SearchRecentSuggestions(this,
//            HelloSuggestionProvider.AUTHORITY, HelloSuggestionProvider.MODE);
//    suggestions.clearHistory();
}
