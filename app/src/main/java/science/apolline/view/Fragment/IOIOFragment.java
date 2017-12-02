package science.apolline.view.Fragment;

import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import science.apolline.R;
import science.apolline.models.IntfSensorData;
import science.apolline.viewModel.SensorViewModel;
import science.apolline.models.IOIOData;
import science.apolline.service.sensor.IOIOService;



public class IOIOFragment extends Fragment implements LifecycleOwner,OnChartValueSelectedListener {


    private ProgressBar progressPM1;
    private ProgressBar progressPM2;
    private ProgressBar progressPM10;

    private TextView textViewPM1;
    private TextView textViewPM2;
    private TextView textViewPM10;

    private Button pieton;
    private Button velo;
    private Button voiture;
    private Button other;

    private MapFragment mapFragment;
    private GoogleMap map = null;

    private BroadcastReceiver BReceiver;

    private LiveData<IOIOData> dataLive;
    private LineChart mChart;
    private List<ILineDataSet> dataList;
    
    public IOIOFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState)  {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_ioio, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        progressPM1 = view.findViewById(R.id.fragment_ioio_progress_pm1);
        textViewPM1 = view.findViewById(R.id.fragment_ioio_tv_pm1_value);
        progressPM2 = view.findViewById(R.id.fragment_ioio_progress_pm2);
        textViewPM2 = view.findViewById(R.id.fragment_ioio_tv_pm2_value);
        progressPM10 = view.findViewById(R.id.fragment_ioio_progress_pm10);
        textViewPM10 = view.findViewById(R.id.fragment_ioio_tv_pm10_value);
        mChart = view.findViewById(R.id.chart1);
//        mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.fragment_ioio_map);
//        pieton = view.findViewById(R.id.fragment_ioio_pieton);
//        velo = view.findViewById(R.id.fragment_ioio_velo);
//        voiture = view.findViewById(R.id.fragment_ioio_voiture);
//        other = view.findViewById(R.id.fragment_ioio_other);
        
        dataList = createMultiSet();
        initGraph();
    }


    //init graph on create view
    private void initGraph(){

        // LineTimeChart
        mChart.setOnChartValueSelectedListener(this);
        // enable description text
        mChart.getDescription().setEnabled(false);


        mChart.setDragDecelerationFrictionCoef(0.9f);
        mChart.setHighlightPerDragEnabled(true);
        // set an alternative background color
//        mChart.setBackgroundColor(Color.WHITE);
//        mChart.setViewPortOffsets(0f, 0f, 0f, 0f);

        // enable touch gestures
        mChart.setTouchEnabled(true);
        // enable scaling and dragging
        mChart.setDragEnabled(true);
        mChart.setScaleEnabled(true);
        mChart.setDrawGridBackground(false);
        // if disabled, scaling can be done on x- and y-axis separately
        mChart.setPinchZoom(false);
        // set an alternative background color
        mChart.setBackgroundColor(Color.TRANSPARENT);


        LineData data = new LineData();
        data.setValueTextColor(Color.WHITE);
        // add empty data
        mChart.setData(data);

        mChart.invalidate();

//        // get the legend (only possible after setting data)
        Legend l = mChart.getLegend();
        // modify the legend ...
        l.setForm(Legend.LegendForm.LINE);
        l.setTypeface(Typeface.DEFAULT);
        l.setTextColor(Color.WHITE);
//        Legend l = mChart.getLegend();
//        l.setEnabled(false);

        XAxis xl = mChart.getXAxis();
        xl.setTypeface(Typeface.DEFAULT);
        xl.setTextColor(Color.BLACK);
        xl.setDrawGridLines(false);
        xl.setAvoidFirstLastClipping(true);
        xl.setEnabled(true);
        xl.setCenterAxisLabels(true);
        xl.setGranularity(1f); // one hour
        xl.setPosition(XAxis.XAxisPosition.TOP_INSIDE);

        xl.setValueFormatter(new IAxisValueFormatter() {
            private SimpleDateFormat mFormat = new SimpleDateFormat("dd MMM HH:mm");
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                long millis = TimeUnit.HOURS.toMillis((long) value);
                return mFormat.format(new Date(millis));
            }
        });

        YAxis leftAxis = mChart.getAxisLeft();
        leftAxis.setTypeface(Typeface.DEFAULT);
        leftAxis.setTextColor(Color.BLACK);
        leftAxis.setAxisMaximum(2500f);
        leftAxis.setAxisMinimum(0f);
