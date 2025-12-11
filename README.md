💰 PiggyFlow – Budget Tracker App (Android)
<p align="center"> <img src="https://github.com/user-attachments/assets/android_ss_1" width="24%" /> <img src="https://github.com/user-attachments/assets/android_ss_2" width="24%" /> <img src="https://github.com/user-attachments/assets/android_ss_3" width="24%" /> <img src="https://github.com/user-attachments/assets/android_ss_4" width="24%" /> </p> <p align="center"> <img src="https://github.com/user-attachments/assets/android_ss_5" width="24%" /> <img src="https://github.com/user-attachments/assets/android_ss_6" width="24%" /> <img src="https://github.com/user-attachments/assets/android_ss_7" width="24%" /> <img src="https://github.com/user-attachments/assets/android_ss_8" width="24%" /> </p>

A beautifully designed Android Budget Tracker App built using Kotlin, Jetpack Compose, Ktor, and Room, helping users manage expenses, track budgets, and visualize financial trends effortlessly.

🚀 Overview

PiggyFlow provides a clean and intuitive interface for tracking daily expenses and income.
The app includes charts, category insights, PDF export, cloud backup/restore, and a modern Compose UI to deliver a polished experience.

✨ Features
🏠 Onboarding

Smooth onboarding experience introducing the app.

Bottom sheet to enter username and personalize the dashboard.

📊 Dashboard (Home)

Credit-card style summary card showing:

Total Budget

Total Spent

Remaining Balance

Search bar and filters to quickly find transactions.

Complete list of all income and expenses.

Each transaction opens a detailed view with:

Amount

Category

Description

Edit

Delete

Floating Action Button to open a bottom sheet for:

➕ Adding Income

➖ Adding Expense

📈 Stats & Insights

Interactive line chart showing monthly spending trends.

“Top Spending Categories” section ranked from high to low.

Each category is clickable, opening a detail screen showing:

All transactions under that category

Budget vs Expense analysis

One-tap option to export the category report as PDF.

⚙️ Settings & Account

User profile card showing username.

Google Sign-In support for cloud sync.

Options available:

Backup Data

Restore Backup

Delete Backup

Sign Out

About card leading to app information screen.

☁️ Cloud Backup

Fully integrated Google Sign-In for secure data backup and restore.

Allows users to store, retrieve, and delete cloud backups anytime.

🧠 Tech Stack
Component	Technology
Language	Kotlin
UI	Jetpack Compose
Architecture	MVVM + Coroutines + StateFlow
Database	Room / SharedPreferences
Networking	Ktor Client
Navigation	Jetpack Navigation
Charts	Custom Compose Canvas / Libraries
Cloud Sync	Google Sign-In + Cloud Storage
Background Work	WorkManager
PDF Export	Android PDFDocument
🎉 Additional Highlights

Elegant and smooth UI animations.

Light & Dark mode support.

Clean architecture with scalable codebase.

Highly responsive design across screen sizes.
