package com.example.mygooglemaps;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.snackbar.Snackbar;
import java.util.List;
import java.util.Map;

public class MostSearchedCarparksFragment extends Fragment {

    private RecyclerView carparkRecyclerView;
    private CarparkAdapter carparkAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_most, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        carparkRecyclerView = view.findViewById(R.id.carparkRecyclerView);
        carparkRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        fetchAndDisplayData();
    }

    private void fetchAndDisplayData() {
        MostSearchedAsync task = new MostSearchedAsync(result -> {
            carparkAdapter = new CarparkAdapter(result);
            carparkRecyclerView.setAdapter(carparkAdapter);
        });
        task.execute();
    }

    private class CarparkAdapter extends RecyclerView.Adapter<CarparkViewHolder> {
        private final List<Map<String, String>> carparks;

        CarparkAdapter(List<Map<String, String>> carparks) {
            this.carparks = carparks;
        }

        @NonNull
        @Override
        public CarparkViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_most, parent, false);
            return new CarparkViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull CarparkViewHolder holder, int position) {
            holder.bind(carparks.get(position), position + 1);
        }

        @Override
        public int getItemCount() {
            return carparks.size();
        }
    }

    private class CarparkViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final TextView rankTextView;
        private final TextView carparkNameTextView;
        private double latitude;
        private double longitude;
        private String vehcat;

        CarparkViewHolder(@NonNull View itemView) {
            super(itemView);
            rankTextView = itemView.findViewById(R.id.rankTextView);
            carparkNameTextView = itemView.findViewById(R.id.carparkNameTextView);
            itemView.setOnClickListener(this);
        }

        void bind(Map<String, String> carpark, int rank) {
            rankTextView.setText(String.valueOf(rank));
            carparkNameTextView.setText(carpark.get("ppName"));
            latitude = Double.parseDouble(carpark.get("latitude"));
            longitude = Double.parseDouble(carpark.get("longitude"));
            if (carpark.get("vehCat").compareTo("Car") == 0) {
                vehcat= String.valueOf('c');
            }
            else if (carpark.get("vehCat").compareTo("Motorcycle") == 0) {
                vehcat= String.valueOf('m');
            }
            else if (carpark.get("vehCat").compareTo("Heavy Vehicle") == 0) {
                vehcat = String.valueOf('h');
            }


        }

        @Override
        public void onClick(View v) {
            // Updated to Material Snackbar
            CarparkInformationFragment carparkInfoFragment = new CarparkInformationFragment();
            Bundle bundle = new Bundle();
            bundle.putDouble("latitude", latitude);
            bundle.putDouble("longitude", longitude);
            bundle.putString("vehcat", vehcat);
            carparkInfoFragment.setArguments(bundle);

            getParentFragmentManager().beginTransaction()
                    .replace(R.id.ffframe, carparkInfoFragment)  // Assuming "container" is your fragment container's ID
                    .addToBackStack("info")
                    .commit();
        }
    }
}
