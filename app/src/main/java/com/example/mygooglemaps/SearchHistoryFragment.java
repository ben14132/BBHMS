package com.example.mygooglemaps;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;


import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Status;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.PlacesClient;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import android.util.Log;

import android.widget.Toast;
import android.content.DialogInterface;
import androidx.appcompat.app.AlertDialog;
import android.content.DialogInterface;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import androidx.fragment.app.FragmentManager;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPlaceResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import android.content.Context;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class SearchHistoryFragment extends Fragment {

    private RecyclerView searchHistoryRecyclerView;
    private SearchHistoryAdapter searchHistoryAdapter;
    private SearchHistory search;
    private FloatingActionButton clearHistoryFAB;



    public SearchHistoryFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof SearchHistoryListener) {
            search = ((SearchHistoryListener) context).getSearchHistory();
        } else {
            throw new ClassCastException(context.toString() + " must implement DatabaseListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search_history, container, false);

        // Initialize views
        clearHistoryFAB = view.findViewById(R.id.clearHistoryFAB);
        searchHistoryRecyclerView = view.findViewById(R.id.searchHistoryRecyclerView);
        searchHistoryRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Fetch and display data
        displayData();

        // Set onClickListener for the Clear History FAB
        clearHistoryFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new com.google.android.material.dialog.MaterialAlertDialogBuilder(getContext())
                        .setTitle("Clear Search History")
                        .setMessage("Are you sure you want to clear all search history?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                search.deleteAllLocations();
                                // Update the displayed data after clearing
                                displayData();
                                Toast.makeText(getContext(), "Search History Cleared", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setNegativeButton("No", null)
                        .show();
            }
        });

        return view;
    }

    @SuppressLint("NotifyDataSetChanged")
    private void displayData() {
        Cursor cursor = search.getAllLocations();
        List<String> searchData = new ArrayList<>();

        if (cursor != null && cursor.moveToFirst()) { // Check if cursor has data
            do {
                String code = cursor.getString(cursor.getColumnIndex(SearchHistory.COLUMN_SEARCH));
                searchData.add(code);
            } while (cursor.moveToNext());
            cursor.close();
        }

        searchHistoryAdapter = new SearchHistoryAdapter(searchData);
        searchHistoryRecyclerView.setAdapter(searchHistoryAdapter);
    }

    public void onResume() {
        super.onResume();
        displayData();  // Refresh the data every time the fragment comes into view
    }

    // Inner adapter class for the RecyclerView
    // Inner adapter class for the RecyclerView
    private class SearchHistoryAdapter extends RecyclerView.Adapter<SearchHistoryViewHolder> {

        private List<String> searchData;

        SearchHistoryAdapter(List<String> searchData) {
            this.searchData = searchData;
        }

        @NonNull
        @Override
        public SearchHistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new SearchHistoryViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_search_history, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull SearchHistoryViewHolder holder, int position) {
            holder.bind(searchData.get(position));
            holder.deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String itemToDelete = searchData.get(position);
                    search.deleteLocation(itemToDelete); // You'll need to implement this method in SearchHistory class
                    searchData.remove(position);
                    notifyItemRemoved(position);
                    Toast.makeText(getContext(), "Item Deleted", Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override
        public int getItemCount() {
            return searchData.size();
        }

        void updateSearchData(List<String> newSearchData) {
            this.searchData = newSearchData;
            notifyDataSetChanged();
        }
    }

    // ViewHolder class
    private class SearchHistoryViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private final TextView textView;
        final ImageView deleteButton; // Your delete button
        private String currentData;  // Store the current item's data



        SearchHistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.text1);
            deleteButton = itemView.findViewById(R.id.deleteButton);
            itemView.setOnClickListener(this);  // Attach the click listener to the itemView
        }

        void bind(String data) {
            textView.setText(data);
            currentData = data;  // Store the current item's data
        }

        @Override
        public void onClick(View v) {
            String place_ID;
            // Here, handle the click event. For demonstration, let's show a Toast.
            // Initialise Place
            Places.initialize(getActivity().getApplicationContext(), getString(R.string.my_map_api_key));

            // Initialize the AutocompleteSupportFragment.
            AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                    getParentFragmentManager().findFragmentById(R.id.autocomplete_fragment);
            autocompleteFragment.setText(currentData);

            //get place_id
            String apiKey = getString(R.string.my_map_api_key);
            String getPlaceName = currentData.replaceAll(" ", "%20");
            String requestUrl = "https://maps.googleapis.com/maps/api/place/textsearch/json?"
                    + "query=" + getPlaceName
                    + "&radius=10"
                    + "&key=" + apiKey;
            Log.i("place", "URL: " + requestUrl);

            JsonTask jsonTask = new JsonTask();
            try {
                place_ID = jsonTask.execute(requestUrl).get();
                Log.i("place", "Place ID: " + place_ID);
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            // Fetch the Place using the Place ID
            try {
                final List<Place.Field> placeFields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG);
                final FetchPlaceRequest request = FetchPlaceRequest.newInstance(place_ID, placeFields);

                PlacesClient placesClient = Places.createClient(getActivity().getApplicationContext());
                placesClient.fetchPlace(request).addOnSuccessListener((response) -> {
                    Place place = response.getPlace();
                    Log.i("place", "Place found: " + place.getName());

                    //call find frm main
                    ((MainActivity)getActivity()).findCarpark(place);
                    ((MainActivity)getActivity()).setCurPlace(place);
                }).addOnFailureListener((exception) -> {
                    if (exception instanceof ApiException) {
                        final ApiException apiException = (ApiException) exception;
                        //Log.e(TAG, "Place not found: " + exception.getMessage());
                        final int statusCode = apiException.getStatusCode();
                        // TODO: Handle error with given status code.
                    }
                });
            } catch (Exception e) {
                // Handle any exceptions
            }

            // open map
            FirstFragment firstFragment = new FirstFragment();
            getParentFragmentManager()
                    .beginTransaction()
                    .replace(R.id.flFragment, firstFragment)
                    .commit();


            // If you want to pass this data to another activity, fragment or process it further, you can do that here.
        }
    }

    private class JsonTask extends AsyncTask<String, String, String> {
        private String place_ID = null;

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
                        if (resultObject.has("place_id")) {
                            place_ID = resultObject.getString("place_id");
                            return place_ID;
                        }
                    }
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
            finally {
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
            return place_ID;
        }
    }
}
