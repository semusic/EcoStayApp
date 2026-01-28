package com.example.ecostayapp;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;

import com.example.ecostayapp.R;

public class FilterDialog extends Dialog {

    public interface FilterListener {
        void onFilterApplied(String priceRange, String category, String type);
        void onFilterCleared();
    }

    private FilterListener listener;
    private EditText edtMinPrice, edtMaxPrice;
    private Spinner spinnerCategory, spinnerType;
    private Button btnApply, btnClear;

    public FilterDialog(@NonNull Context context, FilterListener listener) {
        super(context);
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_filter);

        initViews();
        setupSpinners();
        setupClickListeners();
    }

    private void initViews() {
        edtMinPrice = findViewById(R.id.edtMinPrice);
        edtMaxPrice = findViewById(R.id.edtMaxPrice);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        spinnerType = findViewById(R.id.spinnerType);
        btnApply = findViewById(R.id.btnApply);
        btnClear = findViewById(R.id.btnClear);
    }

    private void setupSpinners() {
        // Categories for activities
        String[] categories = {"All", "Adventure", "Wellness", "Nature", "Cultural", "Educational"};
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(getContext(), 
                android.R.layout.simple_spinner_item, categories);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(categoryAdapter);

        // Types for rooms
        String[] types = {"All", "Standard", "Deluxe", "Suite", "Cabin", "Eco Pod"};
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(getContext(), 
                android.R.layout.simple_spinner_item, types);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerType.setAdapter(typeAdapter);
    }

    private void setupClickListeners() {
        btnApply.setOnClickListener(v -> {
            String minPrice = edtMinPrice.getText().toString().trim();
            String maxPrice = edtMaxPrice.getText().toString().trim();
            String category = spinnerCategory.getSelectedItem().toString();
            String type = spinnerType.getSelectedItem().toString();
            
            String priceRange = "";
            if (!minPrice.isEmpty() || !maxPrice.isEmpty()) {
                // Handle cases where only min or only max is provided
                if (!minPrice.isEmpty() && !maxPrice.isEmpty()) {
                    priceRange = minPrice + "-" + maxPrice;
                } else if (!minPrice.isEmpty()) {
                    priceRange = minPrice + "-";
                } else if (!maxPrice.isEmpty()) {
                    priceRange = "-" + maxPrice;
                }
            }
            
            if (listener != null) {
                listener.onFilterApplied(priceRange, category, type);
            }
            dismiss();
        });

        btnClear.setOnClickListener(v -> {
            edtMinPrice.setText("");
            edtMaxPrice.setText("");
            spinnerCategory.setSelection(0);
            spinnerType.setSelection(0);
            
            if (listener != null) {
                listener.onFilterCleared();
            }
            dismiss();
        });

        findViewById(R.id.btnClose).setOnClickListener(v -> dismiss());
    }
}



