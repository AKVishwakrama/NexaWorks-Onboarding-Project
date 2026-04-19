# 🏢 NexaWorks — Complete Project Documentation

## Executive Summary

**NexaWorks** is an **AI-Powered Employee Onboarding & Workforce Intelligence Platform** designed to automate and enhance the employee onboarding experience while providing predictive analytics for HR professionals. It's built as a full-stack, multi-tier application with production-grade architecture suitable for enterprise deployment.

**Purpose**: Streamline employee onboarding, track engagement, predict attrition risk, and enable data-driven HR decisions through AI-powered insights and automation.

---

## 📊 Architecture Overview

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
│  • Welcome emails | • Document reminders                 │
│  • High-risk alerts | • Meeting notifications            │
│  • Engagement alerts                                     │
└──────────────────────────────────────────────────────────┘
```

---

## 🛠️ Tech Stack

### **Frontend**
| Technology | Version | Purpose |
|-----------|---------|---------|
| React | 18.3.1 | UI library - component-based architecture |
| Vite | 5.3.4 | Build tool - blazing fast dev/build |
| Tailwind CSS | 3.4.6 | Utility-first CSS framework for styling |
| React Router | 6.24.1 | Client-side routing (3 role-based dashboards) |
| Recharts | 2.12.7 | Interactive charts & data visualization |
| Axios | 1.7.2 | HTTP client for API calls |
| Framer Motion | 11.3.2 | Animation & transition library |
| React Hot Toast | 2.4.1 | Toast notifications |
| Lucide React | 0.400.0 | Icon library |
| date-fns | 3.6.0 | Date manipulation & formatting |
| React Dropzone | 14.2.3 | File upload component |

**CSS Preprocessing**: PostCSS (8.4.39), Autoprefixer (10.4.19)

### **Backend**
| Technology | Version | Purpose |
|-----------|---------|---------|
| Spring Boot | 3.2.3 | Java framework for REST APIs & enterprise features |
| Java | 17+ | Programming language |
| Maven | 3.8+ | Build & dependency management |
| Spring Security | Latest | Authentication & authorization |
| Spring Data JPA | Latest | ORM for database operations |
| JWT (JJWT) | 0.12.5 | Token-based authentication |
| H2 Database | Latest | In-memory dev database (zero install) |
| PostgreSQL | Driver only | Production database option |
| Spring Mail | Latest | Email functionality via Gmail SMTP |
| Validation | Latest | Input validation (Jakarta Bean Validation) |
| Lombok | Latest | Boilerplate reduction (@Data, @Builder, etc) |
| Apache Commons | 2.15.1 | File utilities |

**Build & Runtime**: Maven compiler, Java 17 JDK

### **AI Microservice**
| Technology | Version | Purpose |
|-----------|---------|---------|
| Flask | 3.0.3 | Lightweight Python web framework |
| Python | 3.9+ | Programming language |
| Flask-CORS | 4.0.1 | Cross-origin request handling |
| Requests | 2.32.3 | HTTP library for external API calls |
| python-dotenv | 1.0.1 | Environment variable management |
| Gunicorn | 22.0.0 | Production WSGI server |
| TextBlob | (implicit in ML ops) | NLP for sentiment analysis |
| Anthropic Claude API | (via requests) | Advanced chatbot & recommendations |

### **Database**
- **Development**: H2 (in-memory, auto-created, zero install needed)
- **Production**: PostgreSQL (recommended for persistence)

### **Authentication & Security**
- **Auth Method**: JWT (JSON Web Tokens)
- **Password Hashing**: BCrypt
- **Token Expiry**: 24 hours
- **CORS**: Configured for localhost:5173 and 3000

### **Email Service**
- **Provider**: Gmail SMTP (smtp.gmail.com:587)
- **Authentication**: App-specific password (OAuth2-style)
- **TLS**: Enabled

---

## 🔐 Role-Based Access Control (RBAC)

The platform supports **3 user roles**, each with distinct permissions and dashboards:

### **1. Employee (entry-level users)**
**Responsibilities & Permissions:**
- Complete onboarding tasks (document uploads, form submissions)
- Upload documents (PAN, Aadhaar, Voter ID, Passport, Salary Slip, Offer Letter)
- Update profile information
- Submit feedback & self-assessments
- View their onboarding progress (checklist)
- Track engagement score & onboarding completion percentage
- View scheduled meetings with managers
- Track task completion status
- Read notifications from HR/Managers

**Dashboard Features:**
- Progress ring (visual 0-100% completion)
- Document checklist (6 document types)
- Task tracker (pre-defined onboarding tasks)
- Meeting viewer (calendar/schedule)
- Notifications bell (unread count)

### **2. HR (Human Resources Professionals)**
**Responsibilities & Permissions:**
- Verify employee documents (approve/reject with notes)
- Monitor all employees (view 50+ user database)
- Send document reminders to employees
- Trigger high-risk alerts (manual)
- Schedule meetings with employees
- View collective analytics & dashboards
- Send bulk notifications
- View feedback from employees
- Access AI-powered analytics & risk predictions
- Export reports

**Dashboard Features:**
- Employee table with search/filter
- Risk distribution chart (low/medium/high)
- Analytics dashboard (multiple charts)
- High-risk employee list
- Feedback viewer
- Document verification panel
- Alert configuration

### **3. Manager (Team/Department Leads)**
**Responsibilities & Permissions:**
- View team members (assigned employees)
- Track team progress & onboarding status
- Monitor team engagement & risk scores
- Schedule 1-on-1 meetings
- Mark meetings as completed
- View team feedback & sentiment
- Access risk distribution for team
- Monitor specific employee progress

**Dashboard Features:**
- Team radar chart (engagement comparison)
- Risk distribution (team-specific)
- Team member cards with quick stats
- Meeting scheduler & tracker
- High-risk team members list
- Team feedback aggregation

---

## ✨ Core Features

### **A. User Management & Authentication**
- **Registration**: Self-registration with role selection (Employee/HR/Manager)
- **Login**: Email + password authentication with JWT token generation
- **JWT Token**: 24-hour expiry, includes user ID, role, email
- **Password**: Hashed with BCrypt (salted, secure)
- **Session**: Stateless JWT-based (no server-side session storage)
- **Pre-seeded Users**: 50 Indian employees with realistic data

### **B. Document Management**
**Supported Document Types:**
1. PAN Card (Permanent Account Number)
2. Aadhaar ID
3. Voter ID
4. Passport
5. Salary Slip
6. Offer Letter

**Features:**
- **Upload**: Employees upload documents (multipart/form-data)
- **Storage**: Files saved to `./uploads/{userId}/{docType}`
- **Verification**: HR can approve/reject with comment notes
- **Reminders**: Automated scheduled emails for missing documents (every 12h)
- **Status Tracking**: Stores completion status & verification state

### **C. Onboarding Progress Tracking**
- **Progress Metric**: 0-100% completion score
- **Components Tracked**:
  - Document completion
  - Task completion
  - Meeting attendance
  - Engagement score
- **Visual Indicators**: Progress rings, status badges, status color codes
- **Automated Calculation**: Backend computes progress from completed tasks

### **D. AI-Powered Features**

#### **1. Attrition Risk Prediction**
**Algorithm**: Weighted ML model combining multiple factors:
- **Engagement Score** (30% weight): How active user is in system
- **Task Completion** (25% weight): % of onboarding tasks completed
- **Login Frequency** (20% weight): 1-10 scale of recent login activity
- **Sentiment Analysis** (15% weight): From feedback text
- **Document Completeness** (10% weight): How many required docs uploaded

**Output**: Risk Score 0-100 with categories:
- **0-30**: Low risk (green) ✅
- **31-65**: Medium risk (yellow) ⚠️
- **66-100**: High risk (red) 🚨

**Trigger Alerts**: Email sent to HR when risk > 65

#### **2. Sentiment Analysis**
- **Input**: Employee feedback text
- **Method**: TextBlob NLP for polarity & subjectivity
- **Fallback**: Keyword-based sentiment (if TextBlob unavailable)
- **Output**: 
  - Polarity score (-1 to +1)
  - Subjectivity score (0 to 1)
  - Label: Positive/Negative/Neutral
  - Emoji representation 😊😐😞

#### **3. NexaBot AI Chatbot**
- **Powered by**: Anthropic Claude Sonnet (via API)
- **Fallback**: Keyword-based responses if API unavailable
- **Features**:
  - Contextual conversation history
  - Role-aware responses (Employee/HR/Manager)
  - User name personalization
  - Onboarding guidance
  - FAQ answers
- **Use Cases**: Help desk, onboarding questions, policy questions

#### **4. Bulk Analytics Dashboard**
- Charts showing risk distribution across all employees
- Sentiment trends
- Engagement metrics
- Document completion rates
- Department-wise analysis

### **E. Email Automation & Alerts**

**Automated Emails Triggered:**

| Email Type | Trigger | Frequency | Recipients |
|-----------|---------|-----------|-----------|
| Welcome Email | Employee registration | Once | New employee |
| Document Reminders | Missing documents | Every 12h | Employee |
| High-Risk Alerts | Risk score > 65 | Every 6h | HR team emails |
| Low Engagement Alerts | Engagement < 50 | Every 24h | HR team emails |
| Meeting Notifications | Meeting scheduled | On event | Participant |
| Document Verification | Doc verified/rejected | On event | Employee |

**Email Queue**: Handled by Spring Mail with async processing

**Alert Recipients** (configurable):
- sharma964aman@gmail.com
- vishwakarmaankita754@gmail.com
- jrashi813@gmail.com

### **F. Meeting Management**
- **Types**: 1-on-1s, group onboarding, reviews
- **Scheduling**: HR/Manager can schedule with employees
- **Status**: Pending, Completed, Cancelled
- **Notifications**: Email sent to participant
- **Tracking**: Completion status tracked by manager

### **G. Feedback & Survey System**
- **Employee Feedback**: Text + rating (1-5 stars) + category
- **Categories**: Onboarding, Culture, Management, Facilities, Other
- **Sentiment**: Auto-analyzed feedback text
- **Visibility**: Aggregated view for HR/Managers

### **H. Task Management (Onboarding Checklist)**
**Pre-defined Tasks** (customizable):
- [ ] Complete profile information
- [ ] Upload required documents
- [ ] Attend orientation session
- [ ] Sign policies & agreements
- [ ] Set up office equipment
- [ ] Meet with manager
- [ ] Complete initial training

**Tracking**: Task completion status updated by employee

### **I. Notification System**
- **Types**: Document reminders, risk alerts, meeting invites, feedback prompts
- **Delivery**: In-app notifications + email
- **Read Status**: Employees can mark as read
- **Severity**: Info, Warning, Alert

---

## 📁 Project Structure

```
onboarding-platform/
│
├── 📄 README.md                    ← Main documentation
├── 📄 start-all.sh                 ← Script to start all 3 services
│
├── 🔵 FRONTEND (React + Vite)
│   └── frontend/
│       ├── package.json
│       ├── vite.config.js
│       ├── tailwind.config.js
│       ├── postcss.config.js
│       ├── index.html
│       └── src/
│           ├── main.jsx            ← Entry point
│           ├── App.jsx             ← Main routing
│           ├── index.css           ← Global styles
│           │
│           ├── pages/
│           │   ├── LoginPage.jsx
│           │   ├── EmployeeDashboard.jsx
│           │   ├── HRDashboard.jsx
│           │   └── ManagerDashboard.jsx
│           │
│           ├── components/
│           │   ├── auth/           ← Login/auth components
│           │   ├── employee/       ← Employee UI components
│           │   ├── hr/             ← HR UI components
│           │   ├── manager/        ← Manager UI components
│           │   ├── shared/         ← Shared (Sidebar, etc)
│           │   │   ├── Sidebar.jsx ← Role-based navigation
│           │   │   └── ...
│           │   └── chatbot/
│           │       └── Chatbot.jsx ← NexaBot interface
│           │
│           ├── context/
│           │   ├── AuthContext.jsx ← User auth state (JWT)
│           │   └── ThemeContext.jsx ← Dark/light mode
│           │
│           ├── hooks/              ← Custom React hooks
│           ├── services/
│           │   └── api.js          ← Axios API client
│           ├── utils/              ← Helper functions
│           └── assets/             ← Images, logos, etc
│
├── 🟥 BACKEND (Spring Boot)
│   └── backend/
│       ├── pom.xml                 ← Maven config & dependencies
│       ├── target/                 ← Compiled classes & JARs
│       └── src/
│           ├── main/
│           │   ├── java/
│           │   │   └── com/nexaworks/
│           │   │       ├── NexaWorksApplication.java       ← Main Spring app
│           │   │       │
│           │   │       ├── config/
│           │   │       │   ├── SecurityConfig.java         ← JWT + CORS setup
│           │   │       │   └── ...
│           │   │       │
│           │   │       ├── controller/
│           │   │       │   ├── AuthController.java         ← Login/register
│           │   │       │   ├── EmployeeController.java     ← Employee endpoints
│           │   │       │   ├── HRController.java           ← HR endpoints
│           │   │       │   └── ManagerController.java      ← Manager endpoints
│           │   │       │
│           │   │       ├── entity/
│           │   │       │   ├── User.java                   ← Main user entity
│           │   │       │   ├── Notification.java
│           │   │       │   ├── Meeting.java
│           │   │       │   └── Feedback.java
│           │   │       │
│           │   │       ├── enums/
│           │   │       │   └── Role.java                   ← EMPLOYEE/HR/MANAGER
│           │   │       │
│           │   │       ├── repository/
│           │   │       │   ├── UserRepository.java         ← JPA CRUD
│           │   │       │   ├── FeedbackRepository.java
│           │   │       │   ├── MeetingRepository.java
│           │   │       │   ├── NotificationRepository.java
│           │   │       │   └── ...
│           │   │       │
│           │   │       ├── service/
│           │   │       │   ├── UserService.java            ← User business logic
│           │   │       │   ├── EmailService.java           ← Email sending (Gmail)
│           │   │       │   └── DataSeeder.java             ← Seed 50 users on startup
│           │   │       │
│           │   │       ├── security/
│           │   │       │   ├── JwtProvider.java            ← JWT generation/validation
│           │   │       │   ├── JwtFilter.java              ← JWT request filter
│           │   │       │   └── CustomUserDetailsService.java ← Load user for auth
│           │   │       │
│           │   │       ├── scheduler/
│           │   │       │   └── AlertScheduler.java         ← Cron jobs (6h, 12h, 24h)
│           │   │       │
│           │   │       ├── dto/
│           │   │       │   └── Dtos.java                   ← Data transfer objects
│           │   │       │
│           │   │       └── exception/
│           │   │           └── ...                         ← Custom exceptions
│           │   │
│           │   └── resources/
│           │       ├── application.properties              ← Config (DB, JWT, Email, etc)
│           │       └── data.sql                            ← DB seeding script (if any)
│           │
│           └── test/
│               └── java/                                   ← Unit/integration tests
│
├── 🐍 AI MICROSERVICE (Flask)
│   └── ai-service/
│       ├── app.py                  ← Flask main app
│       ├── requirements.txt         ← Python dependencies
│       │
│       ├── data/
│       │   └── employees.json       ← Employee data for AI context
│       │
│       ├── routes/                 ← API endpoint definitions (if modularized)
│       │   ├── sentiment.py
│       │   ├── risk.py
│       │   ├── chatbot.py
│       │   └── analytics.py
│       │
│       ├── models/                 ← ML models
│       │   └── risk_model.py        ← Attrition prediction logic
│       │
│       └── utils/                  ← Helper functions
│           └── nlp_utils.py         ← TextBlob utilities
```

---

## 🔌 API Endpoints

### **Authentication (Public)**
```
POST   /api/auth/login
├── Body: { email, password, role }
└── Response: { token, user: { id, name, role, email } }

