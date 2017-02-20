package com.sam_chordas.android.stockhawk.ui;

import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.db.chart.Tools;
import com.db.chart.model.LineSet;
import com.db.chart.view.AxisController;
import com.db.chart.view.LineChartView;
import com.db.chart.view.animation.Animation;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;

/**
 * Created by DELL on 8/11/2016.
 */

public class MyStocksDetailActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int CURSOR_LOADER_ID = 0;
    private Cursor mCursor;
    private LineChartView lineChartView;
    private Context mContext;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_line_graph);

        mContext = getApplicationContext();
        lineChartView = (LineChartView) findViewById(R.id.linechart);

        Intent intent = getIntent();
        Bundle args = new Bundle();
        String symbol = intent.getStringExtra(getResources().getString(R.string.symbol));
        this.setTitle(symbol);
        args.putString(getResources().getString(R.string.symbol), symbol);
        getLoaderManager().initLoader(CURSOR_LOADER_ID, args, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this, QuoteProvider.Quotes.CONTENT_URI,
                new String[]{ QuoteColumns.BIDPRICE,QuoteColumns.PERCENT_CHANGE, QuoteColumns.CHANGE, QuoteColumns.ISUP},
                QuoteColumns.SYMBOL + " = ?",
                new String[]{args.getString(getResources().getString(R.string.symbol))},
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mCursor = data;
        if (mCursor.getCount() != 0)
            renderChart(mCursor);
    }

    public void renderChart(Cursor data) {
        LineSet lineSet = new LineSet();
        float minimumPrice = Float.MAX_VALUE;
        float maximumPrice = Float.MIN_VALUE;

        for (data.moveToFirst(); !data.isAfterLast(); data.moveToNext()) {

            String label = data.getString(data.getColumnIndexOrThrow(QuoteColumns.BIDPRICE));
            float price = Float.parseFloat(label);

            String priceChange = data.getString(data.getColumnIndexOrThrow(QuoteColumns.CHANGE));
            float change = Float.parseFloat(priceChange);

            float lastPrice = price - change;

            lineSet.addPoint(lastPrice + "" , lastPrice);
            lineSet.addPoint(price + "" ,  price);
            minimumPrice = Math.min(minimumPrice, price);
            maximumPrice = Math.max(maximumPrice, price);
        }

        lineSet.setColor(ContextCompat.getColor(mContext, R.color.line_stroke))
                .setFill(ContextCompat.getColor(mContext, R.color.line_set))
                .setDotsColor(ContextCompat.getColor(mContext, R.color.line_dots))
                .setThickness(4)
                .setDashed(new float[]{10f, 10f});


        lineChartView.setBorderSpacing(Tools.fromDpToPx(15))
                .setYLabels(AxisController.LabelPosition.OUTSIDE)
                .setXLabels(AxisController.LabelPosition.NONE)
                .setLabelsColor(ContextCompat.getColor(mContext, R.color.line_label))
                .setXAxis(false)
                .setYAxis(false)
                .setAxisBorderValues(Math.round(Math.max(0f, minimumPrice - 5f)), Math.round(maximumPrice + 5f))
                .addData(lineSet);

        Animation anim = new Animation();

        if (lineSet.size() > 1)
            lineChartView.show(anim);
        else
            Toast.makeText(this, "No data", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) { }
}
