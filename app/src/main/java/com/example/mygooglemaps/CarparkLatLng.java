package com.example.mygooglemaps;


import android.os.AsyncTask;
import android.util.Log;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;


public class CarparkLatLng extends AsyncTask<Void, Void, ResultSet>{
    private static final String URLC = "jdbc:mysql://172.21.146.188:3306/carparkDetailsC";
    private static final String URLM = "jdbc:mysql://172.21.146.188:3306/carparkDetailsM";
    private static final String URLH = "jdbc:mysql://172.21.146.188:3306/carparkDetailsH";
    private static final String USER = "VMuser";
    private static final String PASSWORD = "sc2006parku";

    private char veh = 'x';

    public interface AsyncResponse {
        void processFinish(ResultSet rs);
    }

    public AsyncResponse delegate = null;

    public CarparkLatLng (char v, AsyncResponse d){
        this.veh = v;
        this.delegate = d;
    }


    protected ResultSet doInBackground(Void... voids) {
        ResultSet rs1 = null;

        try {
            switch (veh) {
                case 'c':
                    Connection connectionC = DriverManager.getConnection(URLC, USER, PASSWORD);
                    String sqlc = "SELECT ppCode, latitude, longitude FROM parking_info";
                    PreparedStatement statementc = connectionC.prepareStatement(sqlc);
                    rs1 = statementc.executeQuery();
                    break;
                case 'm':
                    Connection connectionM = DriverManager.getConnection(URLM, USER, PASSWORD);
                    String sqlm = "SELECT ppCode, latitude, longitude FROM parking_info";
                    PreparedStatement statementm = connectionM.prepareStatement(sqlm);
                    rs1 = statementm.executeQuery();
                    break;
                case 'h':
                    Connection connection = DriverManager.getConnection(URLH, USER, PASSWORD);
                    String sql = "SELECT ppCode, latitude, longitude FROM parking_info";
                    PreparedStatement statement = connection.prepareStatement(sql);
                    rs1 = statement.executeQuery();
                    break;
                default:
                    rs1 = null;
            }

        } catch (Exception e) {
            Log.e("CarparkLatLng", "Error reading information", e);
        }
        return rs1;
    }

    @Override
    protected void onPostExecute(ResultSet rs) {
        delegate.processFinish(rs);
    }
}


