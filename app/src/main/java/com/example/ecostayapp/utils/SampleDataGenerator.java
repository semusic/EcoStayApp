package com.example.ecostayapp.utils;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;
import com.example.ecostayapp.models.Room;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class to generate and insert sample room data into Firebase Firestore
 * Use this to populate your database with initial room data
 */
public class SampleDataGenerator {
    
    private FirebaseFirestore db;
    
    public SampleDataGenerator() {
        db = FirebaseFirestore.getInstance();
    }
    
    /**
     * Adds all sample rooms to Firestore
     * Call this method once to populate your database
     */
    public void addSampleRooms() {
        WriteBatch batch = db.batch();
        
        // Room 1 - Mountain View Cabin
        Map<String, Object> room1 = new HashMap<>();
        room1.put("name", "Mountain View Cabin");
        room1.put("description", "Luxurious cabin with panoramic mountain views and eco-friendly amenities. Perfect for couples seeking a romantic getaway with breathtaking scenery.");
        room1.put("price", 299.00); // USD price (will be converted to LKR in app)
        room1.put("maxGuests", 4);
        room1.put("type", "Cabin");
        room1.put("amenities", "WiFi, Full Kitchen, Fireplace, Hot Tub, Solar Power, Balcony, Mountain Views");
        room1.put("imageUrl", "https://images.unsplash.com/photo-1449824913935-59a10b8d2000?w=800");
        room1.put("featured", true);
        room1.put("available", true);
        room1.put("createdAt", new Date());
        room1.put("updatedAt", new Date());
        
        // Room 2 - Eco Pod
        Map<String, Object> room2 = new HashMap<>();
        room2.put("name", "Eco Pod");
        room2.put("description", "Sustainable pod with modern amenities and solar power. Perfect for eco-conscious travelers seeking a minimalist yet comfortable experience.");
        room2.put("price", 199.00); // USD price (will be converted to LKR in app)
        room2.put("maxGuests", 2);
        room2.put("type", "Pod");
        room2.put("amenities", "WiFi, Heating, Solar Power, Workspace, Eco-Friendly Materials");
        room2.put("imageUrl", "https://images.unsplash.com/photo-1506905925346-21bda4d32df4?w=800");
        room2.put("featured", false);
        room2.put("available", true);
        room2.put("createdAt", new Date());
        room2.put("updatedAt", new Date());
        
        // Room 3 - Forest Bungalow
        Map<String, Object> room3 = new HashMap<>();
        room3.put("name", "Forest Bungalow");
        room3.put("description", "Cozy bungalow surrounded by lush forest. Experience nature while enjoying modern comfort and sustainable living.");
        room3.put("price", 249.00); // USD price (will be converted to LKR in app)
        room3.put("maxGuests", 3);
        room3.put("type", "Bungalow");
        room3.put("amenities", "WiFi, Kitchen, Fireplace, Garden, Solar Power, Forest Views");
        room3.put("imageUrl", "https://images.unsplash.com/photo-1441974231531-c6227db76b6e?w=800");
        room3.put("featured", true);
        room3.put("available", true);
        room3.put("createdAt", new Date());
        room3.put("updatedAt", new Date());
        
        // Room 4 - Sustainable Suite
        Map<String, Object> room4 = new HashMap<>();
        room4.put("name", "Sustainable Suite");
        room4.put("description", "Spacious eco-friendly suite perfect for families. Multiple bedrooms with sustainable amenities and modern comfort.");
        room4.put("price", 349.00); // USD price (will be converted to LKR in app)
        room4.put("maxGuests", 6);
        room4.put("type", "Suite");
        room4.put("amenities", "WiFi, Full Kitchen, 2 Bedrooms, Living Room, Solar Power, Family-Friendly");
        room4.put("imageUrl", "https://images.unsplash.com/photo-1566073771259-6a8506099945?w=800");
        room4.put("featured", true);
        room4.put("available", true);
        room4.put("createdAt", new Date());
        room4.put("updatedAt", new Date());
        
        // Room 5 - Luxury Eco Villa
        Map<String, Object> room5 = new HashMap<>();
        room5.put("name", "Luxury Eco Villa");
        room5.put("description", "Premium villa with all sustainable features and amenities. Ultimate luxury eco-experience with private pool and garden.");
        room5.put("price", 499.00); // USD price (will be converted to LKR in app)
        room5.put("maxGuests", 8);
        room5.put("type", "Villa");
        room5.put("amenities", "WiFi, Gourmet Kitchen, Pool, Hot Tub, 3 Bedrooms, Solar Power, Garden, Private Entrance");
        room5.put("imageUrl", "https://images.unsplash.com/photo-1571896349842-33c89424de2d?w=800");
        room5.put("featured", true);
        room5.put("available", true);
        room5.put("createdAt", new Date());
        room5.put("updatedAt", new Date());
        
        // Add rooms to batch
        batch.set(db.collection("rooms").document("room1"), room1);
        batch.set(db.collection("rooms").document("room2"), room2);
        batch.set(db.collection("rooms").document("room3"), room3);
        batch.set(db.collection("rooms").document("room4"), room4);
        batch.set(db.collection("rooms").document("room5"), room5);
        
        // Commit batch
        batch.commit()
            .addOnSuccessListener(aVoid -> {
                System.out.println("Sample rooms added successfully!");
            })
            .addOnFailureListener(e -> {
                System.err.println("Error adding sample rooms: " + e.getMessage());
            });
    }
    
