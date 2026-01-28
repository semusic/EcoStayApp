package com.example.ecostayapp;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ecostayapp.adapters.PaymentsAdapter;
import com.example.ecostayapp.models.Payment;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class PaymentsActivity extends AppCompatActivity {

    private ImageView btnBack;
    private TextView textViewTotalPayments, textViewTotalAmount, textViewPendingPayments;
    private RecyclerView recyclerViewPayments;
    private PaymentsAdapter paymentsAdapter;
    private List<Payment> payments = new ArrayList<>();
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payments);

        initViews();
        setupClickListeners();
        setupRecyclerView();
        loadPayments();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        textViewTotalPayments = findViewById(R.id.textViewTotalPayments);
        textViewTotalAmount = findViewById(R.id.textViewTotalAmount);
        textViewPendingPayments = findViewById(R.id.textViewPendingPayments);
        recyclerViewPayments = findViewById(R.id.recyclerViewPayments);
        db = FirebaseFirestore.getInstance();
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        paymentsAdapter = new PaymentsAdapter(payments);
        recyclerViewPayments.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewPayments.setAdapter(paymentsAdapter);
    }

    private void loadPayments() {
        db.collection("payments")
                .orderBy("paymentDate", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    payments.clear();
                    
                    double totalAmount = 0;
                    int pendingCount = 0;
                    
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Payment payment = document.toObject(Payment.class);
                        payment.setId(document.getId());
                        payments.add(payment);
                        
                        if (payment.getStatus().equals("completed")) {
                            totalAmount += payment.getAmount();
                        } else if (payment.getStatus().equals("pending")) {
                            pendingCount++;
                        }
                    }
                    
                    paymentsAdapter.notifyDataSetChanged();
                    updateStatistics(payments.size(), totalAmount, pendingCount);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load payments: " + e.getMessage(), 
                                 Toast.LENGTH_SHORT).show();
                });
    }

    private void updateStatistics(int totalPayments, double totalAmount, int pendingPayments) {
        textViewTotalPayments.setText(String.valueOf(totalPayments));
        textViewTotalAmount.setText("LKR " + String.format("%.2f", totalAmount));
        textViewPendingPayments.setText(String.valueOf(pendingPayments));
    }
}
