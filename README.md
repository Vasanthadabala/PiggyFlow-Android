# 💰 PiggyFlow - Budget Tracker App

<p align="center">
  <img src="https://github.com/user-attachments/assets/18d67f2b-2c36-4c12-a1cd-110ec39afd28" width="24%" />
  <img src="https://github.com/user-attachments/assets/297d35e7-423e-40a6-b74e-deadb43e617d" width="24%" />
  <img src="https://github.com/user-attachments/assets/2c88e31b-07a1-4830-a510-cda8b9ae2a1d" width="24%" />

</p>

<p align="center">
  <img src="https://github.com/user-attachments/assets/7e4f45fb-1529-439e-b99e-2ee114beb281" width="24%" />
  <img src="https://github.com/user-attachments/assets/08505f6d-2623-4c5f-bd9e-684dab1f9451" width="24%" />
  <img src="https://github.com/user-attachments/assets/2be29571-f7fc-4821-b279-1d9040ad755d" width="24%" />
</p>


A beautifully designed **Android Budget Tracker App** built with **Kotlin** and **JetPack Compose**, helping users easily track their income, expenses, and spending trends — all synced securely via **G-Drive**.  

---

## 🚀 Overview

The Budget Tracker App allows users to manage their financial activities smoothly with a clean, modern design.
It supports charts, category insights, detailed expense tracking, and secure backup/restore through Google Drive.

---

## ✨ Features

### 🏠 Onboarding
- Simple onboarding screen with app intro and “Get Started” button.  
- Bottom sheet for entering username and continuing to the home screen.

### 📊 Dashboard
- Credit-card style summary card showing:
      -Total Budget
      -Total Spent
      -Remaining Balance
- Search bar and filters to quickly find transactions.
- Complete list of all income and expenses.
- Each transaction opens a detailed view with:
      - Amount
      - Category
      - Description
      - Edit
      - Delete
- Floating Action Button to open a bottom sheet for:
- Adding Income And Expense
- Detailed expense view with editing and delete capability.

### 🧾 Stats
- Interactive line chart showing monthly spending trends.  
- “Top Spending Categories” section ranked from high to low.
- Each category is clickable, opening a detail screen showing:
    - All transactions under that category
    - Budget vs Expense analysis
- One-tap option to export the category report as PDF

### Settings & Account
- User profile card showing username.
- Google Sign-In support for cloud sync.
- Options available:
    - Backup Data
    - Restore Backup
    - Delete Backup
    - Sign Out
- About card leading to app information screen.


### ☁️ Cloud Sync & Authentication
- Fully integrated Google Sign-In for secure data backup and restore.
- Allows users to store, retrieve, and delete cloud backups anytime.

### 🔔 Notifications Panel
- Stay informed with a built-in notifications section for alerts and updates.

---

## 🧠 Tech Stack

| Component | Technology |
|------------|-------------|
| Language | Kotlin |
| Framework | Jetpack Compose |
| Database | RoomDatabase |
| Cloud Sync | Google Drive |
| Authentication | Sign in with Google |
| Charts | Canva Charts |
| Navigation | Jetpack Navigation |
| Network | Ktor Client |
| PDF Export | Android PDFDocument |

---

## 🎉 Additional Highlights
- Light & Dark mode support
- Elegant and smooth UI animations.
- Clean architecture with scalable codebase.
