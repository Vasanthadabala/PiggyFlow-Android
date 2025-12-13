# 💰 PiggyFlow - Budget Tracker App

<p align="center">
  <img src="https://github.com/user-attachments/assets/dea4410a-365f-4937-a9bd-0af47d95e2b8" width="24%" />
  <img src="https://github.com/user-attachments/assets/f4f40106-7d69-4db5-b47d-33ac6b73f420" width="24%" />
  <img src="https://github.com/user-attachments/assets/d44aca49-08fb-4b65-b9c0-80f884bcc288" width="24%" />
  <img src="https://github.com/user-attachments/assets/5ef4b298-8801-4351-bce7-211e41cfb703" width="24%" />
</p>

<p align="center">
  <img src="https://github.com/user-attachments/assets/cff15670-40fa-4afc-9c48-f51118a7132f" width="24%" />
  <img src="https://github.com/user-attachments/assets/000d2c3b-e2e6-468b-81b4-30d3a614161a" width="24%" />
  <img src="https://github.com/user-attachments/assets/3eaa1791-9ec5-4a93-bcd5-1304b36dc001" width="24%" />
  <img src="https://github.com/user-attachments/assets/e6f89443-3101-479f-9d37-602e5fd8cee6" width="24%" />
</p>


A beautifully designed **Android Budget Tracker App** built with **SwiftUI** and **SwiftData**, helping users easily track their income, expenses, and spending trends — all synced securely via **iCloud**.  

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
