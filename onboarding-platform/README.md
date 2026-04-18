# 🏢 NexaWorks — AI-Powered Employee Onboarding & Workforce Intelligence Platform

> **Project Expo**: Built for academic showcase with production-grade architecture

---

## 📋 Table of Contents
- [Architecture](#architecture)
- [Tech Stack](#tech-stack)
- [Setup Instructions](#setup-instructions)
- [Login Credentials](#login-credentials)
- [Features](#features)
- [Email Setup](#email-setup)
- [API Reference](#api-reference)

---

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────────┐
│                    FRONTEND (React + Vite)               │
│         Port: 5173 | Tailwind CSS | Recharts            │
│   Employee Dashboard | HR Dashboard | Manager Dashboard  │
└──────────────┬──────────────────────┬───────────────────┘
               │ REST API (JWT)       │ REST API
               ▼                     ▼
┌──────────────────────┐   ┌──────────────────────────────┐
│  BACKEND (Spring Boot)│   │  AI MICROSERVICE (Flask)     │
│  Port: 8080          │   │  Port: 5001                  │
│  JWT Auth + RBAC     │   │  • Attrition Risk Prediction │
│  H2 / PostgreSQL     │   │  • Sentiment Analysis        │
│  Email Service       │   │  • Claude Chatbot            │
│  50 Indian Users     │   │  • Bulk Analytics            │
└──────────────────────┘   └──────────────────────────────┘
               │
               ▼
┌──────────────────────────────────────────────────────────┐
│              Gmail SMTP Email Service                     │
│  sharma964aman@gmail.com                                 │
│  vishwakarmaankita754@gmail.com                          │
│  jrashi813@gmail.com                                     │
└──────────────────────────────────────────────────────────┘
```

---

## 🛠️ Tech Stack

| Layer       | Technology                          |
|-------------|-------------------------------------|
| Frontend    | React 18, Vite, Tailwind CSS, Recharts, Framer Motion |
| Backend     | Spring Boot 3.2, Spring Security, JWT, JPA |
| Database    | H2 (dev) / PostgreSQL (prod)        |
| AI Service  | Flask, TextBlob, Anthropic Claude API |
| Email       | Gmail SMTP via Spring Mail          |
| Auth        | JWT + BCrypt (RBAC: Employee/HR/Manager) |

---

## 🚀 Setup Instructions

### Prerequisites
- Java 17+
- Maven 3.8+
- Node.js 18+
- Python 3.9+

---

### 1️⃣ Backend (Spring Boot)

```bash
cd backend

# Run (auto-seeds 50 Indian employees into H2 database)
mvn spring-boot:run

# ✅ Backend starts on: http://localhost:8080
# 🗄️ H2 Console:       http://localhost:8080/h2-console
#    JDBC URL: jdbc:h2:mem:nexaworksdb
#    Username: nexaadmin | Password: nexa@2024
```

**⚠️ Email Setup** (see section below before running)

---

### 2️⃣ AI Microservice (Flask)

```bash
cd ai-service

# Install dependencies
pip install -r requirements.txt

# Download TextBlob data
python -m textblob.download_corpora

# Set Anthropic key (optional - chatbot works without it)
export ANTHROPIC_API_KEY=your_key_here

# Run
python app.py

# ✅ AI Service starts on: http://localhost:5001
```

---

### 3️⃣ Frontend (React)

```bash
cd frontend

# Install dependencies
npm install

# Run
npm run dev

# ✅ Frontend starts on: http://localhost:5173
```

**Start all 3 in separate terminal windows.**

---

## 🔐 Login Credentials

### HR Users (password: `HR@123456`)
| Name | Email |
|------|-------|
| Sunita Rao | sunita.rao@nexaworks.in |
| Priya Nair | priya.nair@nexaworks.in |
| Divya Menon | divya.menon@nexaworks.in |
| Preeti Jain | preeti.jain@nexaworks.in |
| Suresh Pillai | suresh.pillai@nexaworks.in |
| Swati Goel | swati.goel@nexaworks.in |
| Poornima Subramanian | poornima.s@nexaworks.in |

### Manager Users (password: `Mgr@123456`)
| Name | Email |
|------|-------|
| Vikram Mehta (Engineering) | vikram.mehta@nexaworks.in |
| Rahul Kapoor (Product) | rahul.kapoor@nexaworks.in |
| Pooja Gupta (Marketing) | pooja.gupta@nexaworks.in |
| Ankit Joshi (Finance) | ankit.joshi@nexaworks.in |
| Meghna Iyer (Operations) | meghna.iyer@nexaworks.in |

### Employee Users (password: `Emp@123456`)
**Good onboarding (low risk):**
- aarav.sharma@nexaworks.in (Engineering)
- kavya.krishnan@nexaworks.in (Product)
- pallavi.rao@nexaworks.in (Product)

**Medium risk:**
- deepak.kumar@nexaworks.in (Sales)
- manish.chauhan@nexaworks.in (Engineering)
- rajesh.pandey@nexaworks.in (Finance)

**High risk (triggers email alerts):**
- kiran.patil@nexaworks.in (Engineering)
- arjun.singh@nexaworks.in (Engineering)
- nikhil.agarwal@nexaworks.in (Sales) ← CRITICAL risk
- dinesh.babu@nexaworks.in (Engineering)

---

## 📧 Email Setup (CRITICAL)

The system sends real emails. To enable:

1. Go to your Gmail account → **Security** → **2-Step Verification** (enable it)
2. Then go to **App Passwords** → Generate a new app password for "Mail"
3. Copy the 16-character app password
4. Open `backend/src/main/resources/application.properties`
5. Replace `YOUR_GMAIL_APP_PASSWORD_HERE` with your app password

```properties
spring.mail.username=sharma964aman@gmail.com
spring.mail.password=xxxx xxxx xxxx xxxx   ← your app password here
```

**Emails sent by the system:**
- 🎉 Welcome email on employee registration
- 📄 Document reminder emails (missing PAN, Aadhaar etc.)
- 🚨 High attrition risk alerts → sharma964aman@gmail.com, vishwakarmaankita754@gmail.com, jrashi813@gmail.com
- 📅 Meeting notification emails
- 📉 Low engagement alerts

**If email is not configured**, the platform still works fully — emails just won't be delivered (logged as warnings).

---

## ✨ Key Features

### 🔒 RBAC (Role-Based Access Control)
- **Employee**: Upload documents, complete tasks, view progress, give feedback, see meetings
- **HR**: Verify documents, view all employees, send alerts, schedule meetings, AI analytics
- **Manager**: View team, track progress, schedule 1:1s, risk monitor

### 🤖 AI Features
- **Attrition Risk Score (0-100)**: Weighted model using engagement, tasks, login frequency, sentiment, document completeness
- **Sentiment Analysis**: TextBlob analyzes employee feedback text
- **NexaBot Chatbot**: Powered by Claude Sonnet (with keyword fallback)
- **Bulk Analytics**: Dashboard with charts for HR

### 📧 Email Automation
- Startup scan: Alerts sent for all high-risk employees
- Scheduled every 6h: New risk alerts
- Scheduled every 12h: Document reminders
- Scheduled every 24h: Low engagement alerts

### 📊 Dashboards
- Employee: Progress ring, document checklist, task tracker, meeting viewer
- HR: Analytics charts, employee table with filters, doc verification, risk table
- Manager: Team radar chart, risk distribution, team member cards

---

## 🔌 API Reference

### Auth (public)
```
POST /api/auth/login     { email, password, role }
POST /api/auth/register  { name, email, password, role, department }
GET  /api/auth/me        → current user (requires JWT)
```

### Employee (requires JWT, ROLE_EMPLOYEE)
```
GET  /api/employee/dashboard
POST /api/employee/document/upload   (multipart: docType, file)
PUT  /api/employee/task/{taskKey}    { completed: true/false }
POST /api/employee/feedback          { content, rating, category }
GET  /api/employee/notifications
PUT  /api/employee/notifications/read-all
GET  /api/employee/meetings
```

### HR (requires JWT, ROLE_HR)
```
GET  /api/hr/dashboard
GET  /api/hr/employees
GET  /api/hr/employees/{id}
PUT  /api/hr/employees/{id}/document/{docType}/verify  { approved, note }
POST /api/hr/employees/{id}/remind-documents
POST /api/hr/employees/{id}/risk-alert
POST /api/hr/employees/{id}/notify    { title, message, type, severity }
POST /api/hr/meetings                 { participantId, title, scheduledAt, ... }
GET  /api/hr/feedback
GET  /api/hr/high-risk
```

### Manager (requires JWT, ROLE_MANAGER)
```
GET  /api/manager/dashboard
GET  /api/manager/team
GET  /api/manager/team/{id}
POST /api/manager/meetings
PUT  /api/manager/meetings/{id}/complete
GET  /api/manager/high-risk
GET  /api/manager/team-feedback
```

### AI Service (Flask, no auth)
```
GET  /health
POST /api/sentiment/analyze   { text, employee_id }
GET  /api/sentiment/bulk
POST /api/risk/predict        { employee: {...} }
GET  /api/risk/bulk
GET  /api/analytics/dashboard
POST /api/chatbot             { message, history, role, user_name }
POST /api/recommendations     { employee: {...} }
```

---

## 📁 Project Structure

```
nexaworks/
├── backend/                    ← Spring Boot
│   ├── pom.xml
│   └── src/main/java/com/nexaworks/
│       ├── NexaWorksApplication.java
│       ├── config/SecurityConfig.java
│       ├── controller/
│       │   ├── AuthController.java
│       │   ├── EmployeeController.java
│       │   ├── HRController.java
│       │   └── ManagerController.java
│       ├── entity/            User, Notification, Meeting, Feedback
│       ├── repository/        JPA repositories
│       ├── security/          JWT utils, filter, UserDetailsService
│       ├── service/           UserService, EmailService, DataSeeder
│       └── scheduler/         AlertScheduler (6h risk, 12h docs, 24h engagement)
│
├── ai-service/                 ← Flask AI
│   ├── app.py                  Main Flask app
│   ├── requirements.txt
│   └── data/employees.json     50 Indian employees
│
└── frontend/                   ← React + Vite
    ├── src/
    │   ├── pages/
    │   │   ├── LoginPage.jsx       Role-based login
    │   │   ├── EmployeeDashboard.jsx
    │   │   ├── HRDashboard.jsx
    │   │   └── ManagerDashboard.jsx
    │   ├── components/
    │   │   ├── shared/Sidebar.jsx
    │   │   └── chatbot/Chatbot.jsx
    │   ├── context/            AuthContext, ThemeContext
    │   └── services/api.js     Axios service layer
    └── ...
```

---

## 🎓 Academic Highlights

This project demonstrates:
1. **Full-stack development** with 3 separate services
2. **JWT-based stateless authentication** with role-based access control
3. **AI/ML integration** — risk prediction model + NLP sentiment analysis
4. **Real-time email notifications** via SMTP with HTML templates
5. **Database seeding** with 50 realistic Indian employees
6. **Scheduled background jobs** for automated monitoring
7. **Responsive UI** with dark/light mode, charts, modals
8. **RESTful API design** with proper HTTP status codes

---

*Built with ❤️ for NexaWorks — Workforce Intelligence Platform*
