package com.example.mygooglemaps;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.bumptech.glide.Glide;

import java.util.Map;
import java.util.*;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CarparkRatingFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CarparkRatingFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_VEH = "veh";
    private static final String ARG_LAT = "latitude";
    private static final String ARG_LNG = "longitude";

    // TODO: Rename and change types of parameters
    private char v;
    private float rating;
    private double lat;
    private double lng;
    private View view;

    public CarparkRatingFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @return A new instance of fragment CarparkRatingFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static CarparkRatingFragment newInstance(char param1, double lat, double lng) {
        CarparkRatingFragment fragment = new CarparkRatingFragment();
        Bundle args = new Bundle();
        args.putChar(ARG_VEH,param1);
        args.putDouble(ARG_LAT, lat);
        args.putDouble(ARG_LNG, lng);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            v = getArguments().getChar(ARG_VEH);
            lat = getArguments().getDouble(ARG_LAT);
            lng = getArguments().getDouble(ARG_LNG);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        this.view = inflater.inflate(R.layout.fragment_carpark_rating, container, false);

        RatingBar rb = view.findViewById(R.id.ratingBar);
        Button btnDone = view.findViewById(R.id.btnDone);
        btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View vieww) {
                rating = Math.round(rb.getRating());
                CarparkRatingAsync cr = (CarparkRatingAsync) new CarparkRatingAsync(v, lat, lng, rating, new CarparkRatingAsync.AsyncResponse(){
                    @Override
                    public void processFinish(Double result){
                        if (result > 0) {   //result will be new rating if successful, 0 otherwise
                            Toast.makeText(view.getContext(), "Rating saved", Toast.LENGTH_SHORT).show();
//                            TextView txtRating = view.findViewById(R.id.txtRating);
//                            txtRating.setText(Float.toString(result));
                        }
                        else {
                            Toast.makeText(view.getContext(), "Unable to save rating", Toast.LENGTH_SHORT).show();
                        }
                    }
                }).execute();

                androidx.fragment.app.FragmentTransaction fragmentTransaction = requireActivity().getSupportFragmentManager().beginTransaction();
                CarparkInformationFragment carparkInfoFragment = new CarparkInformationFragment();
                Bundle carparkInfoBundle = new Bundle();
                carparkInfoBundle.putChar("v", v);
                carparkInfoBundle.putDouble("latitude", lat);
                carparkInfoBundle.putDouble("longitude", lng);
                carparkInfoFragment.setArguments(carparkInfoBundle);
                fragmentTransaction.replace(R.id.ffframe, carparkInfoFragment);
                fragmentTransaction.addToBackStack("info2");
                fragmentTransaction.commit();
            }
        });

        return view;
    }
}