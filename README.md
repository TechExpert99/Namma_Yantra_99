# 🚜 Namma Yantra Share

**"Uber for Tractors" — Empowering Small Farmers in Karnataka**

An Android application that bridges the gap between small farmers who cannot afford agricultural machinery and equipment owners whose machines sit idle. Built with Kotlin, Jetpack Compose, Firebase, and Google Gemini AI.

[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)
[![Android](https://img.shields.io/badge/Platform-Android-green.svg)](https://www.android.com/)
[![Kotlin](https://img.shields.io/badge/Language-Kotlin-purple.svg)](https://kotlinlang.org/)
[![Firebase](https://img.shields.io/badge/Backend-Firebase-orange.svg)](https://firebase.google.com/)

---

## 📱 Screenshots

| Login Screen | Browse Equipment | Machine Detail | AI Chatbot |
|---|---|---|---|
| ![Login](screenshots/login.png) | ![Browse](screenshots/browse.png) | ![Detail](screenshots/detail.png) | ![Chat](screenshots/chat.png) |

---

## 🌟 Features

### For Renters (Small Farmers)
- 🔐 **OTP Login** — Phone authentication with role selection
- 🔍 **Browse Equipment** — Filter tractors, harvesters, and sprayers near you
- 💰 **Smart Price Calculator** — Hourly and daily rate estimation
- 📤 **Send Rental Requests** — Book equipment with date selection
- 📋 **My Bookings** — Track request status (Pending/Accepted/Declined)
- ⭐ **Rate & Review** — Share your rental experience
- 🤖 **AI Farming Assistant** — Ask Gemini AI about equipment and farming

### For Owners (Equipment Owners)
- ➕ **Add Equipment** — List tractors, harvesters, sprayers with rates
- 📥 **Manage Requests** — Accept or decline rental requests
- 💰 **Earnings Dashboard** — Track income from rentals
- 🔔 **Push Notifications** — Get alerted on new requests
- ⭐ **View Ratings** — See renter reviews

### Core Technology Features
- 🔥 **Firebase Firestore** — Real-time database
- 🔔 **Firebase Cloud Messaging** — Push notifications
- 🤖 **Google Gemini AI** — Intelligent chatbot for farming queries
- 📍 **Location-based Search** — Find equipment within 20km radius
- 🌐 **Multilingual Support** — Kannada, Hindi, English (ready)
- 🎨 **Material Design 3** — Modern, accessible UI

---

## 🏗️ Architecture

### Tech Stack
- **Language:** Kotlin
- **UI Framework:** Jetpack Compose
- **Architecture:** MVVM (Model-View-ViewModel)
- **Backend:** Firebase (Auth, Firestore, Storage, Cloud Messaging)
- **AI:** Google Gemini API
- **Dependency Injection:** Hilt (planned)
- **Async:** Kotlin Coroutines + Flow

### Project Structure
```
com.nayak.nammayantara/
├── data/
│   ├── model/           # Data classes (User, Equipment, Booking, Review)
│   └── repository/      # Firebase data operations
├── ui/
│   ├── screens/         # Composable screens
│   ├── viewmodel/       # Business logic
│   └── theme/           # App theming
├── service/             # FCM notification service
└── utils/               # Constants and helpers
```

---

## 🚀 Getting Started

### Prerequisites
- Android Studio Hedgehog 2024+ or newer
- Android SDK API 24+ (Android 7.0+)
- Google account for Firebase
- Gemini API key from [Google AI Studio](https://aistudio.google.com)

### Installation

1. **Clone the repository**
```bash
git clone [https://github.com/TechExpert99/Namma_Yantra_99]
cd namma-yantra-share
```

2. **Set up Firebase**
   - Go to [Firebase Console](https://console.firebase.google.com)
   - Create a new project
   - Add an Android app with package name: `com.nayak.nammayantara`
   - Download `google-services.json` and place in `app/` folder
   - Enable these services:
     - Authentication → Phone
     - Firestore Database
     - Cloud Messaging
     - (Optional) Cloud Storage

3. **Add your Gemini API Key**
   - Open `ui/viewmodel/ChatViewModel.kt`
   - Replace `YOUR_GEMINI_API_KEY_HERE` with your actual key:
   ```kotlin
   private val apiKey = "AIzaSyXXXXXXXXXXXXXXX"
   ```

4. **Sync and Build**
   - Open project in Android Studio
   - Click "Sync Project with Gradle Files"
   - Build → Make Project
   - Run on emulator or physical device

---

## 🔥 Firebase Setup

### Firestore Collections Structure

**users**
```json
{
  "uid": "string",
  "phone": "+919999999999",
  "name": "string",
  "role": "owner | renter",
  "location": "string",
  "rating": 4.5
}
```

**equipment**
```json
{
  "id": "string",
  "ownerId": "string",
  "name": "Mahindra 575 DI",
  "type": "Tractor | Harvester | Sprayer",
  "hourlyRate": 350.0,
  "dailyRate": 2500.0,
  "latitude": 12.9716,
  "longitude": 77.5946,
  "status": "Available | Booked | In-Use",
  "conditionRating": 4.5,
  "lastServiceDate": "2026-03-15",
  "fuelType": "Diesel"
}
```

**bookings**
```json
{
  "id": "string",
  "equipmentId": "string",
  "renterId": "string",
  "ownerId": "string",
  "startDate": "2026-04-29",
  "totalHours": 5,
  "totalDays": 0,
  "totalPrice": 1750.0,
  "status": "Pending | Accepted | Declined"
}
```

**reviews**
```json
{
  "id": "string",
  "bookingId": "string",
  "reviewerId": "string",
  "equipmentId": "string",
  "rating": 5.0,
  "comment": "Excellent service!",
  "date": "2026-04-29"
}
```

### Security Rules (Production)

Replace test mode rules with these before deployment:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /users/{userId} {
      allow read: if true;
      allow write: if request.auth.uid == userId;
    }
    
    match /equipment/{equipmentId} {
      allow read: if true;
      allow create: if request.auth != null;
      allow update, delete: if request.auth.uid == resource.data.ownerId;
    }
    
    match /bookings/{bookingId} {
      allow read: if request.auth.uid == resource.data.renterId 
                  || request.auth.uid == resource.data.ownerId;
      allow create: if request.auth != null;
      allow update: if request.auth.uid == resource.data.ownerId;
    }
    
    match /reviews/{reviewId} {
      allow read: if true;
      allow create: if request.auth != null;
    }
  }
}
```

---

## 🤖 AI Features

### Gemini Chatbot Capabilities
The AI assistant can answer questions like:
- "Which tractor is best for 3 acres of paddy farming?"
- "What is the daily rate for a harvester?"
- "How do I book a machine?"
- "What fuel do tractors use?"
- Regional language support (Kannada, Hindi)

### Sample Interactions
```
User: "Best machine for sugarcane harvesting?"
AI: "For sugarcane, you need a harvester like the Kubota Harvester 
     available at ₹600/hr or ₹4500/day. It's specifically designed 
     for efficient sugarcane cutting and processing."
```

---

## 📊 Problem & Impact

### Problem Statement
- Small farmers (<2 hectares) cannot afford tractors (₹5-10 lakh)
- Large farmers' machines sit idle 15-20 days/month
- Middlemen inflate rental costs by 30-50%
- No transparent rental marketplace

### Impact Goals
- 🚜 **Mechanization** — Bring machine power to smallest farms
- 💰 **Income** — ₹8,000-15,000/month for equipment owners
- ⚡ **Efficiency** — Increase machine utilization from 30% to 70%+
- 💸 **Cost Reduction** — Lower rental cost by 30-50% (remove middlemen)

---

## 🗺️ Roadmap

### ✅ Phase 1 — MVP (Completed)
- [x] OTP Login
- [x] Equipment Browse & Detail
- [x] Booking Requests
- [x] Push Notifications
- [x] AI Chatbot
- [x] Reviews & Ratings

### 🚧 Phase 2 — Coming Soon
- [ ] Payment Gateway (UPI/Razorpay)
- [ ] Google Maps Integration
- [ ] Digital Rental Agreement (PDF)
- [ ] Owner Earnings Analytics
- [ ] In-app Messaging

### 🔮 Phase 3 — Future
- [ ] Multi-language Voice Input
- [ ] Crop Calendar Integration
- [ ] Insurance Module
- [ ] Community Forum
- [ ] iOS Version

---

## 🧪 Testing

### Running Tests
```bash
# Unit tests
./gradlew test

# UI tests
./gradlew connectedAndroidTest
```

### Test Phone Number (Firebase Test Mode)
- Phone: `+91 9999999999`
- OTP: `123456`

---

## 📦 Dependencies

```kotlin
// Firebase
implementation(platform("com.google.firebase:firebase-bom:33.1.0"))
implementation("com.google.firebase:firebase-auth-ktx")
implementation("com.google.firebase:firebase-firestore-ktx")
implementation("com.google.firebase:firebase-messaging-ktx")

// Gemini AI
implementation("com.google.ai.client.generativeai:generativeai:0.9.0")

// Kotlin Coroutines
implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.3")

// Jetpack Compose
implementation(platform("androidx.compose:compose-bom:2024.04.00"))
implementation("androidx.compose.material3:material3")
implementation("androidx.lifecycle:lifecycle-viewmodel-compose")
```

Full dependency list: [`app/build.gradle.kts`](app/build.gradle.kts)

---

## 🤝 Contributing

Contributions are welcome! Please follow these steps:

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

### Code Style
- Follow [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html)
- Use meaningful variable names
- Add comments for complex logic
- Write tests for new features

---

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

## 👨‍💻 Author

**Nithin Nayak V N**
- GitHub: [@TechExpert99]((https://github.com/TechExpert99/Namma_Yantra_99))
- Email: your.email@example.com

---

## 🙏 Acknowledgments

- Firebase for backend infrastructure
- Google Gemini AI for intelligent chatbot
- Jetpack Compose for modern Android UI
- Material Design for design system
- Karnataka farmers for inspiration and feedback

---

## 📞 Support

For issues, questions, or feedback:
- Open an issue on [GitHub Issues](https://github.com/TechExpert99/Namma_Yantra_99))
- Email: nayakronayak@gmail.com
- Join our [Telegram Community](https://t.me/nammayantara) (coming soon)

---

## 🌐 Links

- [Download APK](https://github.com/yourusername/namma-yantra-share/releases)
- [Product Requirements Document](docs/PRD.md)
- [API Documentation](docs/API.md)
- [User Guide](docs/USER_GUIDE.md)

---

**Made with ❤️ for Karnataka farmers** 🌾🚜