POST   /api/auth/register
├── Body: { name, email, password, role, department }
└── Response: User object

GET    /api/auth/me
├── Auth: JWT token required
└── Response: Current user object
```

### **Employee API (requires ROLE_EMPLOYEE)**
```
GET    /api/employee/dashboard
└── Response: Dashboard metrics (progress, engagement, tasks, documents)

POST   /api/employee/document/upload
├── Body: multipart/form-data { docType: "PAN"|"Aadhaar"|etc, file }
└── Response: { status, message, documentId }

PUT    /api/employee/task/{taskKey}
├── Body: { completed: true/false }
└── Response: Updated task status

POST   /api/employee/feedback
├── Body: { content, rating: 1-5, category }
└── Response: Feedback object

GET    /api/employee/notifications
└── Response: Array of notifications

PUT    /api/employee/notifications/read-all
└── Response: { success }

GET    /api/employee/meetings
└── Response: Array of meetings
```

### **HR API (requires ROLE_HR)**
```
GET    /api/hr/dashboard
└── Response: HR dashboard with charts, stats

GET    /api/hr/employees
├── Query: ?search=name&department=eng&page=0&size=10
└── Response: Paginated employee list

GET    /api/hr/employees/{id}
└── Response: Detailed employee object

PUT    /api/hr/employees/{id}/document/{docType}/verify
├── Body: { approved: true/false, note: "reason" }
└── Response: Updated verification status

