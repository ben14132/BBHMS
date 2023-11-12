package com.example.mygooglemaps;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.FragmentManager;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import android.app.AlertDialog;
import android.content.DialogInterface;


import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.os.AsyncTask;
import android.util.Log;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ParkingTrendFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ParkingTrendFragment extends Fragment{

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_LAT = "latitude";
    private static final String ARG_LNG = "longitude";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private char v;
    private double lat;
    private double lng;

    public static ArrayList<String> labels = new ArrayList<>();
    public static ArrayList<Integer> lots = new ArrayList<>();


    public ParkingTrendFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *

     * @return A new instance of fragment ParkingTrendFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ParkingTrendFragment newInstance(char veh, double lat, double lng) {
        ParkingTrendFragment fragment = new ParkingTrendFragment();
        Bundle args = new Bundle();
        args.putDouble(ARG_LAT, lat);
        args.putDouble(ARG_LNG, lng);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        if (getArguments() != null) {
            lat = getArguments().getDouble(ARG_LAT);
            lng = getArguments().getDouble(ARG_LNG);
            v = getArguments().getChar("v");
        }


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_parking_trend, container, false);

        Button back = view.findViewById(R.id.btnBack);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
                fragmentManager.popBackStack();
            }
        });


        ParkingOccupancy po = (ParkingOccupancy) new ParkingOccupancy(v, lat, lng, new ParkingOccupancy.AsyncResponse(){
            @Override
            public void processFinish(ResultSet rs){
                //data processing code will be written here
                try{
                    labels.clear();
                    lots.clear();
                    while (rs.next()) {
                        int occupancy = rs.getInt("available_lots");
                        String hour = rs.getString("hour");
                        String dates;
                        if (hour.length() == 1){
                            dates = "0" + hour + "00H";
                        }
                        else{
                            dates = hour + "00H";
                        }
                        labels.add(dates);
                        lots.add(occupancy);
                    }
                    int peakIndex = 0;
                    int peak = 0;
                    for (int a=0; a<labels.size(); a++){
                        if(a==0){
                            peak = lots.get(a);
                            peakIndex = a;
                        }
                        else {
                            if (lots.get(a) < peak){
                                peak = lots.get(a);
                                peakIndex = a;
                            }
                        }
                    }

                    TextView tv1 = (TextView) view.findViewById(R.id.peakHour);
                    tv1.setText("Peak Hour: " + labels.get(peakIndex));

                    BarChart barChart = (BarChart) view.findViewById(R.id.barChart);
                    CustomMarkerView markerView = new CustomMarkerView(requireContext(), R.layout.custom_marker_layout);
                    markerView.setChartView(barChart);
                    barChart.setMarker(markerView);
                    // Sample data: strings and their corresponding integer values
                    ArrayList<BarEntry> entries = new ArrayList<>();
                    for (int i=0; i<labels.size(); i++){
                        entries.add(new BarEntry(i, lots.get(i)));
                    }
                    BarDataSet barDataSet = new BarDataSet(entries, "Average Available Lots");
                    BarData barData = new BarData(barDataSet);
                    barChart.setData(barData);

                    String description = "Average Available Lots vs Time";
                    barChart.getDescription().setText(description);

                    XAxis xAxis = barChart.getXAxis();

                    xAxis.setValueFormatter(new IndexAxisValueFormatter(labels)); // Set labels on X-axis
                    xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
                    xAxis.setGranularity(1f); // Set granularity to 1 to show all labels\
                    xAxis.setGranularityEnabled(true);

                    barChart.setFitBars(true); // Makes the bars fit the width of the chart
                    barChart.invalidate(); // Refresh the chart
//                    barChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
//                        @Override
//                        public void onValueSelected(Entry e, Highlight h) {
//                            // Show a dialog with information about the selected bar
//                            float f = e.getX();
//                            showCustomPopupDialog(e.getY(), Float.toString(f));
//                        }
//
//                        @Override
//                        public void onNothingSelected() {
//                            // Handle when nothing is selected
//                        }
//                    });


                }
                catch (SQLException e) {
                    Log.e("InfoAsyncTask", "Error reading information", e);
                }
            }

        }).execute();



        return view;
    }

    private void showBarInfoDialog(float value, String label) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Bar Information");
        builder.setMessage("Value: " + value + "\nLabel: " + label);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // Handle OK button click if needed
            }
        });
        builder.create().show();
    }

    private void showCustomPopupDialog(float value, String label) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View popupView = getLayoutInflater().inflate(R.layout.custom_popup_layout, null);

        TextView popupTextView = popupView.findViewById(R.id.popupTextView);
        popupTextView.setText("Value: " + value + "\nLabel: " + label);

        builder.setView(popupView);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // Handle OK button click if needed
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }
}

