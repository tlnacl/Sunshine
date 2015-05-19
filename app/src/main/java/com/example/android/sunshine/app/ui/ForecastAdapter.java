package com.example.android.sunshine.app.ui;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.sunshine.app.R;
import com.example.android.sunshine.app.data.sharedpreference.SharedPreferenceHelper;
import com.example.android.sunshine.app.models.WeatherDetail;
import com.example.android.sunshine.app.ui.widgets.BindableAdapter;
import com.example.android.sunshine.app.utils.Utility;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link ForecastAdapter} exposes a list of weather forecasts
 * from a {@link Cursor} to a {@link android.widget.ListView}.
 */
public class ForecastAdapter extends BindableAdapter<WeatherDetail> {

    private static final int VIEW_TYPE_COUNT = 2;
    private static final int VIEW_TYPE_TODAY = 0;
    private static final int VIEW_TYPE_FUTURE_DAY = 1;

    // Flag to determine if we want to use a separate view for "today".
    private boolean mUseTodayLayout = true;
    private Context mContext;
    private List<WeatherDetail> mWeathers;

    public ForecastAdapter(Context context) {
        super(context);
        mContext = context;
        mWeathers = new ArrayList<>();
    }

    public void setWeathers(List<WeatherDetail> weathers){
        mWeathers = weathers;
    }

    public List<WeatherDetail> getWeathers(){
        return mWeathers;
    }

    @Override
    public int getCount() {
        return mWeathers.size();
    }

    @Override
    public WeatherDetail getItem(int position) {
        return mWeathers.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View newView(LayoutInflater inflater, int position, ViewGroup container) {
        // Choose the layout type
        int viewType = getItemViewType(position);
        int layoutId = -1;
        switch (viewType) {
            case VIEW_TYPE_TODAY: {
                layoutId = R.layout.list_item_forecast_today;
                break;
            }
            case VIEW_TYPE_FUTURE_DAY: {
                layoutId = R.layout.list_item_forecast;
                break;
            }
        }

        View view = inflater.inflate(layoutId, container, false);

        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);

        return view;
    }

    @Override
    public void bindView(WeatherDetail item, int position, View view) {
        ViewHolder viewHolder = (ViewHolder) view.getTag();
        final WeatherDetail weather = mWeathers.get(position);
        int viewType = getItemViewType(position);
        switch (viewType) {
            case VIEW_TYPE_TODAY: {
                // Get weather icon
                viewHolder.iconView.setImageResource(Utility.getArtResourceForWeatherCondition(
                        weather.getWeatherId()));
                break;
            }
            case VIEW_TYPE_FUTURE_DAY: {
                // Get weather icon
                viewHolder.iconView.setImageResource(Utility.getIconResourceForWeatherCondition(
                        weather.getWeatherId()));
                break;
            }
        }

        // Read date from cursor
        String dateString = weather.getDataString();
        // Find TextView and set formatted date on it
        viewHolder.dateView.setText(Utility.getFriendlyDayString(mContext, dateString));

        // Read weather forecast from cursor
        String description = weather.getDescription();
        // Find TextView and set weather forecast on it
        viewHolder.descriptionView.setText(description);

        // For accessibility, add a content description to the icon field
        viewHolder.iconView.setContentDescription(description);

        // Read user preference for metric or imperial temperature units
        boolean isMetric = SharedPreferenceHelper.isMetric(mContext);

        // Read high temperature from cursor
        float high = weather.getHigh();
        viewHolder.highTempView.setText(Utility.formatTemperature(mContext, high));

        // Read low temperature from cursor
        float low = weather.getLow();
        viewHolder.lowTempView.setText(Utility.formatTemperature(mContext, low));
    }

    /**
     * Cache of the children views for a forecast list item.
     */
    public static class ViewHolder {
        public final ImageView iconView;
        public final TextView dateView;
        public final TextView descriptionView;
        public final TextView highTempView;
        public final TextView lowTempView;

        public ViewHolder(View view) {
            iconView = (ImageView) view.findViewById(R.id.list_item_icon);
            dateView = (TextView) view.findViewById(R.id.list_item_date_textview);
            descriptionView = (TextView) view.findViewById(R.id.list_item_forecast_textview);
            highTempView = (TextView) view.findViewById(R.id.list_item_high_textview);
            lowTempView = (TextView) view.findViewById(R.id.list_item_low_textview);
        }
    }

    public void setUseTodayLayout(boolean useTodayLayout) {
        mUseTodayLayout = useTodayLayout;
    }

    @Override
    public int getItemViewType(int position) {
        return (position == 0 && mUseTodayLayout) ? VIEW_TYPE_TODAY : VIEW_TYPE_FUTURE_DAY;
    }

    @Override
    public int getViewTypeCount() {
        return VIEW_TYPE_COUNT;
    }
}