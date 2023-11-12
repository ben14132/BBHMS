package com.example.mygooglemaps;

import android.content.Context;
import android.widget.TextView;
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;

public class CustomMarkerView extends MarkerView {

    private TextView tvContent;

    public CustomMarkerView(Context context, int layoutResource) {
        super(context, layoutResource);
        // Initialize your UI components
        tvContent = findViewById(R.id.tvContent);
    }

    @Override
    public void refreshContent(Entry e, Highlight highlight) {
        // Set the content of the MarkerView based on the selected data point
        Float f = e.getX();
        int i = Math.round(f);
        int lots = Math.round(e.getY());
        String time;
        if (f<10){
            time = "0" + Integer.toString(i) + "00H";
        }
        else{
            time = Integer.toString(i) + "00H";
        }
        tvContent.setText("Avaiable Lots: " + lots + "\nTime: " + time);
        super.refreshContent(e, highlight);
    }
}