POST   /api/hr/employees/{id}/remind-documents
├── Body: { documentTypes: ["PAN", "Aadhaar"] }
└── Response: { success, emailsSent }

POST   /api/hr/employees/{id}/risk-alert
├── Body: { message }
└── Response: Alert sent confirmation

POST   /api/hr/employees/{id}/notify
├── Body: { title, message, type, severity }
└── Response: Notification created

POST   /api/hr/meetings
├── Body: { participantId, title, scheduledAt, description }
└── Response: Meeting object

GET    /api/hr/feedback
├── Query: ?employeeId=123&page=0
└── Response: Feedback list

GET    /api/hr/high-risk
└── Response: All employees with risk score > 65
```

### **Manager API (requires ROLE_MANAGER)**
```
GET    /api/manager/dashboard
└── Response: Team metrics

GET    /api/manager/team
└── Response: Assigned team members

GET    /api/manager/team/{id}
└── Response: Individual team member details

POST   /api/manager/meetings
├── Body: { participantId, title, scheduledAt }
└── Response: Meeting object

PUT    /api/manager/meetings/{id}/complete
└── Response: Updated meeting status

GET    /api/manager/high-risk
└── Response: High-risk team members

GET    /api/manager/team-feedback
└── Response: Team feedback aggregation
```

### **AI Service API (Flask, No Auth)**
```
GET    /health
└── Response: { status: "OK" }

