package com.example.mygooglemaps;

import android.annotation.SuppressLint;

import android.os.AsyncTask;
import android.util.Log;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;


public class CarparkRatingAsync extends AsyncTask<Void, Void, Double> {
    private static final String URLC = "jdbc:mysql://172.21.146.188:3306/carparkDetailsC";
    private static final String URLM = "jdbc:mysql://172.21.146.188:3306/carparkDetailsM";
    private static final String URLH = "jdbc:mysql://172.21.146.188:3306/carparkDetailsH";
    private static final String USER = "VMuser";
    private static final String PASSWORD = "sc2006parku";

    private double rating;
    private double latitude;
    private double longitude;

    public interface AsyncResponse {
        void processFinish(Double result);
    }

    public CarparkRatingAsync.AsyncResponse delegate = null;

    private char v = 'x';

    @SuppressLint("StaticFieldLeak")

    @SuppressWarnings("deprecation")
    public CarparkRatingAsync(char veh, double lat, double lng, double r, CarparkRatingAsync.AsyncResponse d) {
        this.v = veh;
        this.rating = r;
        this.latitude = lat;
        this.longitude = lng;
        this.delegate = d;
    }

    @SuppressWarnings("deprecation")
    protected Double doInBackground(Void... voids) {
        double result;
        double newRating = 0;
        int i = 0;
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

            String sql = "SELECT ppCode,rating,submittedRatings FROM parking_info where latitude = ? AND longitude = ? LIMIT 1";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setDouble(1,latitude);
            statement.setDouble(2,longitude);
            ResultSet resultSet = statement.executeQuery();
            String code = "";
            double total = 0;
            double num = 0;
            if (resultSet.next()) {
                code = resultSet.getString("ppCode");
                num = resultSet.getFloat("submittedRatings");
                total = resultSet.getFloat("rating") * num;
            }
            newRating = (total + rating) / (num + 1);
            newRating = round(newRating);
            String upd = "UPDATE parking_info SET rating = ? WHERE ppCode = ?";
            PreparedStatement updStatement = connection.prepareStatement(upd);
            updStatement.setDouble(1,newRating);
            updStatement.setString(2,code);
            i = updStatement.executeUpdate();
            upd = "UPDATE parking_info SET submittedRatings = ? WHERE ppCode = ?";
            PreparedStatement updStatement2 = connection.prepareStatement(upd);
            updStatement2.setDouble(1,(num + 1));
            updStatement2.setString(2,code);
            i = i * updStatement2.executeUpdate();
        } catch (Exception e) {
            Log.e("CarparkRatingAsync", "Error updating information", e);
        }
        result = i * newRating;
        return result;
    }


    @SuppressWarnings("deprecation")
    @Override
    protected void onPostExecute(Double result) {
        delegate.processFinish(result);
    }

    public static double round(double num)
    {
        double scale = Math.pow(10, 1);
        double roundedNum = Math.round(num * scale) / scale;
        return roundedNum;
    }

}
