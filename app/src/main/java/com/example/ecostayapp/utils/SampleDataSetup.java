package com.example.ecostayapp.utils;

import android.util.Log;

import com.example.ecostayapp.models.Room;
import com.example.ecostayapp.models.Activity;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class SampleDataSetup {

    public static void setupSampleRooms() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        
        // Clear existing rooms first
        db.collection("rooms").get().addOnSuccessListener(queryDocumentSnapshots -> {
            for (int i = 0; i < queryDocumentSnapshots.size(); i++) {
                String docId = queryDocumentSnapshots.getDocuments().get(i).getId();
                db.collection("rooms").document(docId).delete();
            }
            
            // Add sample rooms with affordable prices (USD to LKR conversion: 1 USD = 325 LKR)
            addSampleRoom(db, "Mountain View Cabin", 
                "Luxurious cabin with panoramic mountain views and eco-friendly amenities. Perfect for couples seeking a romantic getaway with breathtaking scenery.",
                45.0, "room_cabin", 4, true, "Cabin", "WiFi, Kitchen, Balcony, Mountain Views", 4.5, 23);
                
            addSampleRoom(db, "Eco Pod", 
                "Sustainable pod with modern amenities and minimal environmental impact. Features solar power and recycled materials.",
                30.0, "room_ecopod", 2, true, "Pod", "WiFi, Solar Power, Recycled Materials", 4.2, 18);
                
            addSampleRoom(db, "Treehouse Suite", 
                "Unique treehouse experience in nature. Elevated accommodation with stunning forest views and sustainable design.",
                60.0, "room_treehouse", 2, true, "Treehouse", "WiFi, Forest Views, Eco-Friendly", 4.8, 31);
                
            addSampleRoom(db, "Sustainable Suite", 
                "Eco-friendly suite with all modern amenities. Features energy-efficient appliances and sustainable materials.",
                38.0, "room_suite", 3, false, "Suite", "WiFi, Kitchen, Energy Efficient", 4.3, 15);
                
            addSampleRoom(db, "Forest Bungalow", 
                "Cozy bungalow surrounded by forest. Perfect for nature lovers seeking tranquility and comfort.",
                25.0, "room_bungalow", 2, false, "Bungalow", "WiFi, Forest Access, Cozy", 4.1, 12);
                
            addSampleRoom(db, "Luxury Eco Villa", 
                "Premium villa with sustainable features and luxury amenities. Spacious accommodation for families.",
                75.0, "room_villa", 6, false, "Villa", "WiFi, Kitchen, Pool, Garden", 4.7, 28);
                
            Log.d("SampleDataSetup", "Sample rooms added successfully");
        }).addOnFailureListener(e -> {
            Log.e("SampleDataSetup", "Failed to clear existing rooms: " + e.getMessage());
        });
    }
    
    public static void setupSampleActivities() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        
        // Clear existing activities first
        db.collection("activities").get().addOnSuccessListener(queryDocumentSnapshots -> {
            for (int i = 0; i < queryDocumentSnapshots.size(); i++) {
                String docId = queryDocumentSnapshots.getDocuments().get(i).getId();
                db.collection("activities").document(docId).delete();
            }
            
            // Add sample activities with proper prices
            addSampleActivity(db, "Guided Nature Hike", 
                "Explore beautiful mountain trails with an experienced guide. Discover local flora and fauna.",
                45.0, "activity_hike", "3 hours", 12, "Moderate", "Outdoor", "John Guide", 4.8, 15);
                
            addSampleActivity(db, "Bird Watching Tour", 
                "Early morning bird watching tour to spot local and migratory birds in their natural habitat.",
                35.0, "activity_birdwatching", "2 hours", 8, "Easy", "Wildlife", "Sarah Ornithologist", 4.6, 22);
                
            addSampleActivity(db, "Sustainable Farming Workshop", 
                "Learn about organic farming techniques and sustainable agriculture practices.",
                55.0, "activity_farming", "4 hours", 15, "Moderate", "Educational", "Mike Farmer", 4.4, 18);
                
            addSampleActivity(db, "Mountain Biking Adventure", 
                "Exciting mountain biking trails through scenic routes with professional equipment.",
                65.0, "activity_biking", "3 hours", 10, "Difficult", "Adventure", "Tom Cyclist", 4.7, 25);
                
            addSampleActivity(db, "Meditation in Nature", 
                "Peaceful meditation sessions in natural surroundings for relaxation and mindfulness.",
                25.0, "activity_meditation", "1.5 hours", 20, "Easy", "Wellness", "Lisa Zen", 4.9, 35);
                
            Log.d("SampleDataSetup", "Sample activities added successfully");
        }).addOnFailureListener(e -> {
            Log.e("SampleDataSetup", "Failed to clear existing activities: " + e.getMessage());
        });
    }
    
    public static void setupSampleEcoInitiatives() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        
        // Clear existing eco initiatives first
        db.collection("eco_initiatives").get().addOnSuccessListener(queryDocumentSnapshots -> {
            for (int i = 0; i < queryDocumentSnapshots.size(); i++) {
                String docId = queryDocumentSnapshots.getDocuments().get(i).getId();
                db.collection("eco_initiatives").document(docId).delete();
            }
            
            // Add sample eco initiatives
            addSampleEcoInitiative(db, "Solar Power System", 
                "100% renewable energy from 500+ solar panels, reducing carbon footprint by 80%", 
                "https://example.com/solar.jpg");
                
            addSampleEcoInitiative(db, "Rainwater Harvesting", 
                "Advanced water collection system saving 50,000 gallons monthly", 
                "https://example.com/water.jpg");
                
            addSampleEcoInitiative(db, "Zero Waste Policy", 
                "Complete composting system and recycling program achieving 95% waste diversion", 
                "https://example.com/waste.jpg");
                
            addSampleEcoInitiative(db, "Local Community Support", 
                "Sourcing 90% of supplies from local farmers and artisans", 
                "https://example.com/local.jpg");
                
            addSampleEcoInitiative(db, "Biodiversity Conservation", 
                "Protected 50 acres of native forest and wildlife habitat", 
                "https://example.com/biodiversity.jpg");
                
            addSampleEcoInitiative(db, "Organic Garden", 
                "Chemical-free vegetable garden providing fresh produce for guests", 
                "https://example.com/garden.jpg");
                
            addSampleEcoInitiative(db, "Eco-Friendly Amenities", 
                "Biodegradable toiletries and reusable water bottles for all guests", 
                "https://example.com/amenities.jpg");
                
            addSampleEcoInitiative(db, "Carbon Offset Program", 
                "Tree planting initiative offsetting 100% of guest travel emissions", 
                "https://example.com/carbon.jpg");
                
            Log.d("SampleDataSetup", "Sample eco initiatives added successfully");
        }).addOnFailureListener(e -> {
            Log.e("SampleDataSetup", "Failed to clear existing eco initiatives: " + e.getMessage());
        });
    }
    
    private static void addSampleRoom(FirebaseFirestore db, String name, String description, 
                                    double pricePerNight, String imageUrl, int maxGuests, 
                                    boolean featured, String type, String amenities, 
                                    double rating, int reviewCount) {
        Map<String, Object> roomData = new HashMap<>();
        roomData.put("name", name);
        roomData.put("description", description);
        roomData.put("pricePerNight", pricePerNight);
        roomData.put("imageUrl", imageUrl);
        roomData.put("maxGuests", maxGuests);
        roomData.put("featured", featured);
        roomData.put("type", type);
        roomData.put("amenities", amenities);
        roomData.put("rating", rating);
        roomData.put("reviewCount", reviewCount);
        roomData.put("available", true);
        roomData.put("createdAt", System.currentTimeMillis());
        
        db.collection("rooms").add(roomData)
            .addOnSuccessListener(documentReference -> {
                Log.d("SampleDataSetup", "Room added: " + name + " with ID: " + documentReference.getId());
            })
            .addOnFailureListener(e -> {
                Log.e("SampleDataSetup", "Failed to add room " + name + ": " + e.getMessage());
            });
    }
    
    private static void addSampleActivity(FirebaseFirestore db, String name, String description, 
                                        double price, String imageUrl, String duration, 
                                        int maxParticipants, String difficulty, String category, 
                                        String instructor, double rating, int reviewCount) {
        Map<String, Object> activityData = new HashMap<>();
        activityData.put("name", name);
        activityData.put("description", description);
        activityData.put("price", price);
        activityData.put("imageUrl", imageUrl);
        activityData.put("duration", duration);
        activityData.put("maxParticipants", maxParticipants);
        activityData.put("difficulty", difficulty);
        activityData.put("category", category);
        activityData.put("instructor", instructor);
        activityData.put("rating", rating);
        activityData.put("reviewCount", reviewCount);
        activityData.put("available", true);
        activityData.put("createdAt", System.currentTimeMillis());
        
        db.collection("activities").add(activityData)
            .addOnSuccessListener(documentReference -> {
                Log.d("SampleDataSetup", "Activity added: " + name + " with ID: " + documentReference.getId());
            })
            .addOnFailureListener(e -> {
                Log.e("SampleDataSetup", "Failed to add activity " + name + ": " + e.getMessage());
            });
    }
    
    private static void addSampleEcoInitiative(FirebaseFirestore db, String title, String description, String imageUrl) {
        Map<String, Object> ecoData = new HashMap<>();
        ecoData.put("title", title);
        ecoData.put("description", description);
        ecoData.put("imageUrl", imageUrl);
        ecoData.put("createdAt", System.currentTimeMillis());
        
        db.collection("eco_initiatives").add(ecoData)
            .addOnSuccessListener(documentReference -> {
                Log.d("SampleDataSetup", "Eco initiative added: " + title + " with ID: " + documentReference.getId());
            })
            .addOnFailureListener(e -> {
                Log.e("SampleDataSetup", "Failed to add eco initiative " + title + ": " + e.getMessage());
            });
    }
}