POST   /api/sentiment/analyze
├── Body: { text, employee_id }
└── Response: { polarity, subjectivity, label, emoji }

GET    /api/sentiment/bulk
└── Response: Sentiment analysis for all 50 employees

POST   /api/risk/predict
├── Body: { employee: {...} }
└── Response: { risk_score, category, reasoning }

GET    /api/risk/bulk
└── Response: Risk predictions for all employees

GET    /api/analytics/dashboard
└── Response: Aggregate analytics & charts data

POST   /api/chatbot
├── Body: { message, history: [], role, user_name }
└── Response: { reply, confidence }

POST   /api/recommendations
├── Body: { employee: {...} }
└── Response: Array of recommendations
```

---

## 🔐 Security Features

### **Authentication & Authorization**
- **JWT (JSON Web Tokens)**: 
  - 24-hour expiry
  - Signed with secret key: `NexaWorks2024SuperSecretKey!ForJWTSigning@Production#256BitKeyLength`
  - Contains: `userId`, `email`, `role`
  
- **Password Security**:
  - Hashed with BCrypt (automatic salting)
  - Never stored in plaintext
  - Min complexity: mixed case, numbers recommended
  
- **Role-Based Access Control (RBAC)**:
  - Three roles: `ROLE_EMPLOYEE`, `ROLE_HR`, `ROLE_MANAGER`
  - Enforced at controller level via `@PreAuthorize` annotations
  - Route-level protection in frontend

