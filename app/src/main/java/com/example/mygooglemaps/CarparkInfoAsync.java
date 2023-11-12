package com.example.mygooglemaps;


import android.annotation.SuppressLint;

import android.os.AsyncTask;
import android.util.Log;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

public class CarparkInfoAsync extends AsyncTask<Void, Void, Map<String, String>> {
    private static final String URLC = "jdbc:mysql://172.21.146.188:3306/carparkDetailsC";
    private static final String URLM = "jdbc:mysql://172.21.146.188:3306/carparkDetailsM";
    private static final String URLH = "jdbc:mysql://172.21.146.188:3306/carparkDetailsH";
    private static final String USER = "VMuser";
    private static final String PASSWORD = "sc2006parku";

    private double latitude;
    private double longitude;

    public interface AsyncResponse {
        void processFinish(Map<String, String> result);
    }

    public CarparkInfoAsync.AsyncResponse delegate = null;

    char v = 'x';

    @SuppressLint("StaticFieldLeak")

    @SuppressWarnings("deprecation")
    public CarparkInfoAsync(char veh, double lat, double lon, CarparkInfoAsync.AsyncResponse d) {
        this.v = veh;
        this.latitude = lat;
        this.longitude = lon;
        this.delegate = d;
    }

    @SuppressWarnings("deprecation")
    protected Map<String, String> doInBackground(Void... voids) {
        Map<String, String> info = new HashMap<>();

        try {
            //Class.forName("com.mysql.jdbc.Driver");
            Connection connection;

            switch (this.v) {
                case 'c':
                    connection = DriverManager.getConnection(URLC, USER, PASSWORD);
                    break;
                case 'm':
                    connection = DriverManager.getConnection(URLM, USER, PASSWORD);
                    break;
                case 'h':
                    connection = DriverManager.getConnection(URLH, USER, PASSWORD);
                    break;
                default:
                    connection = DriverManager.getConnection(URLC, USER, PASSWORD);
            }

            //String sql = "SELECT name, address, phone_number FROM school_info LIMIT 1";
            String sql = "SELECT ppCode,ppName,lotsAvailable,rating,vehCat,weekdayRate,satdayRate,sunPHRate,endTime,startTime,parkCapacity,parkingSystem FROM parking_info where latitude = ? and longitude = ? LIMIT 1";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setDouble(1,latitude);
            statement.setDouble(2,longitude);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                // info.put("searches",resultSet.getString("searches"));
                info.put("ppCode",resultSet.getString("ppCode"));
                info.put("ppName", resultSet.getString("ppName"));
                info.put("lotsAvailable", resultSet.getString("lotsAvailable"));
                info.put("rating", resultSet.getString("rating"));
                info.put("vehCat", resultSet.getString("vehCat"));
                info.put("weekdayRate", resultSet.getString("weekdayRate"));
                info.put("satdayRate", resultSet.getString("satdayRate"));
                info.put("sunPHRate", resultSet.getString("sunPHRate"));
                info.put("endTime", resultSet.getString("endTime"));
                info.put("startTime", resultSet.getString("startTime"));
                info.put("parkCapacity", resultSet.getString("parkCapacity"));
                info.put("parkingSystem", resultSet.getString("parkingSystem"));
                //info.put("phone_number", resultSet.getString("phone_number"));
                String query = "UPDATE parking_info SET searches = searches + 1 WHERE ppCode = ?";
                PreparedStatement statement2 = connection.prepareStatement(query);
                String ppC=resultSet.getString("ppCode");
                statement2.setString(1,ppC);
                statement2.executeUpdate();
            }
        } catch (Exception e) {
            Log.e("CarparkInfoAsync", "Error reading information", e);
        }
        return info;
    }


    @SuppressWarnings("deprecation")
    @Override
    protected void onPostExecute(Map<String, String> result) {
        delegate.processFinish(result);
    }


}
