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
import android.widget.TextView;
import android.widget.Toast;
import androidx.cardview.widget.CardView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ecostayapp.R;
import com.example.ecostayapp.RoomBookingActivity;
import com.example.ecostayapp.ActivityBookingActivity;
import com.example.ecostayapp.adapters.FeaturedRoomsAdapter;
import com.example.ecostayapp.adapters.EcoInitiativesAdapter;
import com.example.ecostayapp.models.Room;
import com.example.ecostayapp.models.EcoInitiative;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private RecyclerView recyclerViewFeaturedRooms;
    private RecyclerView recyclerViewEcoInitiatives;
    private EditText editTextSearch;
    private ImageView btnClearSearch;
    private FeaturedRoomsAdapter featuredRoomsAdapter;
    private EcoInitiativesAdapter ecoInitiativesAdapter;
    private List<Room> featuredRooms = new ArrayList<>();
    private List<Room> filteredRooms = new ArrayList<>();
    private List<EcoInitiative> ecoInitiatives = new ArrayList<>();
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        
        initViews(view);
        setupRecyclerViews();
        setupSearch();
        loadFeaturedRooms();
        loadEcoInitiatives();
        
        return view;
    }

    private void initViews(View view) {
        recyclerViewFeaturedRooms = view.findViewById(R.id.recyclerViewFeaturedRooms);
        recyclerViewEcoInitiatives = view.findViewById(R.id.recyclerViewEcoInitiatives);
        editTextSearch = view.findViewById(R.id.editTextSearch);
        btnClearSearch = view.findViewById(R.id.btnClearSearch);
        
        // Setup "See All" click listener
        TextView textViewSeeAll = view.findViewById(R.id.textViewSeeAll);
        textViewSeeAll.setOnClickListener(v -> {
            // Navigate to rooms tab (MainActivity will handle this)
            if (getActivity() instanceof com.example.ecostayapp.MainActivity) {
                ((com.example.ecostayapp.MainActivity) getActivity()).setCurrentTab(1); // Rooms tab
            }
        });
        
        db = FirebaseFirestore.getInstance();
    }

    private void setupRecyclerViews() {
        // Featured Rooms RecyclerView
        featuredRoomsAdapter = new FeaturedRoomsAdapter(filteredRooms, room -> {
            Intent intent = new Intent(getActivity(), RoomBookingActivity.class);
            intent.putExtra("room", room);
            startActivity(intent);
        });
        recyclerViewFeaturedRooms.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        recyclerViewFeaturedRooms.setAdapter(featuredRoomsAdapter);

        // Eco Initiatives RecyclerView
        ecoInitiativesAdapter = new EcoInitiativesAdapter(ecoInitiatives);
        recyclerViewEcoInitiatives.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewEcoInitiatives.setAdapter(ecoInitiativesAdapter);
    }

    private void setupSearch() {
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
    }

    private void filterRooms(String query) {
        filteredRooms.clear();
        
        if (query.isEmpty()) {
            filteredRooms.addAll(featuredRooms);
        } else {
            String lowerQuery = query.toLowerCase();
            for (Room room : featuredRooms) {
                if (room.getName().toLowerCase().contains(lowerQuery) ||
                    room.getDescription().toLowerCase().contains(lowerQuery)) {
                    filteredRooms.add(room);
                }
            }
        }
        
        featuredRoomsAdapter.notifyDataSetChanged();
    }

    private void updateClearButtonVisibility(String text) {
        btnClearSearch.setVisibility(text.isEmpty() ? View.GONE : View.VISIBLE);
    }


    private void loadFeaturedRooms() {
        db.collection("rooms")
                .whereEqualTo("featured", true)
                .limit(5)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    featuredRooms.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Room room = document.toObject(Room.class);
                        room.setId(document.getId());
                        featuredRooms.add(room);
                    }
                    filterRooms(editTextSearch.getText().toString());
                })
                .addOnFailureListener(e -> {
                    // Load sample data if Firestore fails
                    loadSampleRooms();
                });
    }

    private void loadSampleRooms() {
        featuredRooms.clear();
        featuredRooms.add(new Room("Mountain View Cabin", "Luxurious cabin with panoramic mountain views", 45.0, "https://example.com/cabin1.jpg", 4, true));
        featuredRooms.add(new Room("Eco Pod", "Sustainable pod with modern amenities", 30.0, "https://example.com/ecopod1.jpg", 2, true));
        featuredRooms.add(new Room("Treehouse Suite", "Unique treehouse experience in nature", 60.0, "https://example.com/treehouse1.jpg", 2, true));
        filterRooms(editTextSearch.getText().toString());
    }

    private void loadEcoInitiatives() {
        android.util.Log.d("EcoInitiatives", "Loading eco initiatives from Firebase...");
        db.collection("eco_initiatives")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    android.util.Log.d("EcoInitiatives", "Firebase query successful. Documents: " + queryDocumentSnapshots.size());
                    ecoInitiatives.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        EcoInitiative initiative = document.toObject(EcoInitiative.class);
                        android.util.Log.d("EcoInitiatives", "Loaded initiative: " + initiative.getTitle());
                        ecoInitiatives.add(initiative);
                    }
                    android.util.Log.d("EcoInitiatives", "Total initiatives loaded: " + ecoInitiatives.size());
                    ecoInitiativesAdapter.notifyDataSetChanged();
                    
                    // If no data from Firebase, load sample data
                    if (ecoInitiatives.isEmpty()) {
                        android.util.Log.d("EcoInitiatives", "No data from Firebase, loading sample data...");
                        loadSampleEcoInitiatives();
                    }
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e("EcoInitiatives", "Firebase query failed: " + e.getMessage());
                    // Load sample data if Firestore fails
                    loadSampleEcoInitiatives();
                });
    }

    private void loadSampleEcoInitiatives() {
        android.util.Log.d("EcoInitiatives", "Loading sample eco initiatives...");
        ecoInitiatives.clear();
        ecoInitiatives.add(new EcoInitiative("Solar Power System", "100% renewable energy from 500+ solar panels, reducing carbon footprint by 80%", "https://example.com/solar.jpg"));
        ecoInitiatives.add(new EcoInitiative("Rainwater Harvesting", "Advanced water collection system saving 50,000 gallons monthly", "https://example.com/water.jpg"));
        ecoInitiatives.add(new EcoInitiative("Zero Waste Policy", "Complete composting system and recycling program achieving 95% waste diversion", "https://example.com/waste.jpg"));
        ecoInitiatives.add(new EcoInitiative("Local Community Support", "Sourcing 90% of supplies from local farmers and artisans", "https://example.com/local.jpg"));
        ecoInitiatives.add(new EcoInitiative("Biodiversity Conservation", "Protected 50 acres of native forest and wildlife habitat", "https://example.com/biodiversity.jpg"));
        ecoInitiatives.add(new EcoInitiative("Organic Garden", "Chemical-free vegetable garden providing fresh produce for guests", "https://example.com/garden.jpg"));
        ecoInitiatives.add(new EcoInitiative("Eco-Friendly Amenities", "Biodegradable toiletries and reusable water bottles for all guests", "https://example.com/amenities.jpg"));
        ecoInitiatives.add(new EcoInitiative("Carbon Offset Program", "Tree planting initiative offsetting 100% of guest travel emissions", "https://example.com/carbon.jpg"));
        ecoInitiatives.add(new EcoInitiative("Wildlife Monitoring", "24/7 camera system tracking and protecting local wildlife species", "https://example.com/wildlife.jpg"));
        ecoInitiatives.add(new EcoInitiative("Sustainable Architecture", "Buildings constructed with locally-sourced, eco-friendly materials", "https://example.com/architecture.jpg"));
        ecoInitiatives.add(new EcoInitiative("Educational Programs", "Daily workshops on sustainability and environmental conservation", "https://example.com/education.jpg"));
        ecoInitiatives.add(new EcoInitiative("Green Transportation", "Electric vehicle charging stations and bicycle rental program", "https://example.com/transport.jpg"));
        
        android.util.Log.d("EcoInitiatives", "Sample initiatives loaded: " + ecoInitiatives.size());
        ecoInitiativesAdapter.notifyDataSetChanged();
    }
}