### **API Security**
- **CORS Configuration**: 
  - Allowed origins: `localhost:5173`, `localhost:3000`, `127.0.0.1:5173`
  - Prevents unauthorized cross-origin requests
  
- **HTTP Security**:
  - No CSRF tokens needed (stateless JWT)
  - HTTP-only cookies not used (localStorage for tokens)
  - TLS/HTTPS recommended for production

### **Data Protection**
- **File Uploads**: Stored in `./uploads/{userId}/{docType}/` (server-side)
- **Database**: H2 (dev) requires no setup; PostgreSQL supports encryption in production
- **Email Credentials**: App-specific Gmail passwords (not main account password)

### **Audit & Logging**
- Document verification tracked with notes
- Email alerts logged
- Login attempts recorded (can be extended)
- Scheduler activity logged

---

## 📊 Pre-seeded Test Users (50 Total)

### **Login Passwords**
- HR Users: `HR@123456`
- Manager Users: `Mgr@123456`
- Employee Users: `Emp@123456`

### **HR Users (7 total)**
| Name | Email | Department |
|------|-------|-----------|
| Sunita Rao | sunita.rao@nexaworks.in | HR |
| Priya Nair | priya.nair@nexaworks.in | HR |
| Divya Menon | divya.menon@nexaworks.in | HR |
| Preeti Jain | preeti.jain@nexaworks.in | HR |
| Suresh Pillai | suresh.pillai@nexaworks.in | HR |
| Swati Goel | swati.goel@nexaworks.in | HR |
| Poornima Subramanian | poornima.s@nexaworks.in | HR |

