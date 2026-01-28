# EcoStay Retreat - Mobile Application

A complete Android application for EcoStay Retreat, an eco-friendly resort offering sustainable accommodations and outdoor adventure experiences.

## Features

### 🔐 User Authentication
- Email/Password registration and login
- Google Sign-In integration
- Password reset functionality
- User profile management

### 🏠 Room Booking
- Browse eco-friendly rooms (cabins, eco-pods, treehouses)
- Filter and sort by price, availability, and room type
- Date selection with availability checking
- Real-time price calculation
- Secure payment processing

### 🎯 Activity Booking
- Guided hikes, eco-tours, sustainability workshops
- Bird watching and nature photography sessions
- Yoga sessions in natural settings
- Calendar-based availability
- Participant limit management

### 💳 Payment Integration
- Stripe payment gateway integration
- Secure card processing
- Booking confirmation system
- Payment history tracking

### 📱 User Experience
- Beautiful, intuitive UI with #7AC58A brand color
- Material Design 3 components
- Smooth navigation between sections
- Real-time data synchronization
- Offline capability with sample data

### 🌱 Eco Initiatives
- Information about resort's green practices
- Solar power and renewable energy details
- Water conservation programs
- Waste management policies
- Local community support

## Technology Stack

- **Platform**: Android (Java)
- **Backend**: Firebase (Authentication, Firestore, Storage)
- **Payment**: Stripe Android SDK
- **UI**: Material Design 3, Custom components
- **Image Loading**: Glide
- **Navigation**: ViewPager2, Custom bottom navigation

## Setup Instructions

### 1. Firebase Configuration
1. The `google-services.json` file is already configured for your project
2. Enable Authentication in Firebase Console:
   - Go to Authentication > Sign-in method
   - Enable Email/Password and Google sign-in
3. Enable Firestore Database:
   - Go to Firestore Database > Create database
   - Start in test mode
4. Enable Firebase Storage for images

### 2. Stripe Configuration
1. Get your Stripe publishable key from Stripe Dashboard
2. Replace `"pk_test_YOUR_PUBLISHABLE_KEY_HERE"` in `PaymentActivity.java` with your actual key
3. For production, implement a backend server to handle payment intents

### 3. Build and Run
1. Open the project in Android Studio
2. Sync the project with Gradle files
3. Build and run on device or emulator

## Project Structure

```
app/
├── src/main/
│   ├── java/com/example/ecostayapp/
│   │   ├── activities/          # All activity classes
│   │   ├── fragments/           # Home, Rooms, Activities, Bookings
│   │   ├── adapters/            # RecyclerView adapters
│   │   ├── models/              # Data models (Room, Activity, Booking)
│   │   └── utils/               # Utility classes
│   ├── res/
│   │   ├── layout/              # XML layouts
│   │   ├── drawable/            # Icons and graphics
│   │   ├── values/              # Colors, strings, themes
│   │   └── ...
│   └── google-services.json     # Firebase configuration
```

## Key Components

### Activities
- `SplashActivity` - App launch screen
- `OnboardingActivity` - Welcome screens
- `SignInActivity` - User authentication
- `RegisterActivity` - User registration
- `MainActivity` - Main app with bottom navigation
- `RoomBookingActivity` - Room reservation
- `ActivityBookingActivity` - Activity reservation
- `PaymentActivity` - Payment processing
- `ProfileActivity` - User profile management

### Fragments
- `HomeFragment` - Featured rooms and eco initiatives
- `RoomsFragment` - All available rooms
- `ActivitiesFragment` - Available activities
- `BookingsFragment` - User's booking history

### Models
- `Room` - Room information and availability
- `Activity` - Activity details and scheduling
- `Booking` - Reservation data
- `EcoInitiative` - Sustainability information

## Database Structure

### Firestore Collections

#### Users Collection
```json
{
  "fullName": "John Doe",
  "email": "john@example.com",
  "phoneNumber": "+1234567890",
  "dateOfBirth": "01/01/1990",
  "gender": "Male",
  "createdAt": 1640995200000
}
```

#### Rooms Collection
```json
{
  "name": "Mountain View Cabin",
  "description": "Luxurious cabin with panoramic mountain views",
  "pricePerNight": 299.0,
  "imageUrl": "https://example.com/cabin.jpg",
  "maxGuests": 4,
  "featured": true,
  "type": "Cabin",
  "amenities": "WiFi, Kitchen, Balcony",
  "rating": 4.5,
  "reviewCount": 23,
  "available": true
}
```

#### Activities Collection
```json
{
  "name": "Guided Nature Hike",
  "description": "Explore beautiful mountain trails",
  "price": 45.0,
  "imageUrl": "https://example.com/hike.jpg",
  "duration": "3 hours",
  "maxParticipants": 12,
  "difficulty": "Moderate",
  "category": "Outdoor",
  "instructor": "John Guide",
  "rating": 4.8,
  "reviewCount": 15
}
```

#### Bookings Collection
```json
{
  "userId": "user123",
  "roomId": "room456",
  "type": "room",
  "checkInDate": "2024-01-15T00:00:00Z",
  "checkOutDate": "2024-01-17T00:00:00Z",
  "guests": 2,
  "totalPrice": 598.0,
  "status": "confirmed",
  "bookingDate": "2024-01-10T00:00:00Z",
  "paymentStatus": "paid",
  "paymentId": "pi_1234567890"
}
```

## Customization

### Colors
The app uses the EcoStay brand color `#7AC58A` throughout. To change colors:
1. Update `colors.xml` in `res/values/`
2. Modify the primary color references

### Content
To add new rooms or activities:
1. Add data to Firestore collections
2. Update the sample data in fragment classes as fallback

### Payment
For production deployment:
1. Implement a secure backend server
2. Use Stripe's Payment Intents API
3. Handle webhook events for payment confirmations

## Testing

The app includes sample data that loads when Firestore is unavailable, making it easy to test offline. All major features can be tested without requiring actual payment processing.

## Support

For technical support or customization requests, please refer to the Firebase and Stripe documentation for backend integration details.

---

**EcoStay Retreat** - Experience sustainable luxury in the heart of nature 🌿







