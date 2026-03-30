# 💰 PiggyFlow - Budget Tracker App

<p align="center">
  <img src="https://github.com/user-attachments/assets/527b6292-8647-4111-9f37-d436620ae479" width="24%" />
  <img src="https://github.com/user-attachments/assets/f18d88a7-e014-47d6-ba73-fdf136c562a2" width="24%" />
  <img src="https://github.com/user-attachments/assets/7a03397a-70f2-4432-8951-7f210b0e7d39" width="24%" />
  <img src="https://github.com/user-attachments/assets/f7ed616e-97c9-494e-aeeb-98992b1f81f7" width="24%" />
</p>

<p align="center">
  <img src="https://github.com/user-attachments/assets/2a34c07c-14e6-4a00-aceb-733ee83cc0f8" width="24%" />
  <img src="https://github.com/user-attachments/assets/ccd953c7-a9ca-479e-9010-b5df2af36882" width="24%" />
  <img src="https://github.com/user-attachments/assets/b2f8c8be-46d5-4912-8347-eb446e47a0b1" width="24%" />
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