### **Manager Users (5 total)**
| Name | Email | Department |
|------|-------|-----------|
| Vikram Mehta | vikram.mehta@nexaworks.in | Engineering |
| Rahul Kapoor | rahul.kapoor@nexaworks.in | Product |
| Pooja Gupta | pooja.gupta@nexaworks.in | Marketing |
| Ankit Joshi | ankit.joshi@nexaworks.in | Finance |
| Meghna Iyer | meghna.iyer@nexaworks.in | Operations |

### **Employee Users (38+ total)**
**Low Risk (Good Onboarding):**
- aarav.sharma@nexaworks.in (Engineering)
- kavya.krishnan@nexaworks.in (Product)
- pallavi.rao@nexaworks.in (Product)

**Medium Risk:**
- deepak.kumar@nexaworks.in (Sales)
- manish.chauhan@nexaworks.in (Engineering)
- rajesh.pandey@nexaworks.in (Finance)
- [+many more...]

**High Risk (Triggers Alerts):**
- kiran.patil@nexaworks.in (Engineering)
- arjun.singh@nexaworks.in (Engineering)
- nikhil.agarwal@nexaworks.in (Sales) ← **CRITICAL**
- dinesh.babu@nexaworks.in (Engineering)

---

##  Deployment & Running

### **1. Backend (Spring Boot)**
```bash
cd backend

# Build & run (auto-seeds database)
mvn spring-boot:run

# Starts on: http://localhost:8080
# H2 Console: http://localhost:8080/h2-console
#   JDBC URL: jdbc:h2:mem:nexaworksdb
#   Username: nexaadmin
#   Password: nexa@2024
```

### **2. AI Microservice (Flask)**
```bash
cd ai-service

# Install Python dependencies
pip install -r requirements.txt

# Download TextBlob data (one-time)
python -m textblob.download_corpora

# (Optional) Set Anthropic API key for Claude chatbot
export ANTHROPIC_API_KEY=your_key_here

# Run Flask server
python app.py

# Starts on: http://localhost:5001
```

### **3. Frontend (React)**
```bash
cd frontend

# Install Node dependencies
npm install

# Start dev server
npm run dev

# Starts on: http://localhost:5173
```

### **Production Build**
```bash
# Frontend
npm run build  # Creates dist/ folder

# Backend
mvn clean install  # Creates JAR in target/

# Deploy as Docker container or JAR
```

---

## 📋 Configuration Files

### **Backend** (`application.properties`)
```properties
# Server
server.port=8080

# Database (H2 for dev)
spring.datasource.url=jdbc:h2:mem:nexaworksdb
spring.datasource.username=nexaadmin
spring.datasource.password=nexa@2024

# JWT
app.jwt.secret=NexaWorks2024SuperSecretKey!...
app.jwt.expiration-ms=86400000

# Email (Gmail)
spring.mail.username=24io10am9@mitsgwl.ac.in
spring.mail.password=#Amit$951@

# AI Service URL
ai.service.base-url=http://localhost:5001

# File uploads
app.upload.dir=./uploads
```

### **Frontend** (`vite.config.js`)
```javascript
// API base URL configured in axios interceptor
export default {
  server: { port: 5173 },
  plugins: [react()],
}
```

### **AI Service** (Python environment)
```bash
ANTHROPIC_API_KEY=sk-...
FLASK_ENV=development
```

---

## 📈 Performance & Scalability

