package com.example.ecostayapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ecostayapp.adapters.AdminRoomsAdapter;
import com.example.ecostayapp.models.Room;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class AdminActivity extends AppCompatActivity {

    private RecyclerView recyclerViewRooms;
    private Button btnAddRoom, btnViewBookings, btnSendNotifications;
    private ImageView btnAdminLogout;
    private AdminRoomsAdapter adminRoomsAdapter;
    private List<Room> rooms = new ArrayList<>();
    private FirebaseFirestore db;
    private com.google.firebase.auth.FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        initViews();
        setupRecyclerView();
        loadRooms();
        setupClickListeners();
    }

    private void initViews() {
        recyclerViewRooms = findViewById(R.id.recyclerViewAdminRooms);
        btnAddRoom = findViewById(R.id.btnAddRoom);
        btnViewBookings = findViewById(R.id.btnViewBookings);
        btnSendNotifications = findViewById(R.id.btnSendNotifications);
        btnAdminLogout = findViewById(R.id.btnAdminLogout);
        db = FirebaseFirestore.getInstance();
        mAuth = com.google.firebase.auth.FirebaseAuth.getInstance();
    }

    private void setupRecyclerView() {
        adminRoomsAdapter = new AdminRoomsAdapter(rooms, new AdminRoomsAdapter.OnRoomActionListener() {
            @Override
            public void onEdit(Room room) {
                editRoom(room);
            }

            @Override
            public void onDelete(Room room) {
                deleteRoom(room);
            }

            @Override
            public void onToggleAvailability(Room room) {
                toggleRoomAvailability(room);
            }
        });
        recyclerViewRooms.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewRooms.setAdapter(adminRoomsAdapter);
    }

    private void setupClickListeners() {
        btnAddRoom.setOnClickListener(v -> {
            startActivity(new Intent(this, AddRoomActivity.class));
        });

        btnViewBookings.setOnClickListener(v -> {
            startActivity(new Intent(this, AdminBookingsActivity.class));
        });

        btnSendNotifications.setOnClickListener(v -> {
            startActivity(new Intent(this, AdminNotificationsActivity.class));
        });

        btnAdminLogout.setOnClickListener(v -> {
            logoutAdmin();
        });
    }

    private void logoutAdmin() {
        // Show confirmation dialog
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout from admin panel?")
                .setPositiveButton("Logout", (dialog, which) -> {
                    mAuth.signOut();
                    Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
                    
                    // Go back to admin login
                    Intent intent = new Intent(this, AdminLoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Cancel", null)
                .show();
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
                    adminRoomsAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load rooms: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void editRoom(Room room) {
        Intent intent = new Intent(this, AddRoomActivity.class);
        intent.putExtra("room", room);
        startActivity(intent);
    }

    private void deleteRoom(Room room) {
        db.collection("rooms").document(room.getId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Room deleted successfully", Toast.LENGTH_SHORT).show();
                    loadRooms(); // Reload the list
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to delete room: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void toggleRoomAvailability(Room room) {
        boolean newAvailability = !room.isAvailable();
        db.collection("rooms").document(room.getId())
                .update("available", newAvailability)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Room availability updated", Toast.LENGTH_SHORT).show();
                    room.setAvailable(newAvailability);
                    adminRoomsAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to update: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadRooms(); // Refresh when returning from add/edit
    }
    
    @Override
    public void onBackPressed() {
        // Prevent back button - admin should only logout
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Exit Admin Panel")
                .setMessage("Do you want to logout from admin panel?")
                .setPositiveButton("Logout", (dialog, which) -> {
                    logoutAdmin();
                })
                .setNegativeButton("Stay", null)
                .show();
    }
    
}

