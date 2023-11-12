package com.example.mygooglemaps;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.util.Log;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MostSearchedAsync extends AsyncTask<Void, Void, List<Map<String, String>>> {

    private static final String URLC = "jdbc:mysql://172.21.146.188:3306/carparkDetailsC";
    private static final String URLM = "jdbc:mysql://172.21.146.188:3306/carparkDetailsM";
    private static final String URLH = "jdbc:mysql://172.21.146.188:3306/carparkDetailsH";
    private static final String USER = "VMuser";
    private static final String PASSWORD = "sc2006parku";

    public interface AsyncResponse {
        void processFinish(List<Map<String, String>> result);
    }

    private final AsyncResponse delegate;

    @SuppressLint("StaticFieldLeak")
    public MostSearchedAsync(AsyncResponse delegate) {
        this.delegate = delegate;
    }

    @SuppressWarnings("deprecation")
    @Override
    protected List<Map<String, String>> doInBackground(Void... voids) {
        List<Map<String, String>> combinedResults = new ArrayList<>();
        String[] urls = {URLC, URLM, URLH};

        try {
            for (String url : urls) {
                Connection connection = DriverManager.getConnection(url, USER, PASSWORD);
                String sql = "SELECT * FROM parking_info ORDER BY searches DESC LIMIT 10;";
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery();

                while (resultSet.next()) {
                    Map<String, String> tempInfo = new HashMap<>();
                    tempInfo.put("ppCode", resultSet.getString("ppCode"));
                    tempInfo.put("searches", resultSet.getString("searches"));
                    tempInfo.put("ppName", resultSet.getString("ppName"));
                    tempInfo.put("longitude", resultSet.getString("longitude"));
                    tempInfo.put("latitude", resultSet.getString("latitude"));
                    tempInfo.put("vehCat", resultSet.getString("vehCat"));
                    // ... Add other fields as required
                    combinedResults.add(tempInfo);
                }
                connection.close();
            }

            // Sort combinedResults based on searches
            Collections.sort(combinedResults, new Comparator<Map<String, String>>() {
                public int compare(Map<String, String> o1, Map<String, String> o2) {
                    return Integer.compare(Integer.parseInt(o2.get("searches")), Integer.parseInt(o1.get("searches")));
                }
            });

            // If combinedResults has more than 10 results, limit it
            if (combinedResults.size() > 10) {
                combinedResults = combinedResults.subList(0, 10);
            }
        } catch (Exception e) {
            Log.e("InfoAsyncTask", "Error reading information", e);
        }

        return combinedResults;
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void onPostExecute(List<Map<String, String>> result) {
        delegate.processFinish(result);
    }
}
