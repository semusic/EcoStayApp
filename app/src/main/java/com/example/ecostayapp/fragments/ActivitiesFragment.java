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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ecostayapp.R;
import com.example.ecostayapp.ActivityBookingActivity;
import com.example.ecostayapp.adapters.ActivitiesAdapter;
import com.example.ecostayapp.models.Activity;
import com.example.ecostayapp.FilterDialog;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ActivitiesFragment extends Fragment {

    private RecyclerView recyclerViewActivities;
    private ActivitiesAdapter activitiesAdapter;
    private List<Activity> activities = new ArrayList<>();
    private List<Activity> filteredActivities = new ArrayList<>();
    private EditText editTextSearch;
    private ImageView btnClearSearch;
    private ImageView btnCalendar;
    private ImageView btnFilter;
    private FirebaseFirestore db;
    
    // Filter state variables
    private String currentPriceRange = "";
    private String currentType = "";
    private String currentCategory = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_activities, container, false);
        
        initViews(view);
        setupRecyclerView();
        setupSearchAndFilter();
        loadActivities();
        
        return view;
    }

    private void initViews(View view) {
        recyclerViewActivities = view.findViewById(R.id.recyclerViewActivities);
        editTextSearch = view.findViewById(R.id.editTextSearch);
        btnClearSearch = view.findViewById(R.id.btnClearSearch);
        btnCalendar = view.findViewById(R.id.btnCalendar);
        btnFilter = view.findViewById(R.id.btnFilter);
        db = FirebaseFirestore.getInstance();
    }

    private void setupRecyclerView() {
        activitiesAdapter = new ActivitiesAdapter(filteredActivities, activity -> {
            Intent intent = new Intent(getActivity(), ActivityBookingActivity.class);
            intent.putExtra("activity", activity);
            startActivity(intent);
        });
        recyclerViewActivities.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewActivities.setAdapter(activitiesAdapter);
    }

    private void setupSearchAndFilter() {
        // Search functionality
        editTextSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterActivities(s.toString());
                updateClearButtonVisibility(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Clear search button
        btnClearSearch.setOnClickListener(v -> {
            editTextSearch.setText("");
            filterActivities("");
            updateClearButtonVisibility("");
        });

        // Calendar button - opens calendar view
        btnCalendar.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), com.example.ecostayapp.ActivitiesCalendarActivity.class);
            startActivity(intent);
        });

        // Filter button
        btnFilter.setOnClickListener(v -> {
            showFilterDialog();
        });
    }

    private void filterActivities(String query) {
        filteredActivities.clear();
        
        if (query.isEmpty()) {
            filteredActivities.addAll(activities);
        } else {
            String lowerQuery = query.toLowerCase();
            for (Activity activity : activities) {
                if (activity.getName().toLowerCase().contains(lowerQuery) ||
                    activity.getDescription().toLowerCase().contains(lowerQuery) ||
                    activity.getCategory().toLowerCase().contains(lowerQuery) ||
                    activity.getDifficulty().toLowerCase().contains(lowerQuery)) {
                    filteredActivities.add(activity);
                }
            }
        }
        
        activitiesAdapter.notifyDataSetChanged();
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
        filteredActivities.clear();
        
        // Debug logging
        android.util.Log.d("ActivitiesFragment", "=== APPLYING FILTERS ===");
        android.util.Log.d("ActivitiesFragment", "PriceRange: " + priceRange);
        android.util.Log.d("ActivitiesFragment", "Category: " + category);
        android.util.Log.d("ActivitiesFragment", "Type: " + type);
        
        for (Activity activity : activities) {
            boolean matchesPrice = true;
            boolean matchesCategory = true;
            
            // Price filter - Convert USD prices to LKR for comparison
            if (!priceRange.isEmpty() && !priceRange.equals("-")) {
                String[] prices = priceRange.split("-");
                if (prices.length == 2) {
                    try {
                        double minPriceLKR = prices[0].isEmpty() ? 0 : Double.parseDouble(prices[0]);
                        double maxPriceLKR = prices[1].isEmpty() ? Double.MAX_VALUE : Double.parseDouble(prices[1]);
                        
                        // Convert stored USD price to LKR (1 USD = 325 LKR)
                        double activityPriceLKR = activity.getPrice() * 325;
                        
                        matchesPrice = activityPriceLKR >= minPriceLKR && activityPriceLKR <= maxPriceLKR;
                        
                        android.util.Log.d("ActivitiesFragment", "Activity: " + activity.getName() + 
                            " Price USD: " + activity.getPrice() + 
                            " Price LKR: " + activityPriceLKR + 
                            " MinPrice LKR: " + minPriceLKR + 
                            " MaxPrice LKR: " + maxPriceLKR + 
                            " Matches: " + matchesPrice);
                    } catch (NumberFormatException e) {
                        android.util.Log.e("ActivitiesFragment", "Error parsing price range: " + priceRange, e);
                        matchesPrice = true;
                    }
                }
            }
            
            // Category filter
            if (!category.equals("All") && !category.isEmpty()) {
                matchesCategory = activity.getCategory() != null && activity.getCategory().equalsIgnoreCase(category);
                android.util.Log.d("ActivitiesFragment", "Activity: " + activity.getName() + 
                    " Category: " + activity.getCategory() + 
                    " FilterCategory: " + category + 
                    " Matches: " + matchesCategory);
            }
            
            if (matchesPrice && matchesCategory) {
                filteredActivities.add(activity);
                android.util.Log.d("ActivitiesFragment", "Activity added to filtered list: " + activity.getName());
            }
        }
        
        android.util.Log.d("ActivitiesFragment", "Total activities: " + activities.size());
        android.util.Log.d("ActivitiesFragment", "Filtered activities: " + filteredActivities.size());
        
        activitiesAdapter.notifyDataSetChanged();
        
        if (filteredActivities.isEmpty()) {
            Toast.makeText(getContext(), "No activities match the selected filters", Toast.LENGTH_SHORT).show();
        }
    }

    private void clearFilters() {
        filteredActivities.clear();
        filteredActivities.addAll(activities);
        activitiesAdapter.notifyDataSetChanged();
    }

    private void loadActivities() {
        db.collection("activities")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    activities.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Activity activity = document.toObject(Activity.class);
                        activity.setId(document.getId());
                        activities.add(activity);
                    }
                    filterActivities(editTextSearch.getText().toString());
                })
                .addOnFailureListener(e -> {
                    loadSampleActivities();
                });
    }

    private void loadSampleActivities() {
        activities.clear();
        activities.add(new Activity("Guided Nature Hike", "Explore the beautiful mountain trails with our expert guide", 45.0, "https://example.com/hike1.jpg", "3 hours", 12, "Moderate", "Outdoor"));
        activities.add(new Activity("Eco Tour", "Learn about sustainable practices and local ecosystem", 35.0, "https://example.com/ecotour1.jpg", "2 hours", 15, "Easy", "Educational"));
        activities.add(new Activity("Sustainability Workshop", "Hands-on workshop on sustainable living practices", 25.0, "https://example.com/workshop1.jpg", "1.5 hours", 20, "Easy", "Educational"));
        activities.add(new Activity("Bird Watching", "Observe local bird species in their natural habitat", 30.0, "https://example.com/birdwatch1.jpg", "2 hours", 10, "Easy", "Wildlife"));
        activities.add(new Activity("Nature Photography", "Capture the beauty of nature with professional guidance", 55.0, "https://example.com/photography1.jpg", "3 hours", 8, "Moderate", "Creative"));
        activities.add(new Activity("Sunrise Yoga", "Morning yoga session in the peaceful mountain setting", 20.0, "https://example.com/yoga1.jpg", "1 hour", 25, "Easy", "Wellness"));
        filterActivities(editTextSearch.getText().toString());
    }
}
