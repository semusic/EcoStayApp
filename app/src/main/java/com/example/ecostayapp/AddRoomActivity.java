package com.example.ecostayapp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.ecostayapp.models.Room;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AddRoomActivity extends AppCompatActivity {

    private EditText edtRoomName, edtDescription, edtPrice, edtMaxGuests, edtType, edtAmenities;
    private CheckBox chkFeatured, chkAvailable;
    private Button btnSave, btnCancel, btnSelectImage;
    private ImageView imageViewPreview;
    private TextView textViewImageStatus;
    private ProgressBar progressBar;
    
    private FirebaseFirestore db;
    
    private Room editingRoom = null;
    private Uri selectedImageUri = null;
    private String currentImagePath = null;

    // Activity result launcher for image picker
    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_room);

        initViews();
        setupFirebase();
        setupImagePicker();
        checkIfEditing();
        setupClickListeners();
    }

    private void initViews() {
        edtRoomName = findViewById(R.id.edtRoomName);
        edtDescription = findViewById(R.id.edtDescription);
        edtPrice = findViewById(R.id.edtPrice);
        edtMaxGuests = findViewById(R.id.edtMaxGuests);
        edtType = findViewById(R.id.edtType);
        edtAmenities = findViewById(R.id.edtAmenities);
        chkFeatured = findViewById(R.id.chkFeatured);
        chkAvailable = findViewById(R.id.chkAvailable);
        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);
        btnSelectImage = findViewById(R.id.btnSelectImage);
        imageViewPreview = findViewById(R.id.imageViewPreview);
        textViewImageStatus = findViewById(R.id.textViewImageStatus);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupFirebase() {
        db = FirebaseFirestore.getInstance();
    }

    private void setupImagePicker() {
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri uri = result.getData().getData();
                        if (uri != null) {
                            selectedImageUri = uri;
                            
                            // Show preview
                            try {
                                Glide.with(this)
                                        .load(selectedImageUri)
                                        .placeholder(R.drawable.room_placeholder)
                                        .error(R.drawable.room_placeholder)
                                        .into(imageViewPreview);
                                
                                textViewImageStatus.setText("Image selected ✓");
                                textViewImageStatus.setTextColor(getResources().getColor(R.color.success_color));
                                
                                Toast.makeText(this, "Image loaded successfully!", Toast.LENGTH_SHORT).show();
                            } catch (Exception e) {
                                Toast.makeText(this, "Failed to load image: " + e.getMessage(), 
                                             Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show();
                        }
                    } else if (result.getResultCode() == RESULT_CANCELED) {
                        Toast.makeText(this, "Image selection cancelled", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void checkIfEditing() {
        if (getIntent().hasExtra("room")) {
            editingRoom = (Room) getIntent().getSerializableExtra("room");
            if (editingRoom != null) {
                fillRoomData(editingRoom);
            }
        }
    }

    private void fillRoomData(Room room) {
        edtRoomName.setText(room.getName());
        edtDescription.setText(room.getDescription());
        edtPrice.setText(String.valueOf(room.getPricePerNight()));
        edtMaxGuests.setText(String.valueOf(room.getMaxGuests()));
        edtType.setText(room.getType());
        edtAmenities.setText(room.getAmenities());
        chkFeatured.setChecked(room.isFeatured());
        chkAvailable.setChecked(room.isAvailable());
        
        // Load existing image from local storage
        currentImagePath = room.getImageUrl();
        if (currentImagePath != null && !currentImagePath.isEmpty()) {
            File imageFile = new File(currentImagePath);
            if (imageFile.exists()) {
                Glide.with(this)
                        .load(imageFile)
                        .placeholder(R.drawable.room_placeholder)
                        .into(imageViewPreview);
                textViewImageStatus.setText("Current image loaded");
            } else {
                // Fallback: try as URL for backward compatibility
                Glide.with(this)
                        .load(currentImagePath)
                        .placeholder(R.drawable.room_placeholder)
                        .into(imageViewPreview);
                textViewImageStatus.setText("Current image loaded");
            }
        }
    }

    private void setupClickListeners() {
        btnSave.setOnClickListener(v -> saveRoom());
        btnCancel.setOnClickListener(v -> finish());
        
        btnSelectImage.setOnClickListener(v -> openImagePicker());
    }

    private void openImagePicker() {
        try {
            // Try multiple intent actions for better compatibility
            Intent pickIntent = new Intent(Intent.ACTION_PICK);
            pickIntent.setType("image/*");
            
            Intent getContentIntent = new Intent(Intent.ACTION_GET_CONTENT);
            getContentIntent.setType("image/*");
            getContentIntent.addCategory(Intent.CATEGORY_OPENABLE);
            
            // Create chooser with both options
            Intent chooserIntent = Intent.createChooser(getContentIntent, "Select Room Image");
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{pickIntent});
            
            imagePickerLauncher.launch(chooserIntent);
        } catch (Exception e) {
            Toast.makeText(this, "Error opening gallery: " + e.getMessage(), 
                         Toast.LENGTH_SHORT).show();
            
            // Fallback: Try simple ACTION_PICK
            try {
                Intent fallbackIntent = new Intent(Intent.ACTION_PICK);
                fallbackIntent.setType("image/*");
                imagePickerLauncher.launch(fallbackIntent);
            } catch (Exception ex) {
                Toast.makeText(this, "Unable to open image picker. Please check app permissions.", 
                             Toast.LENGTH_LONG).show();
            }
        }
    }

    private void saveRoom() {
        String name = edtRoomName.getText().toString().trim();
        String description = edtDescription.getText().toString().trim();
        String priceStr = edtPrice.getText().toString().trim();
        String maxGuestsStr = edtMaxGuests.getText().toString().trim();
        String type = edtType.getText().toString().trim();
        String amenities = edtAmenities.getText().toString().trim();

        // Validation
        if (TextUtils.isEmpty(name)) {
            edtRoomName.setError("Room name is required");
            edtRoomName.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(description)) {
            edtDescription.setError("Description is required");
            edtDescription.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(priceStr)) {
            edtPrice.setError("Price is required");
            edtPrice.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(maxGuestsStr)) {
            edtMaxGuests.setError("Max guests is required");
            edtMaxGuests.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(type)) {
            edtType.setError("Room type is required");
            edtType.requestFocus();
            return;
        }

        double price;
        int maxGuests;

        try {
            price = Double.parseDouble(priceStr);
            maxGuests = Integer.parseInt(maxGuestsStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid price or max guests", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if new image is selected or editing existing room
        if (selectedImageUri != null) {
            // Save new image to local storage
            saveImageAndRoom(name, description, price, maxGuests, type, amenities);
        } else if (editingRoom != null && currentImagePath != null) {
            // Editing existing room, keep current image
            saveRoomToFirestore(name, description, price, maxGuests, type, amenities, currentImagePath);
        } else {
            // No image selected for new room
            Toast.makeText(this, "Please select an image for the room", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveImageAndRoom(String name, String description, double price, 
                                   int maxGuests, String type, String amenities) {
        showProgress(true);
        textViewImageStatus.setText("Saving image...");

        try {
            // Create directory for room images
            File imageDir = new File(getFilesDir(), "room_images");
            if (!imageDir.exists()) {
                imageDir.mkdirs();
            }

            // Create unique filename
            String filename = System.currentTimeMillis() + "_" + UUID.randomUUID().toString() + ".jpg";
            File imageFile = new File(imageDir, filename);

            // Copy image from URI to internal storage
            InputStream inputStream = getContentResolver().openInputStream(selectedImageUri);
            if (inputStream != null) {
                // Decode and compress image to save space
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                inputStream.close();

                // Compress and save
                FileOutputStream outputStream = new FileOutputStream(imageFile);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream);
                outputStream.flush();
                outputStream.close();

                // Recycle bitmap
                bitmap.recycle();

                // Get file path
                String imagePath = imageFile.getAbsolutePath();
                
                android.util.Log.d("AddRoomActivity", "Image saved to: " + imagePath);
                textViewImageStatus.setText("Image saved ✓");

                // Save room with local image path
                saveRoomToFirestore(name, description, price, maxGuests, type, amenities, imagePath);
            } else {
                showProgress(false);
                Toast.makeText(this, "Failed to read image", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            showProgress(false);
            android.util.Log.e("AddRoomActivity", "Error saving image: " + e.getMessage());
            e.printStackTrace();
            Toast.makeText(this, "Failed to save image: " + e.getMessage(), 
                         Toast.LENGTH_LONG).show();
        }
    }


    private void saveRoomToFirestore(String name, String description, double price, 
                                     int maxGuests, String type, String amenities, String imageUrl) {
        Map<String, Object> roomData = new HashMap<>();
        roomData.put("name", name);
        roomData.put("description", description);
        roomData.put("price", price);
        roomData.put("maxGuests", maxGuests);
        roomData.put("type", type);
        roomData.put("amenities", amenities);
        roomData.put("imageUrl", imageUrl);
        roomData.put("featured", chkFeatured.isChecked());
        roomData.put("available", chkAvailable.isChecked());
        roomData.put("updatedAt", System.currentTimeMillis());

        if (editingRoom != null) {
            // Update existing room - don't modify createdAt
            db.collection("rooms").document(editingRoom.getId())
                    .update(roomData)
                    .addOnSuccessListener(aVoid -> {
                        showProgress(false);
                        Toast.makeText(this, "Room updated successfully!", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        showProgress(false);
                        Toast.makeText(this, "Failed to update room: " + e.getMessage(), 
                                     Toast.LENGTH_LONG).show();
                    });
        } else {
            // Add new room with createdAt timestamp
            roomData.put("createdAt", System.currentTimeMillis());
            
            db.collection("rooms")
                    .add(roomData)
                    .addOnSuccessListener(documentReference -> {
                        showProgress(false);
                        Toast.makeText(this, "Room added successfully!", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        showProgress(false);
                        Toast.makeText(this, "Failed to add room: " + e.getMessage(), 
                                     Toast.LENGTH_LONG).show();
                    });
        }
    }

    private void showProgress(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnSave.setEnabled(!show);
        btnCancel.setEnabled(!show);
        btnSelectImage.setEnabled(!show);
    }
}
