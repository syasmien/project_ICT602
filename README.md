# MediNear: Emergency Pharmacy Finder
## ICT602 Mobile Technology and Development — Group Project

---

## 📱 App Overview
MediNear is an Android application that helps users locate nearby pharmacies
during emergency or urgent situations using GPS and Google Maps.

---

## 🚀 Setup Instructions

### Step 1: Firebase Setup
1. Go to https://console.firebase.google.com
2. Create a new project → "MediNear"
3. Add Android app → package: `com.example.medinear`
4. Download `google-services.json` → place in `app/` folder
5. Enable:
   - Authentication → Email/Password
   - Firestore Database → Test mode
   - Storage → Test mode
   - Cloud Messaging (FCM)

### Step 2: Google Maps API Key
1. Go to https://console.cloud.google.com
2. Enable: Maps SDK for Android + Directions API
3. Create API Key
4. Replace `YOUR_GOOGLE_MAPS_API_KEY` in `AndroidManifest.xml`

### Step 3: Add Sample Pharmacy Data to Firestore
Collection: `pharmacies`
```
Document 1:
  name: "Guardian Pharmacy Shah Alam"
  address: "AEON Mall Shah Alam, Seksyen 13"
  lat: 3.0738
  lng: 101.5183
  phone: "03-55108888"
  operating_hours: "10:00 AM - 10:00 PM"
  is_24hours: false

Document 2:
  name: "Watsons Pharmacy"
  address: "Plaza Masalam, Shah Alam"
  lat: 3.0800
  lng: 101.5340
  phone: "03-55123456"
  operating_hours: "10:00 AM - 10:00 PM"
  is_24hours: false

Document 3:
  name: "Farmasi Al-Ikhlas"
  address: "Seksyen 7, Shah Alam"
  lat: 3.0851
  lng: 101.5321
  phone: "03-55139999"
  operating_hours: "Open 24 Hours"
  is_24hours: true
```

### Step 4: ic_notification drawable
Create a simple notification icon in res/drawable/ named ic_notification.xml
or use any existing icon from the drawable resources.

---

## 📁 Project Structure
```
app/src/main/java/com/example/medinear/
├── activities/
│   ├── SplashActivity.java           ← Entry point
│   ├── LoginActivity.java            ← Firebase Auth login
│   ├── RegisterActivity.java         ← Signup + Firestore
│   ├── HomeActivity.java             ← Dashboard
│   ├── NearbyPharmacyActivity.java   ← Feature 1 & 2: GPS + Maps + Navigation
│   ├── CameraActivity.java           ← Feature 3: Camera medicine capture
│   ├── MedicineReminderActivity.java ← Feature 4: Push Notifications
│   ├── FavouritesActivity.java       ← Saved pharmacies (CRUD)
│   └── ProfileActivity.java          ← View/edit profile
├── adapters/
│   ├── ReminderAdapter.java
│   └── FavouriteAdapter.java
├── models/
│   ├── Pharmacy.java
│   ├── Reminder.java
│   └── Favourite.java
└── utils/
    ├── NotificationHelper.java
    └── MyFirebaseMessagingService.java
```

---

## 📋 4 Core Mobile Features
| # | Feature | Activity | API/Library |
|---|---------|----------|------------|
| 1 | GPS Location Detection | NearbyPharmacyActivity | FusedLocationProvider |
| 2 | Google Maps & Navigation | NearbyPharmacyActivity | Maps SDK + Directions |
| 3 | Camera (Medicine Capture) | CameraActivity | CameraX |
| 4 | Push Notifications | NotificationHelper + FCM | FCM + NotificationManager |

---

## 🔥 Firebase Firestore Structure
```
users/{userId}
  → name, email, phone, createdAt

pharmacies/{pharmacyId}
  → name, address, lat, lng, phone, operating_hours, is_24hours

favourites/{favouriteId}
  → userId, pharmacyId, pharmacyName, address, phone, lat, lng, addedAt

medicine_records/{recordId}
  → userId, medicineName, notes, photoUrl, capturedAt

reminders/{reminderId}
  → userId, medicineName, dosage, time, isActive, createdAt
```

---

## 📋 CRUD Operations
| Operation | Where |
|-----------|-------|
| CREATE | Add favourite pharmacy, Set medicine reminder, Save medicine photo |
| READ | View nearby pharmacies on map, View favourites, View reminders |
| UPDATE | Edit user profile, Toggle reminder active/inactive |
| DELETE | Remove favourite, Delete reminder |

---

## ⚠️ Important Notes
- You still need to create XML layout files for each activity
- Replace YOUR_GOOGLE_MAPS_API_KEY with your actual key
- Add google-services.json from Firebase Console
- Add ic_notification.xml drawable resource

---

## 👥 Group Members
- Member 1: [Name] — [Student ID]
- Member 2: [Name] — [Student ID]
- Member 3: [Name] — [Student ID]
- Member 4: [Name] — [Student ID]

GitHub: [Your GitHub URL]
