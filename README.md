<div align="center">

# 🎓 StudentHire
### AI-Powered Job Matching & Career Development Platform

**Connecting Students with Opportunities — Powered by Firebase & Artificial Intelligence**

![Platform](https://img.shields.io/badge/Platform-Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Language](https://img.shields.io/badge/Language-Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)
![Backend](https://img.shields.io/badge/Backend-Firebase-FFCA28?style=for-the-badge&logo=firebase&logoColor=black)
![AI](https://img.shields.io/badge/Feature-AI_Powered-1D9E75?style=for-the-badge&logo=openai&logoColor=white)
![Status](https://img.shields.io/badge/Status-Completed-success?style=for-the-badge)

</div>

---

## 📝 Project Overview

**StudentHire** is an advanced, native Android application engineered to bridge the employment gap between student job seekers and potential recruiters within the academic ecosystem. Built explicitly for the **University of Layyah**, the application serves a dual purpose:

- 🎓 Provides students with **flexible part-time jobs**, **freelance opportunities**, and **internships** tailored around their academic schedules.
- 💼 Empowers **local businesses, corporate employers, and industry recruiters** to post vacancies, manage high-volume application traffic, and discover top-tier young talent effortlessly.

The core framework leverages **Firebase Cloud Services** for secure real-time authentication and data synchronization, blended with **Artificial Intelligence (AI)** modules for automated resume engineering and intelligent applicant screening.

---

## 📱 Application Architecture & Visual Flow

### 🔐 Core Gateway & Onboarding

| 01. Premium Splash Screen | 02. Targeted Role Selection | 03. Centralized Secure Login |
| :---: | :---: | :---: |
| <img src="screenshots/01_splash_screen.png" width="220" alt="Splash Screen"/> | <img src="screenshots/02_role_selection.png" width="220" alt="Role Selection"/> | <img src="screenshots/03_login_screen.png" width="220" alt="Login Gateway"/> |

### 👨‍🎓 Student Experience & AI Modules

| 04. Job Feed Dashboard | 05. AI CV Analysis Engine | 06. AI Resume Builder |
| :---: | :---: | :---: |
| <img src="screenshots/04_student_dashboard.png" width="220" alt="Student Feed"/> | <img src="screenshots/05_cv_analyzer.png" width="220" alt="AI Analyzer Panel"/> | <img src="screenshots/06_resume_builder.png" width="220" alt="Resume Builder Drawer"/> |

### 💼 Recruiter Dashboard & Automated Screening

| 07. Job Management Board | 08. Vacancy Creation Dashboard | 09. AI Top 10 Shortlisting |
| :---: | :---: | :---: |
| <img src="screenshots/07_recruiter_dashboard.png" width="220" alt="Recruiter Board"/> | <img src="screenshots/08_post_job.png" width="220" alt="Post Job Form"/> | <img src="screenshots/09_ai_shortlist.png" width="220" alt="AI Automation Panel"/> |

> *Note: All interface screenshots are organized and sourced directly from the root `/screenshots` directory.*

---

## 🚀 Key Functional Modules

### 1. 🔑 Account Registries & Security Gateway

| Role | Fields Captured |
|------|----------------|
| **Student** | Full Name, Email, Contact Number, Password, University Name, Degree Program |
| **Recruiter** | Full Name, Email, Contact Number, Password, Company Name, Designation, Industry Sector |

- **Role-Based Authentication** — Unified login page with specialized toggle buttons (Student / Recruiter), cross-referenced securely against the Firebase Authentication backend.

---

### 2. 👨‍🎓 Student Subsystem (Job Seeker)

| Feature | Description |
|---------|-------------|
| 🔍 **Real-Time Query Feed** | Integrated search bar enabling students to query the active job market instantly by title or tags |
| 📋 **Granular Job Specifications** | Detailed breakdowns: job tasks, criteria, salary details, and employer contact info |
| 🤖 **AI CV Analyzer** | Upload CV before applying — AI scans for errors and gives optimization suggestions or approves submission |
| 📄 **Interactive Resume Builder** | Activated via horizontal slide gesture; auto-generates a professional resume from student input |

---

### 3. 💼 Recruiter Subsystem (Talent Acquisition)

| Feature | Description |
|---------|-------------|
| 🏢 **Corporate Dashboard** | Complete index of all active vacancies with rapid search filter |
| 📢 **Seamless Job Publishing** | Instant deployment of new vacancies: Job Title, Salary, Location, Requirements |
| 📊 **High-Traffic Tracking** | Handles 500–1000+ applicants; manual "View CV" and "Shortlist" actions available |
| 🤖 **AI Top 10 Shortlist** | AI analyzes all submitted CVs against the job description and instantly renders the top 10 best-matched candidates |

---

## 🛠️ Technical Stack & Infrastructure

```
┌─────────────────────────────────────────────────────┐
│                   STUDENTHIRE STACK                 │
├──────────────────┬──────────────────────────────────┤
│ Frontend UI      │ Native Android XML Layouts        │
│ Language         │ Kotlin (Kotlin DSL)               │
│ Backend          │ Firebase Authentication           │
│ Database         │ Firebase Realtime Database        │
│ Build System     │ Gradle 9.1.0 + Version Catalogs  │
│ AI Integration   │ CV Analyzer + Resume Builder      │
└──────────────────┴──────────────────────────────────┘
```

---

## 📥 Deployment & Installation Guide

To install and evaluate the application on any physical Android device:

1. Navigate into the `apk/` directory located within this project's root folder.
2. Download the **`StudentHire.apk`** installation binary.
3. On your Android device, enable **"Install from Unknown Sources"** in Settings.
4. Run the installer, create a test account, and explore the platform!

📦 **[Download StudentHire.apk](apk/StudentHire.apk)**

---

## 📜 Repository Documentation Resources

| Document | Link |
|----------|------|
| 📄 Official App Privacy Policy | [View PDF](docs/privacy_policy.pdf) |
| 📘 Complete App User Manual | [View PDF](docs/user_manual.pdf) |

---

## 👥 Development Team & Credits

| Field | Detail |
|-------|--------|
| 👨‍💻 **Lead Developer** | Muneeb Hamza |
| 🎓 **Academic Program** | Bachelor of Science in Information Technology (BS IT) |
| 🏛️ **Department** | Dept. of Computer Science & Information Technology |
| 🏫 **Institution** | University of Layyah |
| 👩‍🏫 **Project Evaluator** | Mam Nabiha Komal |

---

<div align="center">

**© 2026 StudentHire — University of Layyah | Dept. of CS & IT**

*Developed with ❤️ by Muneeb Hamza as part of BS IT Semester Project*

</div>