### **Current Constraints**
- **Max Users**: 50 (hardcoded in seeder)
- **Database**: H2 (in-memory, not persistent by default)
- **File Storage**: Local filesystem
- **Email**: Synchronous (blocks request)

### **Production Scalability**
- **Database**: Switch to PostgreSQL with connection pooling (HikariCP)
- **Storage**: Use S3/Azure Blob Storage instead of local files
- **Email**: Use RabbitMQ/Kafka for async job queue
- **Caching**: Add Redis for session/cache layer
- **Load Balancing**: Deploy multiple backend instances behind Nginx
- **AI Service**: Scale Flask with Gunicorn workers
- **Frontend**: Serve static build from CDN

---

## 🔧 Development Guidelines

### **Adding New Features**

**1. New Entity**
- Create `src/main/java/com/nexaworks/entity/YourEntity.java`
- Add `@Entity`, `@Id`, fields, getters/setters
- Create repository: `YourEntityRepository.java`

**2. New API Endpoint**
- Add method to controller: `@GetMapping`, `@PostMapping`, etc
- Add service logic: `YourService.java`
- Map DTOs in `Dtos.java`
- Add JWT authorization: `@PreAuthorize`

**3. New Frontend Page**
- Create in `src/pages/YourPage.jsx`
- Add route in `App.jsx`
- Create components in `src/components/`
- Call API using `axios` from `services/api.js`

**4. New AI Endpoint**
- Add route in `ai-service/app.py`
- Import/create model in `models/`
- Handle CORS & JSON responses
- Test with `http://localhost:5001/api/your-endpoint`

---

## 🧪 Testing

### **Manual Testing Workflows**

**Employee Onboarding Flow:**
1. Login as employee (aarav.sharma@...)
2. Upload 6 documents
3. Complete tasks
4. Submit feedback
5. View progress dashboard

**HR Analytics Flow:**
1. Login as HR (sunita.rao@...)
2. View employee analytics
3. Verify documents
4. Send risk alerts
5. Export reports

**AI Features:**
1. Open chatbot → ask onboarding question
2. AI service analyzes feedback sentiment
3. Risk predictions update in real-time
4. Email alerts sent for high-risk employees

---

## 📦 Dependencies Summary

| Category | Count | Key Libraries |
|----------|-------|---------------|
| Frontend | 12 | React, Vite, Tailwind, Recharts, Framer |
| Backend | 8 | Spring Boot, Security, JPA, JWT, Mail |
| AI Service | 5 | Flask, TextBlob, Anthropic, requests |

**Total Development Dependencies**: 40+

---

## 🎯 Key Business Metrics

- **User Base**: 50 pre-seeded Indian employees
- **Document Types**: 6 (PAN, Aadhaar, Voter, Passport, Salary, Offer)
- **Email Recipients**: 3 HR alerts
- **Task Types**: 7+ pre-defined tasks
- **Risk Calculation**: Weighted model (5 factors)
- **Onboarding Duration**: Avg 7-14 days
- **Alert Frequency**: Every 6h (risk), 12h (docs), 24h (engagement)

---

## 🔗 Quick Links

- **Frontend**: http://localhost:5173
- **Backend**: http://localhost:8080
- **H2 Console**: http://localhost:8080/h2-console
- **AI Service**: http://localhost:5001
- **GitHub Docs**: See `README.md` in root

---

## 📝 Notes for Explanation

**When explaining to others:**

1. **Start with**: "It's a 3-tier application: React frontend, Spring Boot backend, Flask AI service"
2. **Highlight**: "Automates employee onboarding with AI-powered risk prediction"
3. **Tech**: "Uses JWT for stateless auth, H2/PostgreSQL for DB, Gmail for emails"
4. **Features**: "Document tracking, sentiment analysis, attrition prediction, chatbot"
5. **Data**: "50 pre-seeded Indian employees with realistic profiles"
6. **Security**: "BCrypt passwords, JWT tokens (24h), role-based access"
7. **Scalability**: "H2 for dev, PostgreSQL for prod; can add Redis, S3, RabbitMQ"
8. **Purpose**: "Streamline onboarding, reduce manual work, predict employee attrition"

---

**Document Version**: 1.0 | **Last Updated**: April 2026 | **Status**: Production-Ready
