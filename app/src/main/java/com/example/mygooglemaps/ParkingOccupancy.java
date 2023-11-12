package com.example.mygooglemaps;


import android.os.AsyncTask;
import android.util.Log;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import java.util.Calendar;
import java.text.SimpleDateFormat;
import java.util.Date;


public class ParkingOccupancy extends AsyncTask<Void, Void, ResultSet>{
    private static final String URLC = "jdbc:mysql://172.21.146.188:3306/carparkDetailsC";
    private static final String URLM = "jdbc:mysql://172.21.146.188:3306/carparkDetailsM";
    private static final String URLH = "jdbc:mysql://172.21.146.188:3306/carparkDetailsH";
    private static final String USER = "VMuser";
    private static final String PASSWORD = "sc2006parku";

    private char veh = 'x';
    private double latitude;
    private double longitude;

    public interface AsyncResponse {
        void processFinish(ResultSet rs);
    }

    public AsyncResponse delegate = null;

    public ParkingOccupancy(char v, double lat, double lon, AsyncResponse d){
        this.veh = v;
        this.delegate = d;
        this.latitude = lat;
        this.longitude = lon;
    }


    protected ResultSet doInBackground(Void... voids) {
        ResultSet rs1 = null;

        try {
            //Class.forName("com.mysql.jdbc.Driver");
            Connection connection;

            switch (this.veh) {
                case 'c':
                    connection = DriverManager.getConnection(URLC, USER, PASSWORD);
                    break;
                case 'm':
                    connection = DriverManager.getConnection(URLM, USER, PASSWORD);
                    break;
                case 'h':
                    connection = DriverManager.getConnection(URLH, USER, PASSWORD);
                default:
                    connection = DriverManager.getConnection(URLC, USER, PASSWORD);
            }
            String sql = "SELECT ppCode FROM parking_info where latitude = ? and longitude = ? LIMIT 1";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setDouble(1,latitude);
            Log.d("ParkingOccupancy", "lat: " + latitude);
            statement.setDouble(2,longitude);
            Log.d("ParkingOccupancy", "long: " + longitude);
            ResultSet resultSet = statement.executeQuery();
            Log.d("ParkingOccupancy", "Checkpoint 1");
            resultSet.next();
            String carparkID = resultSet.getString("ppCode");
            Log.d("ParkingOccupancy", "Checkpoint 2: " + carparkID);
            String sqlc = "SELECT available_lots, hour FROM average WHERE date = DATE_FORMAT(DATE_SUB(CURDATE(), INTERVAL 1 DAY), '%Y-%c-%e') AND carpark_id = ?";
            PreparedStatement statementc = connection.prepareStatement(sqlc);
            statementc.setString(1, carparkID);
//            Calendar calendar = Calendar.getInstance();
//            calendar.add(Calendar.DAY_OF_YEAR, -1); // Subtract 1 day
//            Date yesterday = calendar.getTime();
//            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
//            String formatted_yesterday = dateFormat.format(yesterday);
//            Log.d("ParkingOccupancy", "Date: " + formatted_yesterday);
//            statementc.setString(2, formatted_yesterday);
            rs1 = statementc.executeQuery();
            Log.d("ParkingOccupancy", "Checkpoint 3");

        } catch (Exception e) {
            Log.e("ParkingOccupancy", "Error reading information", e);
        }
        return rs1;
    }

    @Override
    protected void onPostExecute(ResultSet rs) {
        delegate.processFinish(rs);
    }
}