    /**
     * Adds sample eco initiatives data
     */
    public void addSampleEcoInitiatives() {
        WriteBatch batch = db.batch();
        
        // Eco Initiative 1
        Map<String, Object> initiative1 = new HashMap<>();
        initiative1.put("title", "Solar Power System");
        initiative1.put("description", "100% renewable energy from 500+ solar panels, reducing carbon footprint by 80%");
        initiative1.put("imageUrl", "https://images.unsplash.com/photo-1497435334941-8c899ee9e8e9?w=800");
        initiative1.put("category", "Energy");
        initiative1.put("createdAt", new Date());
        
        // Eco Initiative 2
        Map<String, Object> initiative2 = new HashMap<>();
        initiative2.put("title", "Rainwater Harvesting");
        initiative2.put("description", "Advanced water collection system saving 50,000 gallons monthly");
        initiative2.put("imageUrl", "https://images.unsplash.com/photo-1558618666-fcd25c85cd64?w=800");
        initiative2.put("category", "Water Conservation");
        initiative2.put("createdAt", new Date());
        
        // Eco Initiative 3
        Map<String, Object> initiative3 = new HashMap<>();
        initiative3.put("title", "Zero Waste Policy");
        initiative3.put("description", "Complete composting system and recycling program achieving 95% waste diversion");
        initiative3.put("imageUrl", "https://images.unsplash.com/photo-1500937386664-56d1dfef3854?w=800");
        initiative3.put("category", "Waste Management");
        initiative3.put("createdAt", new Date());
        
        // Eco Initiative 4
        Map<String, Object> initiative4 = new HashMap<>();
        initiative4.put("title", "Local Community Support");
        initiative4.put("description", "Sourcing 90% of supplies from local farmers and artisans");
        initiative4.put("imageUrl", "https://images.unsplash.com/photo-1464207687429-7505649dae38?w=800");
        initiative4.put("category", "Community");
        initiative4.put("createdAt", new Date());
        
        // Eco Initiative 5
        Map<String, Object> initiative5 = new HashMap<>();
        initiative5.put("title", "Biodiversity Conservation");
        initiative5.put("description", "Protected 50 acres of native forest and wildlife habitat");
        initiative5.put("imageUrl", "https://images.unsplash.com/photo-1441974231531-c6227db76b6e?w=800");
        initiative5.put("category", "Conservation");
        initiative5.put("createdAt", new Date());
        
        // Eco Initiative 6
        Map<String, Object> initiative6 = new HashMap<>();
        initiative6.put("title", "Organic Garden");
        initiative6.put("description", "Chemical-free vegetable garden providing fresh produce for guests");
        initiative6.put("imageUrl", "https://images.unsplash.com/photo-1416879595882-3373a0480b5b?w=800");
        initiative6.put("category", "Agriculture");
        initiative6.put("createdAt", new Date());
        
        // Add initiatives to batch
        batch.set(db.collection("ecoInitiatives").document("initiative1"), initiative1);
        batch.set(db.collection("ecoInitiatives").document("initiative2"), initiative2);
        batch.set(db.collection("ecoInitiatives").document("initiative3"), initiative3);
        batch.set(db.collection("ecoInitiatives").document("initiative4"), initiative4);
        batch.set(db.collection("ecoInitiatives").document("initiative5"), initiative5);
        batch.set(db.collection("ecoInitiatives").document("initiative6"), initiative6);
        
        // Commit batch
        batch.commit()
            .addOnSuccessListener(aVoid -> {
                System.out.println("Sample eco initiatives added successfully!");
            })
            .addOnFailureListener(e -> {
                System.err.println("Error adding eco initiatives: " + e.getMessage());
            });
    }
    
    /**
     * Adds sample activities data
     */
    public void addSampleActivities() {
        WriteBatch batch = db.batch();
        
        // Activity 1
        Map<String, Object> activity1 = new HashMap<>();
        activity1.put("name", "Guided Nature Hike");
        activity1.put("description", "Explore local trails with our experienced naturalist guide.");
        activity1.put("price", 45.00);
        activity1.put("duration", "3 hours");
        activity1.put("difficulty", "Medium");
        activity1.put("imageUrl", "https://images.unsplash.com/photo-1551632811-561732d1e306?w=800");
        activity1.put("available", true);
        activity1.put("createdAt", new Date());
        
        // Activity 2
        Map<String, Object> activity2 = new HashMap<>();
        activity2.put("name", "Eco-Workshop");
        activity2.put("description", "Learn sustainable living practices and eco-friendly techniques.");
        activity2.put("price", 35.00);
        activity2.put("duration", "2 hours");
        activity2.put("difficulty", "Easy");
        activity2.put("imageUrl", "https://images.unsplash.com/photo-1582213782179-e0d53f98f2ca?w=800");
        activity2.put("available", true);
        activity2.put("createdAt", new Date());
        
        // Activity 3
        Map<String, Object> activity3 = new HashMap<>();
        activity3.put("name", "Bird Watching Tour");
        activity3.put("description", "Discover local bird species in their natural habitat.");
        activity3.put("price", 25.00);
        activity3.put("duration", "2.5 hours");
        activity3.put("difficulty", "Easy");
        activity3.put("imageUrl", "https://images.unsplash.com/photo-1441974231531-c6227db76b6e?w=800");
        activity3.put("available", true);
        activity3.put("createdAt", new Date());
        
        // Add activities to batch
        batch.set(db.collection("activities").document("activity1"), activity1);
        batch.set(db.collection("activities").document("activity2"), activity2);
        batch.set(db.collection("activities").document("activity3"), activity3);
        
        // Commit batch
        batch.commit()
            .addOnSuccessListener(aVoid -> {
                System.out.println("Sample activities added successfully!");
            })
            .addOnFailureListener(e -> {
                System.err.println("Error adding activities: " + e.getMessage());
            });
    }
    
    /**
     * Adds all sample data at once
     */
    public void addAllSampleData() {
        addSampleRooms();
        addSampleEcoInitiatives();
        addSampleActivities();
    }
}