//        leftAxis.setSpaceTop(80);
//        leftAxis.setSpaceBottom(20);

        leftAxis.setDrawGridLines(true);

        YAxis rightAxis = mChart.getAxisRight();
        rightAxis.setEnabled(false);

    }

    //Add new points to graph
    private void addEntry(int[] dataTosend ) {
        LineData data = mChart.getData();

        if (data != null) {
            if (data.getDataSetCount()!=3)
                    for (ILineDataSet temp : dataList) {
                        data.addDataSet(temp);
                    }
            }

            long now = TimeUnit.MILLISECONDS.toHours(System.currentTimeMillis());

            data.addEntry(new Entry(now, (float) dataTosend[0]), 0);
            data.addEntry(new Entry(now, (float) dataTosend[1]), 1);
            data.addEntry(new Entry(now, (float) dataTosend[2]), 2);

            data.notifyDataChanged();
            // let the chart know it's data has changed
            mChart.notifyDataSetChanged();
            // limit the number of visible entries
            mChart.setVisibleXRangeMaximum(24);
            // Sets the size of the area (range on the y-axis) that should be maximum visible at once
            mChart.setVisibleYRangeMaximum(500f, YAxis.AxisDependency.LEFT);
            // mChart.setVisibleYRange(30, AxisDependency.LEFT);
            // move to the latest entry
            mChart.moveViewToX(data.getEntryCount());
            // this automatically refreshes the chart (calls invalidate())
//             mChart.moveViewTo(data.getEntryCount() -7, 55f,
//             YAxis.AxisDependency.LEFT);

    }


    //Graph init
    private ArrayList<ILineDataSet> createMultiSet() {

        ArrayList<ILineDataSet> dataSets = new ArrayList<>();

                LineDataSet setPM1 = new LineDataSet(null, "PM1");
                setPM1.setAxisDependency(YAxis.AxisDependency.LEFT);
                setPM1.setColor(Color.BLUE);
                setPM1.setCircleColor(Color.BLUE);
                setPM1.setLineWidth(2f);
                setPM1.setCircleRadius(4f);
                setPM1.setFillAlpha(65);
                setPM1.setFillColor(Color.BLUE);
                setPM1.setHighLightColor(Color.rgb(244, 117, 117));
                setPM1.setValueTextColor(Color.BLUE);
                setPM1.setValueTextSize(9f);
                setPM1.setDrawValues(false);

                LineDataSet setPM2 = new LineDataSet(null, "PM2");
                setPM2.setAxisDependency(YAxis.AxisDependency.LEFT);
                setPM2.setColor(Color.GREEN);
                setPM2.setCircleColor(Color.GREEN);
                setPM2.setLineWidth(2f);
                setPM2.setCircleRadius(4f);
                setPM2.setFillAlpha(65);
                setPM2.setFillColor(Color.GREEN);
                setPM2.setHighLightColor(Color.rgb(244, 117, 117));
                setPM2.setValueTextColor(Color.GREEN);
                setPM2.setValueTextSize(9f);
                setPM2.setDrawValues(false);

                LineDataSet setPM10 = new LineDataSet(null, "PM10");
                setPM10.setAxisDependency(YAxis.AxisDependency.LEFT);
                setPM10.setColor(Color.YELLOW);
                setPM10.setCircleColor(Color.YELLOW);
                setPM10.setLineWidth(2f);
                setPM10.setCircleRadius(4f);
                setPM10.setFillAlpha(65);
                setPM10.setFillColor(Color.YELLOW);
                setPM10.setHighLightColor(Color.rgb(244, 117, 117));
                setPM10.setValueTextColor(Color.YELLOW);
                setPM10.setValueTextSize(9f);
                setPM10.setDrawValues(false);

                dataSets.add(setPM1);
                dataSets.add(setPM2);
                dataSets.add(setPM10);

        return dataSets;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        getActivity().startService(new Intent(getActivity(), IOIOService.class));
    }

    @Override
    public void onDetach() {
        super.onDetach();

    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        SensorViewModel viewModel = new SensorViewModel(getActivity().getApplication());
        LiveData<IntfSensorData> data = viewModel.getDataLive();
        data.observeForever(new Observer<IntfSensorData>() {
            @Override
            public void onChanged(@Nullable IntfSensorData sensorData) {
                Log.e("fragment", "onChanged");
                if (sensorData != null && sensorData.getClass() == IOIOData.class) {
                    Log.e("fragment", "if statement");
                    IOIOData data = (IOIOData) sensorData;
                    int PM01Value = data.getPM01Value();
                    int PM2_5Value = data.getPM2_5Value();
                    int PM10Value = data.getPM10Value();
                    progressPM1.setProgress(PM01Value);
                    progressPM2.setProgress(PM2_5Value);
                    progressPM10.setProgress(PM10Value);
                    textViewPM1.setText(PM01Value + "");
                    textViewPM2.setText(PM2_5Value + "");
                    textViewPM10.setText(PM10Value + "");
                    int dataToDisplay[] = {PM01Value,PM2_5Value,PM10Value};

                    addEntry(dataToDisplay);

                }
            }
        });
    }


    @Override
    public void onValueSelected(Entry e, Highlight h) {

    }

    @Override
    public void onNothingSelected() {

    }
}


