package com.example.ecostayapp.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ecostayapp.R;
import com.example.ecostayapp.RoomBookingActivity;
import com.example.ecostayapp.adapters.RoomsAdapter;
import com.example.ecostayapp.models.Room;
import com.example.ecostayapp.FilterDialog;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class RoomsFragment extends Fragment {

    private RecyclerView recyclerViewRooms;
    private RoomsAdapter roomsAdapter;
    private List<Room> rooms = new ArrayList<>();
    private List<Room> filteredRooms = new ArrayList<>();
    private EditText editTextSearch;
    private ImageView btnClearSearch;
    private ImageView btnFilter;
    private FirebaseFirestore db;
    
    // Filter state variables
    private String currentPriceRange = "";
    private String currentType = "";
    private String currentCategory = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_rooms, container, false);
        
        initViews(view);
        setupRecyclerView();
        setupSearchAndFilter();
        loadRooms();
        
        return view;
    }

    private void initViews(View view) {
        recyclerViewRooms = view.findViewById(R.id.recyclerViewRooms);
        editTextSearch = view.findViewById(R.id.editTextSearch);
        btnClearSearch = view.findViewById(R.id.btnClearSearch);
        btnFilter = view.findViewById(R.id.btnFilter);
        db = FirebaseFirestore.getInstance();
    }

    private void setupRecyclerView() {
        roomsAdapter = new RoomsAdapter(filteredRooms, room -> {
            Intent intent = new Intent(getActivity(), RoomBookingActivity.class);
            intent.putExtra("room", room);
            startActivity(intent);
        });
        recyclerViewRooms.setLayoutManager(new GridLayoutManager(getContext(), 2));
        recyclerViewRooms.setAdapter(roomsAdapter);
    }

    private void setupSearchAndFilter() {
        // Search functionality
        editTextSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterRooms(s.toString());
                updateClearButtonVisibility(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Clear search button
        btnClearSearch.setOnClickListener(v -> {
            editTextSearch.setText("");
            filterRooms("");
            updateClearButtonVisibility("");
        });

        // Filter button
        btnFilter.setOnClickListener(v -> {
            showFilterDialog();
        });
    }

    private void filterRooms(String query) {
        // Apply all filters including search
        applyAllFilters();
    }

    private void updateClearButtonVisibility(String text) {
        btnClearSearch.setVisibility(text.isEmpty() ? View.GONE : View.VISIBLE);
    }

    private void showFilterDialog() {
        FilterDialog filterDialog = new FilterDialog(getContext(), new FilterDialog.FilterListener() {
            @Override
            public void onFilterApplied(String priceRange, String category, String type) {
                applyFilters(priceRange, category, type);
            }

            @Override
            public void onFilterCleared() {
                clearFilters();
            }
        });
        filterDialog.show();
    }

    private void applyFilters(String priceRange, String category, String type) {
        // Store current filter state
        currentPriceRange = priceRange;
        currentType = type;
        currentCategory = category;
        
        android.util.Log.d("RoomsFragment", "=== FILTER APPLIED ===");
        android.util.Log.d("RoomsFragment", "Stored PriceRange: " + currentPriceRange);
        android.util.Log.d("RoomsFragment", "Stored Type: " + currentType);
        android.util.Log.d("RoomsFragment", "Stored Category: " + currentCategory);
        
        // Apply all filters (search + price/type)
        applyAllFilters();
        
        if (filteredRooms.isEmpty()) {
            Toast.makeText(getContext(), "No rooms match the selected filters", Toast.LENGTH_SHORT).show();
        }
    }

    private void clearFilters() {
        android.util.Log.d("RoomsFragment", "=== CLEAR FILTERS CALLED ===");
        
        // Reset filter state
        currentPriceRange = "";
        currentType = "";
        currentCategory = "";
        
        filteredRooms.clear();
        filteredRooms.addAll(rooms);
        roomsAdapter.notifyDataSetChanged();
        
        android.util.Log.d("RoomsFragment", "Filters cleared - showing all " + rooms.size() + " rooms");
    }
    
    // Test method to manually apply a price filter
    public void testPriceFilter() {
        android.util.Log.d("RoomsFragment", "=== TESTING PRICE FILTER ===");
        applyFilters("25000-", "", "");
    }
    
    private void applyAllFilters() {
        // Apply both search and price/type filters
        String searchQuery = editTextSearch.getText().toString();
        
        filteredRooms.clear();
        
        android.util.Log.d("RoomsFragment", "=== APPLYING ALL FILTERS ===");
        android.util.Log.d("RoomsFragment", "Search Query: " + searchQuery);
        android.util.Log.d("RoomsFragment", "Price Range: " + currentPriceRange);
        android.util.Log.d("RoomsFragment", "Type: " + currentType);
        
        for (Room room : rooms) {
            boolean matchesSearch = true;
            boolean matchesPrice = true;
            boolean matchesType = true;
            
            // Search filter
            if (!searchQuery.isEmpty()) {
                String lowerQuery = searchQuery.toLowerCase();
                matchesSearch = room.getName().toLowerCase().contains(lowerQuery) ||
                              room.getDescription().toLowerCase().contains(lowerQuery) ||
                              room.getType().toLowerCase().contains(lowerQuery);
            }
            
            // Price filter - Convert USD prices to LKR for comparison
            android.util.Log.d("RoomsFragment", "Checking price filter for: " + room.getName());
            android.util.Log.d("RoomsFragment", "currentPriceRange: '" + currentPriceRange + "'");
            android.util.Log.d("RoomsFragment", "isEmpty: " + currentPriceRange.isEmpty());
            android.util.Log.d("RoomsFragment", "equals('-'): " + currentPriceRange.equals("-"));
            
            if (!currentPriceRange.isEmpty() && !currentPriceRange.equals("-")) {
                android.util.Log.d("RoomsFragment", "Applying price filter...");
                String[] prices = currentPriceRange.split("-");
                android.util.Log.d("RoomsFragment", "Split prices: " + java.util.Arrays.toString(prices));
                android.util.Log.d("RoomsFragment", "Prices length: " + prices.length);
                
                if (prices.length == 2) {
                    try {
                        double minPriceLKR = prices[0].isEmpty() ? 0 : Double.parseDouble(prices[0]);
                        double maxPriceLKR = prices[1].isEmpty() ? Double.MAX_VALUE : Double.parseDouble(prices[1]);
                        
                        // Convert stored USD price to LKR (1 USD = 325 LKR)
                        double roomPriceLKR = room.getPricePerNight() * 325;
                        
                        matchesPrice = roomPriceLKR >= minPriceLKR && roomPriceLKR <= maxPriceLKR;
                        
                        android.util.Log.d("RoomsFragment", "PRICE CHECK: " + room.getName() + 
                            " USD: " + room.getPricePerNight() + 
                            " LKR: " + roomPriceLKR + 
                            " Min: " + minPriceLKR + 
                            " Max: " + maxPriceLKR + 
                            " >= Min: " + (roomPriceLKR >= minPriceLKR) +
                            " <= Max: " + (roomPriceLKR <= maxPriceLKR) +
                            " FINAL: " + matchesPrice);
                    } catch (NumberFormatException e) {
                        android.util.Log.e("RoomsFragment", "Error parsing price range: " + currentPriceRange, e);
                        matchesPrice = true;
                    }
                } else {
                    android.util.Log.d("RoomsFragment", "Price array length not 2, skipping price filter");
                }
            } else {
                android.util.Log.d("RoomsFragment", "No price filter applied");
            }
            
            // Type filter
            if (!currentType.equals("All") && !currentType.isEmpty()) {
                matchesType = room.getType() != null && room.getType().equalsIgnoreCase(currentType);
                android.util.Log.d("RoomsFragment", "Room: " + room.getName() + 
                    " Type: " + room.getType() + 
                    " FilterType: " + currentType + 
                    " Matches Type: " + matchesType);
            }
            
            if (matchesSearch && matchesPrice && matchesType) {
                filteredRooms.add(room);
                android.util.Log.d("RoomsFragment", "Room added to filtered list: " + room.getName());
            }
        }
        
        android.util.Log.d("RoomsFragment", "Total rooms: " + rooms.size());
        android.util.Log.d("RoomsFragment", "Filtered rooms: " + filteredRooms.size());
        
        // Debug: List all rooms and their prices
        android.util.Log.d("RoomsFragment", "=== ALL ROOMS DEBUG ===");
        for (Room room : rooms) {
            double priceLKR = room.getPricePerNight() * 325;
            android.util.Log.d("RoomsFragment", "Room: " + room.getName() + " - USD: " + room.getPricePerNight() + " - LKR: " + priceLKR);
        }
        
        android.util.Log.d("RoomsFragment", "=== FILTERED ROOMS DEBUG ===");
        for (Room room : filteredRooms) {
            double priceLKR = room.getPricePerNight() * 325;
            android.util.Log.d("RoomsFragment", "Filtered Room: " + room.getName() + " - USD: " + room.getPricePerNight() + " - LKR: " + priceLKR);
        }
        
        roomsAdapter.notifyDataSetChanged();
    }

    private void loadRooms() {
        db.collection("rooms")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    rooms.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Room room = document.toObject(Room.class);
                        room.setId(document.getId());
                        rooms.add(room);
                    }
                    applyAllFilters();
                })
                .addOnFailureListener(e -> {
                    loadSampleRooms();
                });
    }

    private void loadSampleRooms() {
        rooms.clear();
        rooms.add(new Room("Mountain View Cabin", "Luxurious cabin with panoramic mountain views", 45.0, "https://example.com/cabin1.jpg", 4, false));
        rooms.add(new Room("Eco Pod", "Sustainable pod with modern amenities", 30.0, "https://example.com/ecopod1.jpg", 2, false));
        rooms.add(new Room("Treehouse Suite", "Unique treehouse experience in nature", 60.0, "https://example.com/treehouse1.jpg", 2, false));
        rooms.add(new Room("Sustainable Suite", "Eco-friendly suite with all amenities", 38.0, "https://example.com/suite1.jpg", 3, false));
        rooms.add(new Room("Forest Bungalow", "Cozy bungalow surrounded by forest", 25.0, "https://example.com/bungalow1.jpg", 2, false));
        rooms.add(new Room("Luxury Eco Villa", "Premium villa with sustainable features", 75.0, "https://example.com/villa1.jpg", 6, false));
        filterRooms(editTextSearch.getText().toString());
    }
}
