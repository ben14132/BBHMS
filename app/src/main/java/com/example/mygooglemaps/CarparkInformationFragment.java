package com.example.mygooglemaps;

import android.app.ProgressDialog;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.bumptech.glide.Glide;
import android.content.Intent;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;
import java.util.Map;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;
import java.io.InputStream;
import java.net.MalformedURLException;




/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CarparkInformationFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CarparkInformationFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_LAT = "latitude";
    private static final String ARG_LNG = "longitude";

    // TODO: Rename and change types of parameters
    private char v;
    private double lat;
    private double lng;
    private String ID;
    private View view;
    private DatabaseHelper dbHelper;
    private String ppName, ppCode, address;
    private String cat;
    private double latitude, longitude;
    private int searches;

    private static final String URLC = "jdbc:mysql://172.21.146.188:3306/carparkDetailsC";
    private static final String URLM = "jdbc:mysql://172.21.146.188:3306/carparkDetailsM";
    private static final String URLH = "jdbc:mysql://172.21.146.188:3306/carparkDetailsH";
    private static final String USER = "VMuser";
    private static final String PASSWORD = "sc2006parku";



    //
    ProgressDialog pd;

    public CarparkInformationFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param lat Parameter 2.
     * @param lng Parameter 3.
     * @return A new instance of fragment CarparkInformation.
     */
    // TODO: Rename and change types and number of parameters
    public static CarparkInformationFragment newInstance(char veh, double lat, double lng) {
        CarparkInformationFragment fragment = new CarparkInformationFragment();
        Bundle args = new Bundle();
        args.putDouble(ARG_LAT, lat);
        args.putDouble(ARG_LNG, lng);
        args.putChar("v",veh);
        fragment.setArguments(args);
        return fragment;
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getActivity() instanceof DatabaseListener) {
            dbHelper = ((DatabaseListener) getActivity()).getDatabase();
        } else {
            throw new ClassCastException(getActivity().toString() + " must implement DatabaseListener");
        }
        if (getArguments() != null) {
            lat = getArguments().getDouble(ARG_LAT);
            lng = getArguments().getDouble(ARG_LNG);
            v = getArguments().getChar("v");
        }

        CarparkInfoAsync ci = (CarparkInfoAsync) new CarparkInfoAsync(v, lat, lng, new CarparkInfoAsync.AsyncResponse(){
            @Override
            public void processFinish(Map<String, String> result){
                //data processing code will be written here
                if (!result.isEmpty()) {
                    TextView txtName = view.findViewById(R.id.txtppName);
                    TextView txtAddress = view.findViewById(R.id.txtAddress);
                    TextView txtLotsAvail = view.findViewById(R.id.txtnumLotsAvailable);
                    TextView txtRating = view.findViewById(R.id.txtRating);
                    TextView txtVeh = view.findViewById(R.id.txtVehType);
                    TextView txtWkdayRate = view.findViewById(R.id.txtWkdayRate);
                    TextView txtSatRate = view.findViewById(R.id.txtSatRate);
                    TextView txtSunPHRate = view.findViewById(R.id.txtSunPHRate);
                    TextView txtEndTime = view.findViewById(R.id.txtEndTime);
                    TextView txtStartTime = view.findViewById(R.id.txtStartTime);
                    TextView txtParkCap = view.findViewById(R.id.txtParkCap);
                    TextView txtParkSys = view.findViewById(R.id.txtParkSys);
                    //TextView textViewPhoneNumber = findViewById(R.id.textViewPhone);

                    txtName.setText(result.get("ppName"));
                    ppName = result.get("ppName");
                    ppCode = result.get("ppCode");
                    address = (getAddress(lat, lng));
                    cat = result.get("parkingSystem");


                    //location image------------------------------------------------------------------------
                    String apiKey = getString(R.string.my_map_api_key);
                    String photoReference = getPhotoRef("car park "+ppName); // Obtained from the Autocomplete Fragment
                    int maxWidth = 400; // You can adjust this as needed.

                    if(photoReference!=null){
                        String photoUrl = "https://maps.googleapis.com/maps/api/place/photo?" +
                                "maxwidth=" + maxWidth +
                                "&photoreference=" + photoReference +
                                "&key=" + apiKey;
                        Log.d("photo", "onCreate: " + photoUrl);

                        Context context = getContext();
                        ImageView mapImageView = view.findViewById(R.id.mapImageView);

                        // Load and display the image using Glide
                        Glide.with(context)
                                .load(photoUrl)
                                .into(mapImageView);
                    }
                    //-----------------------------------------------------------------------------------


                    //latitude= Double.parseDouble(result.get("lat"));
                    //longitude= Double.parseDouble(result.get("lng"));
                    txtAddress.setText(getAddress(lat, lng));
                    txtLotsAvail.setText(result.get("lotsAvailable"));
                    txtRating.setText(result.get("rating"));
                    txtVeh.setText(result.get("vehCat"));
                    txtWkdayRate.setText(result.get("weekdayRate"));
                    txtSatRate.setText(result.get("satdayRate"));
                    txtSunPHRate.setText(result.get("sunPHRate"));
                    txtEndTime.setText(result.get("endTime"));
                    txtStartTime.setText(result.get("startTime"));
                    txtParkCap.setText(result.get("parkCapacity"));
                    if (result.get("parkingSystem").compareTo("B") == 0) {
                        txtParkSys.setText("Electronic Parking");
                    } else if (result.get("parkingSystem").compareTo("C") == 0) {
                        txtParkSys.setText(result.get("Coupon Parking"));
                        //textViewPhoneNumber.setText(result.get("phone_number"));
                    }


                }
            }
        }).execute();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        this.view = inflater.inflate(R.layout.carpark_information, container, false);

        Button back = view.findViewById(R.id.btnBack);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
                fragmentManager.popBackStack("info", FragmentManager.POP_BACK_STACK_INCLUSIVE);
            }
        });


        Button btnNavigate = view.findViewById(R.id.btnNavigate);
        btnNavigate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lat = getArguments().getDouble(ARG_LAT);
                lng = getArguments().getDouble(ARG_LNG);

                MainActivity mainActivity = (MainActivity) getActivity();
                if (mainActivity != null) {
                    mainActivity.mainonNavigateButtonClick(lat, lng);
                }

