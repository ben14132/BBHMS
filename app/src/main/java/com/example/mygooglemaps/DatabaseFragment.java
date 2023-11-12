package com.example.mygooglemaps;

import android.content.Context;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class DatabaseFragment extends Fragment {

    private DatabaseHelper dbHelper;
    private CarparkAdapter adapter;
    private RecyclerView carparkRecyclerView;
    private FloatingActionButton btnDeleteAll;

    public static class Carpark {
        private String name;
        private String ppCode;
        private String address;
        private String filter;
        private double latitude,longitude;

        public Carpark(String name, String ppCode,String address,String filter,double latitude, double longitude) {
            this.name = name;
            this.ppCode = ppCode;
            this.address=address;
            this.filter=filter;
            this.latitude=latitude;
            this.longitude=longitude;
        }

        public String getName() {
            return name;
        }
        public void setName(String name){
            this.name=name;
        }
        public String getAddress(){
            return address;
        }

        public String getFilter(){
            return filter;
        }
        public double getLatitude(){
            return latitude;
        }
        public double getLongitude(){
            return longitude;
        }
        public String getPpCode() {
            return ppCode;
        }
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
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof DatabaseListener) {
            dbHelper = ((DatabaseListener) context).getDatabase();
        } else {
            throw new ClassCastException(context.toString() + " must implement DatabaseListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_database, container, false);
        carparkRecyclerView = view.findViewById(R.id.carparkRecyclerView);
        carparkRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        FloatingActionButton btnDeleteAll = view.findViewById(R.id.clearCarparkFAB);
        btnDeleteAll.setOnClickListener(v -> deleteAllCarparks());
        initReorderLogic();
        displayData();
        return view;
    }
    private void deleteAllCarparks() {
        new MaterialAlertDialogBuilder(getContext())
                .setTitle("Delete All Carparks")
                .setMessage("Are you sure you want to delete all carparks?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    dbHelper.deleteAllLocations();
                    Toast.makeText(getContext(), "Saved Carparks Cleared", Toast.LENGTH_SHORT).show();
                    displayData();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
    private void initReorderLogic() {
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(createItemTouchHelperCallback());
        itemTouchHelper.attachToRecyclerView(carparkRecyclerView);
    }

    private ItemTouchHelper.Callback createItemTouchHelperCallback() {
        return new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                int fromPosition = viewHolder.getAdapterPosition();
                int toPosition = target.getAdapterPosition();

                // Swap items in the local list
                Collections.swap(adapter.carparks, fromPosition, toPosition);
                adapter.notifyItemMoved(fromPosition, toPosition);

                return true;
            }

            @Override
            public void clearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                super.clearView(recyclerView, viewHolder);
                // Save the new order when the dragging is finished
                for (int i = 0; i < adapter.carparks.size(); i++) {
                    Carpark carPark = adapter.carparks.get(i);
                    dbHelper.reorderItem(carPark.getPpCode(), i);  // You'll implement this function in the next step
                }
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                // Not needed as we are not implementing swipe to dismiss
            }
        };
    }
    /*private void initReorderLogic() {
        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0) {

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                int fromPosition = viewHolder.getAdapterPosition();
                int toPosition = target.getAdapterPosition();

// Temporarily update positions in the database with a big offset to avoid collisions
                dbHelper.updateCarParkPosition(String.valueOf(adapter.getItemId(fromPosition)), fromPosition + 1000);
                dbHelper.updateCarParkPosition(String.valueOf(adapter.getItemId(toPosition)), toPosition + 1000);

// Swap items in the local list
                Collections.swap(adapter.carparks, fromPosition, toPosition);

// Notify adapter about the move
                adapter.notifyItemMoved(fromPosition, toPosition);


                // Swap items in our ArrayList.
                for (int i = 0; i < adapter.carparks.size(); i++) {
                    Carpark carPark = adapter.carparks.get(i);
                    dbHelper.updateCarParkPosition(carPark.getPpCode(), i);
                }

                return true;
            }


            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                // We don't need this as we are not implementing swiping.
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(carparkRecyclerView);
    }*/

    private void displayData() {
        Cursor cursor = dbHelper.getAllLocations();
        List<Carpark> carparks = new ArrayList<>();

        if (cursor != null && cursor.moveToFirst()) {
            do {
                String carparkName = cursor.getString(cursor.getColumnIndex(dbHelper.COLUMN_NAME));
                String ppCode = cursor.getString(cursor.getColumnIndex(dbHelper.COLUMN_PP_CODE));
                String address=getAddress(cursor.getDouble(cursor.getColumnIndex(dbHelper.COLUMN_LATITUDE)),cursor.getDouble(cursor.getColumnIndex(dbHelper.COLUMN_LONGITUDE)));
                String cat=cursor.getString(cursor.getColumnIndex(dbHelper.COLUMN_CAT));
                Carpark carpark = new Carpark(carparkName, ppCode,address,cat,cursor.getDouble(cursor.getColumnIndex(dbHelper.COLUMN_LATITUDE)),cursor.getDouble(cursor.getColumnIndex(dbHelper.COLUMN_LONGITUDE)));
                carparks.add(carpark);
            } while (cursor.moveToNext());
            cursor.close();
        }
        adapter = new CarparkAdapter(carparks, this);
        carparkRecyclerView.setAdapter(adapter);
    }
    private void openCarparkInformationFragment(Carpark carpark) {
        CarparkInformationFragment carparkInfoFragment = new CarparkInformationFragment();

        Bundle carparkInfoBundle = new Bundle();
        carparkInfoBundle.putString("v", carpark.getFilter());
        carparkInfoBundle.putDouble("latitude", carpark.getLatitude());
        carparkInfoBundle.putDouble("longitude", carpark.getLongitude());

        carparkInfoFragment.setArguments(carparkInfoBundle);

        getParentFragmentManager().beginTransaction()
                .replace(R.id.ffframe, carparkInfoFragment)  // Assuming "container" is your fragment container's ID
                .addToBackStack("info")
                .commit();
    }

    public void onRename(int position) {
        Carpark carpark = adapter.carparks.get(position);
        final EditText input = new EditText(getContext());
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setText(carpark.getName());

        new MaterialAlertDialogBuilder(getContext())
                .setTitle("Rename Carpark")
                .setView(input)
                .setPositiveButton("OK", (dialog, which) -> {
                    String newName = input.getText().toString();
                    if (!newName.isEmpty()) {
                        dbHelper.updateLocationName(carpark.getPpCode(), newName);
                        displayData();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }


    public void onDelete(int position) {
        Carpark carparkToDelete = adapter.carparks.get(position);
        dbHelper.deleteLocation(carparkToDelete.getPpCode());
        displayData();
    }

    private class CarparkAdapter extends RecyclerView.Adapter<CarparkAdapter.CarparkViewHolder> {
        private List<Carpark> carparks;
        private DatabaseFragment fragment;

        public CarparkAdapter(List<Carpark> carparks, DatabaseFragment fragment) {
            this.carparks = carparks;
            this.fragment = fragment;
        }

        @NonNull
        @Override
        public CarparkViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_carpark, parent, false);
            return new CarparkViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull CarparkViewHolder holder, int position) {
            Carpark carpark = carparks.get(position);
            holder.tvCarparkName.setText(carpark.getName());
            holder.tvCarparkAddress.setText(carpark.getAddress());
            holder.menuButton.setOnClickListener(v -> showMenu(v, position));
        }

        private void showMenu(View v, int position) {
            PopupMenu popupMenu = new PopupMenu(fragment.getActivity(), v);
            popupMenu.getMenuInflater().inflate(R.menu.carpark_menu, popupMenu.getMenu());

            try {
                Field mPopup = popupMenu.getClass().getDeclaredField("mPopup");
                mPopup.setAccessible(true);
                Object menuPopupHelper = mPopup.get(popupMenu);
                Class<?> classPopupHelper = Class.forName(menuPopupHelper.getClass().getName());
                Method setForceShowIcon = classPopupHelper.getMethod("setForceShowIcon", boolean.class);
                setForceShowIcon.invoke(menuPopupHelper, true);
            } catch (Exception e) {
                e.printStackTrace();
            }

            popupMenu.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();
                if (id == R.id.rename) {
                    fragment.onRename(position);
                    return true;
                } else if (id == R.id.delete) {
                    fragment.onDelete(position);
                    return true;
                } else {
                    return false;
                }
            });

            popupMenu.show();
        }

        @Override
        public int getItemCount() {
            return carparks.size();
        }

        private class CarparkViewHolder extends RecyclerView.ViewHolder {
            TextView tvCarparkName;
            TextView tvCarparkAddress;  // New TextView for the address
            ImageView menuButton;

            public CarparkViewHolder(@NonNull View itemView) {
                super(itemView);
                tvCarparkName = itemView.findViewById(R.id.tvCarparkName);
                tvCarparkAddress = itemView.findViewById(R.id.tvCarparkAddress);  // Initialize the new TextView
                menuButton = itemView.findViewById(R.id.menuButton);

                // 2. Set a click listener on the carpark card to open the CarparkInformationFragment:
                itemView.setOnClickListener(v -> {
                    Carpark selectedCarpark = carparks.get(getAdapterPosition());
                    openCarparkInformationFragment(selectedCarpark);
                });
            }

        }
    }
}