//                FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
//                fragmentManager.popBackStack("home", FragmentManager.POP_BACK_STACK_INCLUSIVE);
                FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
                fragmentManager.popBackStack("info", FragmentManager.POP_BACK_STACK_INCLUSIVE);
                FirstFragment firstFragment = new FirstFragment();
                FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.flFragment, firstFragment);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();








            }
        });





        //menu button
        // Define your Button and CardView
        Button btnMenu = view.findViewById(R.id.btnMenu);
        CardView cardView = view.findViewById(R.id.cardView);

        btnMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu popupMenu = new PopupMenu(requireContext(), cardView); // context should be your activity or fragment context

                // Inflate the menu resource
                popupMenu.getMenuInflater().inflate(R.menu.popup_menu, popupMenu.getMenu());

                // Set an item click listener for the menu items
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        int id = item.getItemId();
                        if(id == R.id.menu_item_1) {
                            // Handle menu item 1 click
                            dbHelper.insertLocation(ppCode,ppName,lat,lng,cat);
                            Toast.makeText(requireContext(), "Carpark Saved", Toast.LENGTH_SHORT).show();
                            return true;
                        }
                        else if (id == R.id.menu_item_2) {
                            // Handle menu item 2 click
                            //add fragment
                            CarparkRatingFragment ratingFragment = new CarparkRatingFragment();
                            Bundle b = new Bundle();
                            b.putChar("veh", v);
                            b.putDouble("latitude", lat);
                            b.putDouble("longitude", lng);
                            ratingFragment.setArguments(b);
                            final androidx.fragment.app.FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
                            fragmentTransaction.add(R.id.ratingLayout, ratingFragment);
                            fragmentTransaction.addToBackStack("rating");
                            fragmentTransaction.commit();
                            return true;
                        }
                        else if (id == R.id.menu_item_3){
                            ParkingTrendFragment parkingTrendFragment = new ParkingTrendFragment();
                            Bundle occupancyInfoBundle = new Bundle();
                            occupancyInfoBundle.putChar("v", v);
                            occupancyInfoBundle.putDouble("latitude", lat);
                            occupancyInfoBundle.putDouble("longitude", lng);
                            parkingTrendFragment.setArguments(occupancyInfoBundle);
                            getActivity().getSupportFragmentManager().beginTransaction()
                                    .replace(R.id.carParkInfo, parkingTrendFragment)
                                    .addToBackStack(null)
                                    .commit();
                            return true;
                        }
                            // Add more cases for other menu items as needed
                        return false;
                    }
                });

                // Show the popup menu
                popupMenu.show();
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        CarparkInfoAsync ci = (CarparkInfoAsync) new CarparkInfoAsync(v, lat, lng, new CarparkInfoAsync.AsyncResponse() {
            @Override
            public void processFinish(Map<String, String> result) {
                //data processing code will be written here
                if (!result.isEmpty()) {
                    TextView txtLotsAvail = view.findViewById(R.id.txtnumLotsAvailable);
                    TextView txtRating = view.findViewById(R.id.txtRating);
                    txtLotsAvail.setText(result.get("lotsAvailable"));
                    txtRating.setText(result.get("rating"));
                }
            }
        }).execute();
    }

    private String getAddress(double lat, double lng) {
        Geocoder geocoder;
        Address address = null;
        String a = "";
        geocoder = new Geocoder(this.getActivity(), Locale.getDefault());
        try {
            address = geocoder.getFromLocation(lat, lng, 1).get(0); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
        } catch (Exception e) {
            Log.d("carpark address", "address error");
        }

        if (address != null) {
            String addressLine = address.getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
            //String knownName = address.getFeatureName(); // Only if available else return NULL
            a = addressLine;
        }

        return a;
    }

    private String getPhotoRef(String cpName){
        String photoReference = null;
        cpName = cpName.replaceAll(" ", "%20").toLowerCase();
        try {
            // Create the URL for the Place Search request
            String apiKey = getString(R.string.my_map_api_key);
            String requestUrl = "https://maps.googleapis.com/maps/api/place/textsearch/json?"
                    + "query=" + cpName
                    + "&type=parking"
                    + "&radius=50"
                    + "&key=" + apiKey;
            Log.d("photo", "url: " + requestUrl);

            try {
                // Create and execute the JsonTask to fetch the data
                JsonTask jsonTask = new JsonTask();
                String firstPhotoReference = jsonTask.execute(requestUrl).get();

                if (firstPhotoReference != null) {
                    // Use the firstPhotoReference as needed
                    Log.d("First Photo Reference", "GET PHOTO REF" + firstPhotoReference);
                } else {
                    Log.d("First Photo Reference", "No photo reference found.");
                }
                return firstPhotoReference;
            }
            catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private class JsonTask extends AsyncTask<String, String, String> {
        private String firstPhotoReference = null;
        protected void onPreExecute() {
            super.onPreExecute();

            Context context = getContext();
            pd = new ProgressDialog(context);
            pd.setMessage("Please wait");
            pd.setCancelable(false);
            pd.show();
        }

        protected String doInBackground(String... params) {
            HttpURLConnection connection = null;
            BufferedReader reader = null;

            try {
                URL url = new URL(params[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();


                InputStream stream = connection.getInputStream();

                reader = new BufferedReader(new InputStreamReader(stream));

                StringBuffer buffer = new StringBuffer();
                String line = "";

                while ((line = reader.readLine()) != null) {
                    buffer.append(line+"\n");
                    //Log.d("Response: ", "> " + line);
                }

                // Convert the JSON response to a string
                String jsonString = buffer.toString();

                // Parse the JSON response
                JSONObject jsonObject = new JSONObject(jsonString);

                // Check if the "results" array exists in the JSON response
                if (jsonObject.has("results")) {
                    JSONArray resultsArray = jsonObject.getJSONArray("results");

                    // Check if there are results in the array
                    for (int i = 0; i < resultsArray.length(); i++) {
                        JSONObject resultObject = resultsArray.getJSONObject(i);

                        // Check if the "photos" array exists in the first result
                        if (resultObject.has("photos")) {
                            JSONArray photosArray = resultObject.getJSONArray("photos");

                            // Iterate through the photos
                            for (int j = 0; j < photosArray.length(); j++) {
                                JSONObject photo = photosArray.getJSONObject(j);

                                // Check if the "photo_reference" exists in the photo
                                if (photo.has("photo_reference")) {
                                    // Get the "photo_reference" value from the first photo
                                    firstPhotoReference = photo.getString("photo_reference");
                                    return firstPhotoReference;
                                }
                            }
                        }
                    }
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                throw new RuntimeException(e);
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
                try {
                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return firstPhotoReference;
        }

        protected void onPostExecute(String firstPhotoReference) {
            // Use the firstPhotoReference in your UI here
            if (pd.isShowing()) {
                pd.dismiss();
            }
            //Log.d("First Photo Reference", "ON POST EXE" + firstPhotoReference);
        }
    }

}