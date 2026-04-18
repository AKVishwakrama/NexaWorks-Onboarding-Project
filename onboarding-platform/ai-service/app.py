"""
NexaWorks AI Microservice — Flask
Features:
  • Attrition Risk Prediction (weighted ML model)
  • Sentiment Analysis (TextBlob)
  • AI Chatbot (Anthropic Claude)
  • Bulk analytics for 50 employees
  • Email alert trigger proxy
"""

import os, json, math, logging
from datetime import datetime
from flask import Flask, request, jsonify
from flask_cors import CORS

app = Flask(__name__)
CORS(app, resources={r"/*": {"origins": "*"}})
logging.basicConfig(level=logging.INFO, format="%(asctime)s %(levelname)s %(message)s")
log = logging.getLogger(__name__)

BASE = os.path.dirname(os.path.abspath(__file__))
DATA_FILE = os.path.join(BASE, "data", "employees.json")

# ─── Load employee data ───────────────────────────────────────────────────────
def load_employees():
    data_file = os.path.join(BASE, "data", "employees.json")

    with open(data_file) as f:
        return json.load(f)

# ─── TextBlob sentiment (graceful fallback) ───────────────────────────────────
# ─── Simple sentiment (keyword-based) ───────────────────────────────────
def analyze_sentiment(text: str) -> dict:
    text = str(text or "").lower()
    if not text.strip():
        return {"polarity": 0.0, "subjectivity": 0.5, "normalized": 0.5,
                "label": "Neutral", "emoji": "😐"}

    # keyword fallback
    neg = ["terrible","horrible","awful","useless","lost","confused",
           "frustrated","disappointed","nobody","idle","disorganized",
           "poor","never","bad","wrong","angry","unorganized"]
    pos = ["excellent","great","wonderful","amazing","superb","fantastic",
           "helpful","smooth","positive","outstanding","loved","perfect"]
    words = text.split()
    neg_count = sum(1 for w in words if any(n in w for n in neg))
    pos_count = sum(1 for w in words if any(p in w for p in pos))
    total = max(1, neg_count + pos_count)
    pol  = (pos_count - neg_count) / total
    sub  = 0.6

    norm = round((pol + 1) / 2, 3)
    if   pol >  0.4: label, emoji = "Positive",          "😊"
    elif pol >  0.1: label, emoji = "Slightly Positive",  "🙂"
    elif pol > -0.1: label, emoji = "Neutral",            "😐"
    elif pol > -0.4: label, emoji = "Slightly Negative",  "😕"
    else:            label, emoji = "Negative",           "😟"

    return {"polarity": round(pol, 3), "subjectivity": round(sub, 3),
            "normalized": norm, "label": label, "emoji": emoji}

# ─── Risk prediction model ────────────────────────────────────────────────────
def compute_risk(emp: dict) -> int:
    score = 0
    eng  = emp.get("engagement_score", 70)
    task = emp.get("task_completion",  50)
    login= emp.get("login_frequency",  5)
    sent = emp.get("sentiment_score",  0.5)
    done = emp.get("onboarding_complete", True)

    # Engagement (0-35)
    if eng < 35: score += 35
    elif eng < 50: score += 25
    elif eng < 65: score += 15
    elif eng < 75: score += 7

    # Task completion (0-25)
    if task < 35: score += 25
    elif task < 50: score += 18
    elif task < 65: score += 10
    elif task < 75: score += 4

    # Login frequency (0-20)
    if login <= 2: score += 20
    elif login <= 4: score += 12
    elif login <= 6: score += 5

    # Sentiment (0-15)
    if sent < 0.25: score += 15
    elif sent < 0.40: score += 8
    elif sent < 0.55: score += 3

    # Onboarding incomplete (+5)
    if not done: score += 5

    # Missing docs
    docs = emp.get("documents", {})
    missing = sum(1 for v in docs.values() if not v)
    score += min(missing * 2, 10)

    # Experience penalty (very junior)
    if emp.get("experience_years", 3) <= 1:
        score += 5

    return min(score, 100)

def risk_level(score):
    if score >= 75: return "Critical"
    if score >= 50: return "High"
    if score >= 30: return "Medium"
    return "Low"

def risk_recommendations(emp, score):
    recs = []
    if score >= 75:
        recs += ["🚨 Immediate 1:1 meeting required",
                 "Assign dedicated buddy/mentor urgently",
                 "HR escalation required within 24h"]
    elif score >= 50:
        recs += ["📅 Schedule check-in meeting this week",
                 "Review blockers in onboarding checklist"]
    elif score >= 30:
        recs += ["📊 Send engagement pulse survey",
                 "Encourage team participation & activities"]
    recs.append("📧 Send weekly check-in message")
    if emp.get("sentiment_score", 0.5) < 0.4:
        recs.append("💬 Address feedback concerns in 1:1")
    return recs

# ══════════════════════════════════════════════════════════════════════════════
#  ROUTES
# ══════════════════════════════════════════════════════════════════════════════

@app.route("/")
def root():
    return jsonify({
        "status": "ok",
        "service": "NexaWorks AI",
        "version": "2.0",
        "message": "NexaWorks AI service is running",
        "health": "/health",
        "api_example": "/api/sentiment/analyze",
        "timestamp": datetime.now().isoformat()
    })

@app.route("/health")
def health():
    return jsonify({"status": "ok", "service": "NexaWorks AI", "version": "2.0",
                    "timestamp": datetime.now().isoformat()})

# ── Sentiment analysis ────────────────────────────────────────────────────────
@app.route("/api/sentiment/analyze", methods=["POST"])
def sentiment():
    data = request.get_json() or {}
    text = data.get("text", "")
    if not text:
        return jsonify({"error": "text is required"}), 400
    result = analyze_sentiment(text)
    return jsonify({**result, "text_snippet": text[:80] + ("..." if len(text)>80 else ""),
                    "employee_id": data.get("employee_id"),
                    "timestamp": datetime.now().isoformat()})

@app.route("/api/sentiment/bulk", methods=["GET"])
def sentiment_bulk():
    employees = load_employees()
    results = []
    for emp in employees:
        fb = emp.get("feedback","") or emp.get("last_feedback","") or ""
        if not fb: continue
        s = analyze_sentiment(fb)
        results.append({
            "id": emp.get("id"), "name": emp.get("name"),
            "department": emp.get("department"),
            "feedback_snippet": fb[:60]+"...",
            **s
        })
    return jsonify(results)

# ── Risk prediction ────────────────────────────────────────────────────────────
@app.route("/api/risk/predict", methods=["POST"])
def risk_predict():
    data = request.get_json() or {}
    emp_id = data.get("employee_id")
    # Accept employee dict directly or lookup by id
    emp = data.get("employee") or {}

    if emp_id and not emp:
        employees = load_employees()
        emp = next((e for e in employees if e.get("id") == emp_id), None)
        if not emp:
            return jsonify({"error": "Employee not found"}), 404

    if not emp:
        return jsonify({"error": "employee_id or employee object required"}), 400

    score = compute_risk(emp)
    level = risk_level(score)
    recs  = risk_recommendations(emp, score)
    sent  = analyze_sentiment(emp.get("feedback","") or emp.get("last_feedback","") or "")

    return jsonify({
        "employee_id":      emp.get("id"),
        "employee_name":    emp.get("name"),
        "risk_score":       score,
        "risk_level":       level,
        "needs_alert":      score >= 50,
        "sentiment":        sent,
        "recommendations":  recs,
        "computed_at":      datetime.now().isoformat()
    })

@app.route("/api/risk/bulk", methods=["GET"])
def risk_bulk():
    employees = load_employees()
    results = []
    for emp in employees:
        score = compute_risk(emp)
        results.append({
            "id": emp.get("id"), "name": emp.get("name"),
            "department": emp.get("department"),
            "risk_score": score, "risk_level": risk_level(score),
            "needs_alert": score >= 50,
            "engagement_score": emp.get("engagement_score"),
            "task_completion": emp.get("task_completion"),
        })
    results.sort(key=lambda x: x["risk_score"], reverse=True)
    return jsonify(results)

# ── Dashboard analytics ────────────────────────────────────────────────────────
@app.route("/api/analytics/dashboard", methods=["GET"])
def analytics():
    employees = load_employees()
    total = len(employees)
    completed = sum(1 for e in employees if e.get("onboarding_complete"))

    risk_dist = {"Critical": 0, "High": 0, "Medium": 0, "Low": 0}
    dept_stats = {}
    monthly = {}
    sentiments = {"Positive": 0, "Slightly Positive": 0, "Neutral": 0,
                  "Slightly Negative": 0, "Negative": 0}
    avg_engagement = 0
    avg_task = 0

    for emp in employees:
        score = compute_risk(emp)
        risk_dist[risk_level(score)] += 1

        dept = emp.get("department", "Other")
        if dept not in dept_stats:
            dept_stats[dept] = {"total": 0, "completed": 0, "avg_engagement": 0, "high_risk": 0}
        dept_stats[dept]["total"] += 1
        if emp.get("onboarding_complete"): dept_stats[dept]["completed"] += 1
        dept_stats[dept]["avg_engagement"] += emp.get("engagement_score", 0)
        if score >= 50: dept_stats[dept]["high_risk"] += 1

        # Monthly trend
        jd = emp.get("joining_date", "")
        if jd:
            try:
                d = datetime.strptime(jd[:10], "%Y-%m-%d")
                key = d.strftime("%b %Y")
                monthly[key] = monthly.get(key, 0) + 1
            except: pass

        # Sentiment from stored score
        s_score = emp.get("sentiment_score", 0.5)
        pol = (s_score - 0.5) * 2   # back to -1..1
        sent_label = analyze_sentiment(emp.get("feedback","") or "")["label"]
        sentiments[sent_label] = sentiments.get(sent_label, 0) + 1

        avg_engagement += emp.get("engagement_score", 0)
        avg_task += emp.get("task_completion", 0)

    # Finalize dept averages
    for d in dept_stats:
        t = dept_stats[d]["total"]
        dept_stats[d]["avg_engagement"] = round(dept_stats[d]["avg_engagement"] / t, 1)
        dept_stats[d]["completion_rate"] = round(dept_stats[d]["completed"] / t * 100, 1)

    return jsonify({
        "summary": {
            "total_employees":      total,
            "onboarding_completed": completed,
            "completion_rate":      round(completed / total * 100, 1) if total else 0,
            "high_risk_count":      risk_dist["High"] + risk_dist["Critical"],
            "avg_engagement":       round(avg_engagement / total, 1) if total else 0,
            "avg_task_completion":  round(avg_task / total, 1) if total else 0,
        },
        "risk_distribution":    risk_dist,
        "sentiment_distribution": sentiments,
        "department_stats":     dept_stats,
        "monthly_joins":        dict(sorted(monthly.items())),
    })

# ── Chatbot ───────────────────────────────────────────────────────────────────
@app.route("/api/chatbot", methods=["POST"])
def chatbot():
    data     = request.get_json() or {}
    message  = data.get("message", "").strip()
    history  = data.get("history", [])
    role     = data.get("role", "employee")
    username = data.get("user_name", "there")

    if not message:
        return jsonify({"error": "message is required"}), 400

    # Gather stats for context
    employees = load_employees()
    high_risk  = sum(1 for e in employees if compute_risk(e) >= 50)
    total      = len(employees)
    completed  = sum(1 for e in employees if e.get("onboarding_complete"))

    system_prompt = f"""You are NexaBot, the friendly AI assistant for NexaWorks Employee Onboarding Platform.
You are currently helping {username}, who is a {role}.

Platform context:
- Total employees in system: {total}
- Employees who completed onboarding: {completed}
- Employees at high attrition risk: {high_risk}
- Platform name: NexaWorks

Role-specific guidance:
{"Employee: Help them with their onboarding tasks, document uploads (PAN, Aadhaar, Voter ID, Passport, Salary Slip, Offer Letter, 10th/12th cert, Degree), training schedule, progress tracking, personalized onboarding paths, career progression insights, and gamification achievements." if role=="employee" else ""}
{"HR: Help with employee verification, risk scores, sending alerts, scheduling meetings, bulk analytics, personalized onboarding recommendations, career progression tracking, and engagement gamification." if role=="hr" else ""}
{"Manager: Help with team onboarding progress, scheduling 1:1s, viewing risk scores for your team, personalized development plans, career progression predictions, and team engagement metrics." if role=="manager" else ""}

Document types accepted: PAN Card, Aadhaar Card, Voter ID, Passport, Salary Slip (last 3 months), Offer Letter, 10th Certificate, 12th Certificate, Degree Certificate, Experience Letter, Relieving Letter, Photo.

Be concise, warm, and professional. Use bullet points for lists. Use emojis sparingly.
Always respond in under 150 words unless the question is complex."""

    messages = []
    for h in history[-8:]:
        messages.append({"role": h.get("role","user"), "content": h.get("content","")})
    messages.append({"role": "user", "content": message})

    # Try Anthropic API
    api_key = os.environ.get("ANTHROPIC_API_KEY", "")
    if api_key:
        try:
            import anthropic
            client = anthropic.Anthropic(api_key=api_key)
            response = client.messages.create(
                model="claude-sonnet-4-20250514",
                max_tokens=400,
                system=system_prompt,
                messages=messages
            )
            reply = response.content[0].text
            return jsonify({"reply": reply, "source": "claude",
                            "timestamp": datetime.now().isoformat()})
        except Exception as e:
            log.warning(f"Anthropic API error: {e}")

    # Keyword fallback
    ml = message.lower()
    if any(w in ml for w in ["document","upload","pan","aadhaar","voter","passport","salary","certificate","degree"]):
        reply = ("📄 **Required Documents for Onboarding:**\n"
                 "• PAN Card *(mandatory)*\n"
                 "• Aadhaar Card *(mandatory)*\n"
                 "• Voter ID / Passport\n"
                 "• Last 3 Salary Slips\n"
                 "• Offer Letter\n"
                 "• 10th & 12th Certificates\n"
                 "• Degree Certificate\n"
                 "• Experience & Relieving Letters\n\n"
                 "Upload them from **My Documents** in your dashboard.")
    elif any(w in ml for w in ["risk","attrition","leaving","quit","resign"]):
        reply = (f"🚨 **Attrition Risk Summary:**\n"
                 f"• {high_risk} employees are flagged as high risk\n"
                 f"• Risk is calculated from engagement, task completion, login frequency, and sentiment\n"
                 f"• Scores above 50 trigger automatic HR email alerts\n"
                 f"• View the Risk Analysis section for details.")
    elif any(w in ml for w in ["meeting","schedule","1:1","one on one"]):
        reply = ("📅 **Scheduling a Meeting:**\n"
                 "• HR and Managers can schedule meetings from their dashboards\n"
                 "• Go to Meetings → Schedule New Meeting\n"
                 "• Employee receives email + in-app notification automatically\n"
                 "• Meeting link is included in the email.")
    elif any(w in ml for w in ["progress","complete","onboard","checklist","task"]):
        reply = ("**Onboarding Progress:**\n"
                 "• Documents uploaded = 40% of total progress\n"
                 "• Tasks completed = 60% of total progress\n"
                 "• 90%+ progress marks onboarding as complete\n"
                 "• Check your checklist on the Employee Dashboard.")
    elif any(w in ml for w in ["training","course","learn","program"]):
        reply = ("🎓 **Training Schedule:**\n"
                 "• Week 1: Company orientation & HR policies\n"
                 "• Week 2: Role-specific tools & systems training\n"
                 "• Week 3: Department processes & compliance\n"
                 "• Week 4: First project kickoff with mentor\n"
                 "• All training tracked in your dashboard.")
    elif any(w in ml for w in ["path","personalized","onboarding plan"]):
        reply = ("🎯 **Personalized Onboarding Path:**\n"
                 "• AI creates customized 4-week plans based on your experience and role\n"
                 "• Junior employees get foundational training focus\n"
                 "• Senior employees get leadership and strategic focus\n"
                 "• Department-specific customizations included\n"
                 "• View your personalized path in the dashboard!")
    elif any(w in ml for w in ["career","progression","promotion","growth"]):
        reply = ("🚀 **Career Progression Insights:**\n"
                 "• AI predicts your next promotion timeline\n"
                 "• Analyzes engagement, performance, and sentiment\n"
                 "• Provides personalized development recommendations\n"
                 "• Identifies skill gaps and learning opportunities\n"
                 "• Check your career dashboard for detailed insights!")
    elif any(w in ml for w in ["gamification","badges","achievements","score","level"]):
        reply = ("🎮 **Gamification & Achievements:**\n"
                 "• Earn XP points for completing tasks and engaging with team\n"
                 "• Unlock badges: Task Master, Team Player, Documentation Champion\n"
                 "• Level up based on performance and contributions\n"
                 "• Daily challenges for extra engagement points\n"
                 "• Compete on the leaderboard with colleagues!")
    elif any(w in ml for w in ["sentiment","feedback","mood","feeling"]):
        reply = ("💬 **Sentiment Analysis:**\n"
                 "• AI analyzes all employee feedback automatically\n"
                 "• Scores range from Negative → Positive\n"
                 "• Low sentiment triggers HR alert emails\n"
                 "• HR can view sentiment trends in the Analytics section.")
    elif any(w in ml for w in ["email","alert","notification","remind"]):
        reply = ("📧 **Email Notification System:**\n"
                 "• Welcome email sent on registration\n"
                 "• Document reminder emails for pending uploads\n"
                 "• High-risk attrition alerts to HR team\n"
                 "• Meeting notifications to participants\n"
                 "• Low engagement alerts every 24 hours\n"
                 f"• Alert emails go to: sharma964aman@gmail.com and team")
    else:
        reply = (f"👋 Hi {username}! I'm **NexaBot**, your onboarding assistant.\n\n"
                 "I can help you with:\n"
                 "📄 Document uploads & requirements\n"
                 "📊 Onboarding progress & tasks\n"
                 "🎯 Personalized onboarding paths\n"
                 "🚀 Career progression insights\n"
                 "🎮 Gamification & achievements\n"
                 "🚨 Risk scores & alerts\n"
                 "📅 Meeting scheduling\n"
                 "🎓 Training schedule\n"
                 "💬 Sentiment & feedback analysis\n\n"
                 "What would you like to know?")

    return jsonify({"reply": reply, "source": "fallback",
                    "timestamp": datetime.now().isoformat()})

# ─── Personalized onboarding path ─────────────────────────────────────────────
@app.route("/api/onboarding/personalized-path", methods=["POST"])
def personalized_path():
    data = request.get_json() or {}
    emp = data.get("employee", {})

    if not emp:
        emp_id = data.get("employee_id")
        employees = load_employees()
        emp = next((e for e in employees if e.get("id") == emp_id), None)
        if not emp:
            return jsonify({"error": "Employee not found"}), 404

    # Analyze employee profile for personalized path
    experience = emp.get("experience_years", 0)
    department = emp.get("department", "").lower()
    role = emp.get("role", "").lower()

    # Base path structure
    path = {
        "employee_id": emp.get("id"),
        "employee_name": emp.get("name"),
        "personalized_path": [],
        "estimated_completion_days": 0,
        "focus_areas": [],
        "recommended_resources": []
    }

    # Week 1: Foundation
    week1 = {
        "week": 1,
        "theme": "Welcome & Foundation",
        "tasks": [
            "Complete HR paperwork and document uploads",
            "Attend company orientation session",
            "Set up workstation and access credentials",
            "Meet with HR and direct manager"
        ],
        "estimated_hours": 16
    }

    # Week 2-4: Role-specific training
    if experience < 2:
        # Junior level
        week2 = {
            "week": 2,
            "theme": "Technical Fundamentals",
            "tasks": [
                "Complete basic training modules",
                "Shadow senior team members",
                "Practice core tools and workflows",
                "Review company policies and procedures"
            ],
            "estimated_hours": 20
        }
        week3 = {
            "week": 3,
            "theme": "Hands-on Practice",
            "tasks": [
                "Work on sample projects",
                "Participate in team stand-ups",
                "Receive feedback on initial work",
                "Attend department-specific training"
            ],
            "estimated_hours": 25
        }
        week4 = {
            "week": 4,
            "theme": "Integration & Growth",
            "tasks": [
                "Take ownership of first real task",
                "Schedule 1:1 with mentor",
                "Join team collaboration channels",
                "Complete feedback survey"
            ],
            "estimated_hours": 20
        }
        path["estimated_completion_days"] = 21
        path["focus_areas"] = ["Technical Skills", "Team Integration", "Process Learning"]
        path["recommended_resources"] = [
            "Beginner tutorial series",
            "Mentorship program",
            "Practice project repository"
        ]

    elif experience < 5:
        # Mid-level
        week2 = {
            "week": 2,
            "theme": "Advanced Technical Training",
            "tasks": [
                "Deep dive into advanced tools",
                "Review codebase and architecture",
                "Collaborate on current projects",
                "Learn department workflows"
            ],
            "estimated_hours": 18
        }
        week3 = {
            "week": 3,
            "theme": "Leadership & Collaboration",
            "tasks": [
                "Lead small feature development",
                "Mentor junior team members",
                "Participate in design discussions",
                "Optimize existing processes"
            ],
            "estimated_hours": 22
        }
        week4 = {
            "week": 4,
            "theme": "Innovation & Impact",
            "tasks": [
                "Propose process improvements",
                "Present work to leadership",
                "Take ownership of key deliverables",
                "Plan for next quarter goals"
            ],
            "estimated_hours": 18
        }
        path["estimated_completion_days"] = 14
        path["focus_areas"] = ["Technical Leadership", "Process Optimization", "Team Mentoring"]
        path["recommended_resources"] = [
            "Advanced technical workshops",
            "Leadership training modules",
            "Industry best practices"
        ]

    else:
        # Senior level
        week2 = {
            "week": 2,
            "theme": "Strategic Integration",
            "tasks": [
                "Review team objectives and KPIs",
                "Analyze current challenges",
                "Design solutions for pain points",
                "Establish working relationships"
            ],
            "estimated_hours": 15
        }
        week3 = {
            "week": 3,
            "theme": "Leadership & Strategy",
            "tasks": [
                "Lead team planning sessions",
                "Mentor multiple team members",
                "Present strategic initiatives",
                "Optimize team performance"
            ],
            "estimated_hours": 20
        }
        week4 = {
            "week": 4,
            "theme": "Transformation & Growth",
            "tasks": [
                "Drive process improvements",
                "Implement new methodologies",
                "Plan team expansion strategies",
                "Establish best practices"
            ],
            "estimated_hours": 16
        }
        path["estimated_completion_days"] = 10
        path["focus_areas"] = ["Strategic Leadership", "Team Development", "Innovation"]
        path["recommended_resources"] = [
            "Executive leadership courses",
            "Industry conferences",
            "Strategic planning frameworks"
        ]

    # Department-specific customizations
    if "engineering" in department:
        path["focus_areas"].append("Code Quality & Best Practices")
        path["recommended_resources"].append("Code review guidelines")
    elif "marketing" in department:
        path["focus_areas"].append("Brand Guidelines & Campaign Planning")
        path["recommended_resources"].append("Marketing automation tools")
    elif "sales" in department:
        path["focus_areas"].append("CRM Training & Sales Methodology")
        path["recommended_resources"].append("Sales playbook")
    elif "hr" in department:
        path["focus_areas"].append("Compliance & Employee Relations")
        path["recommended_resources"].append("HR policy manual")

    path["personalized_path"] = [week1, week2, week3, week4]

    return jsonify(path)

# ─── Career progression insights ──────────────────────────────────────────────
@app.route("/api/career/progression-insights", methods=["POST"])
def career_progression():
    data = request.get_json() or {}
    emp = data.get("employee", {})

    if not emp:
        emp_id = data.get("employee_id")
        employees = load_employees()
        emp = next((e for e in employees if e.get("id") == emp_id), None)
        if not emp:
            return jsonify({"error": "Employee not found"}), 404

    # Analyze current performance and predict progression
    experience = emp.get("experience_years", 0)
    engagement = emp.get("engagement_score", 70)
    task_completion = emp.get("task_completion", 50)
    sentiment = emp.get("sentiment_score", 0.5)
    department = emp.get("department", "")

    # Calculate progression score (0-100)
    progression_score = min(100, (
        (engagement / 100) * 30 +           # 30% weight on engagement
        (task_completion / 100) * 25 +      # 25% weight on task completion
        (sentiment * 100 / 100) * 20 +      # 20% weight on sentiment
        min(experience * 5, 25)             # 25% weight on experience (max 5 years = 25 points)
    ))

    # Predict timeline for next level
    if progression_score >= 85:
        timeline_months = 6
        confidence = "High"
        next_role = get_next_role(emp.get("role", ""), department)
    elif progression_score >= 70:
        timeline_months = 12
        confidence = "Medium"
        next_role = get_next_role(emp.get("role", ""), department)
    elif progression_score >= 50:
        timeline_months = 18
        confidence = "Low"
        next_role = get_next_role(emp.get("role", ""), department)
    else:
        timeline_months = 24
        confidence = "Very Low"
        next_role = "Current role stabilization needed"

    # Generate personalized recommendations
    recommendations = []
    if engagement < 75:
        recommendations.append("Increase participation in team activities and meetings")
    if task_completion < 70:
        recommendations.append("Focus on improving task completion rates through better time management")
    if sentiment < 0.6:
        recommendations.append("Address concerns through regular feedback sessions with manager")
    if experience < 2:
        recommendations.append("Seek mentorship and additional training opportunities")

    # Skill development suggestions
    skill_gaps = identify_skill_gaps(emp)
    recommendations.extend(skill_gaps)

    return jsonify({
        "employee_id": emp.get("id"),
        "employee_name": emp.get("name"),
        "progression_score": round(progression_score, 1),
        "progression_level": get_progression_level(progression_score),
        "predicted_timeline_months": timeline_months,
        "confidence_level": confidence,
        "next_potential_role": next_role,
        "key_strengths": identify_strengths(emp),
        "development_recommendations": recommendations,
        "skill_development_plan": generate_skill_plan(emp),
        "generated_at": datetime.now().isoformat()
    })

# ─── Gamification & engagement scoring ────────────────────────────────────────
@app.route("/api/engagement/gamification", methods=["POST"])
def gamification_score():
    data = request.get_json() or {}
    emp = data.get("employee", {})

    if not emp:
        emp_id = data.get("employee_id")
        employees = load_employees()
        emp = next((e for e in employees if e.get("id") == emp_id), None)
        if not emp:
            return jsonify({"error": "Employee not found"}), 404

    # Calculate gamification score based on various metrics
    base_score = 0
    achievements = []
    badges = []
    level = 1
    xp_points = 0

    # Task completion achievements
    task_completion = emp.get("task_completion", 0)
    if task_completion >= 90:
        achievements.append("Task Master")
        badges.append("🏆")
        base_score += 25
        xp_points += 500
    elif task_completion >= 75:
        achievements.append("Reliable Contributor")
        badges.append("⭐")
        base_score += 15
        xp_points += 300

    # Engagement achievements
    engagement = emp.get("engagement_score", 0)
    if engagement >= 85:
        achievements.append("Team Player Extraordinaire")
        badges.append("🤝")
        base_score += 20
        xp_points += 400
    elif engagement >= 70:
        achievements.append("Active Participant")
        badges.append("💪")
        base_score += 10
        xp_points += 200

    # Document completion
    docs = emp.get("documents", {})
    completed_docs = sum(1 for v in docs.values() if v)
    total_docs = len(docs)
    doc_completion_rate = (completed_docs / total_docs * 100) if total_docs > 0 else 0

    if doc_completion_rate == 100:
        achievements.append("Documentation Champion")
        badges.append("📋")
        base_score += 15
        xp_points += 250
    elif doc_completion_rate >= 75:
        achievements.append("Well Prepared")
        badges.append("📝")
        base_score += 10
        xp_points += 150

    # Sentiment-based achievements
    sentiment = emp.get("sentiment_score", 0.5)
    if sentiment >= 0.8:
        achievements.append("Positive Vibes Only")
        badges.append("😊")
        base_score += 10
        xp_points += 200
    elif sentiment >= 0.6:
        achievements.append("Good Spirit")
        badges.append("🙂")
        base_score += 5
        xp_points += 100

    # Experience bonus
    experience = emp.get("experience_years", 0)
    if experience >= 5:
        achievements.append("Veteran Contributor")
        badges.append("🎖️")
        base_score += 10
        xp_points += 300
    elif experience >= 2:
        achievements.append("Experienced Professional")
        badges.append("👔")
        base_score += 5
        xp_points += 150

    # Calculate level based on XP
    level = max(1, min(10, xp_points // 200 + 1))

    # Next level requirements
    next_level_xp = level * 200
    xp_to_next = max(0, next_level_xp - xp_points)

    # Daily challenges (simulated)
    daily_challenges = [
        {"task": "Complete 3 tasks today", "reward": 50, "completed": task_completion > 60},
        {"task": "Attend team meeting", "reward": 30, "completed": engagement > 70},
        {"task": "Submit feedback", "reward": 25, "completed": sentiment > 0.5},
        {"task": "Help a colleague", "reward": 40, "completed": engagement > 80}
    ]

    completed_challenges = sum(1 for c in daily_challenges if c["completed"])
    challenge_score = sum(c["reward"] for c in daily_challenges if c["completed"])

    final_score = min(100, base_score + (completed_challenges * 5))

    return jsonify({
        "employee_id": emp.get("id"),
        "employee_name": emp.get("name"),
        "gamification_score": final_score,
        "level": level,
        "xp_points": xp_points,
        "xp_to_next_level": xp_to_next,
        "achievements": achievements,
        "badges": badges,
        "daily_challenges_completed": completed_challenges,
        "challenge_score": challenge_score,
        "engagement_level": "High" if final_score >= 80 else "Medium" if final_score >= 60 else "Low",
        "next_milestone": get_next_milestone(level, achievements),
        "leaderboard_position": calculate_leaderboard_position(emp),
        "generated_at": datetime.now().isoformat()
    })

def get_next_milestone(level, achievements):
    milestones = [
        "Reach Level 2",
        "Earn first badge",
        "Complete 5 achievements",
        "Reach Level 5",
        "Become a top performer",
        "Earn all department badges"
    ]

    for milestone in milestones:
        if "Level" in milestone:
            target_level = int(milestone.split()[-1])
            if level < target_level:
                return milestone
        elif "badge" in milestone.lower():
            if len(achievements) == 0:
                return milestone
        elif "achievements" in milestone.lower():
            if len(achievements) < 5:
                return milestone

    return "Master all skills and become a team leader"

def calculate_leaderboard_position(emp):
    # Simulate leaderboard calculation
    employees = load_employees()
    scores = []

    for e in employees:
        # Simple score calculation for simulation
        score = (e.get("engagement_score", 0) + e.get("task_completion", 0)) / 2
        scores.append((e.get("id"), score))

    scores.sort(key=lambda x: x[1], reverse=True)
    positions = {emp_id: pos + 1 for pos, (emp_id, _) in enumerate(scores)}

    return positions.get(emp.get("id"), len(employees))

def get_next_role(current_role, department):
    role_progression = {
        "Software Engineer": ["Senior Software Engineer", "Tech Lead", "Engineering Manager"],
        "Senior Software Engineer": ["Tech Lead", "Engineering Manager", "Director of Engineering"],
        "Marketing Manager": ["Senior Marketing Manager", "Marketing Director", "CMO"],
        "Financial Analyst": ["Senior Financial Analyst", "Finance Manager", "CFO"],
        "HR Specialist": ["Senior HR Specialist", "HR Manager", "Chief HR Officer"],
        "Sales Representative": ["Senior Sales Representative", "Sales Manager", "Sales Director"]
    }

    current_lower = current_role.lower()
    for base_role, progression in role_progression.items():
        if base_role.lower() in current_lower:
            return progression[0] if len(progression) > 0 else f"Senior {current_role}"
        for i, role in enumerate(progression):
            if role.lower() in current_lower and i + 1 < len(progression):
                return progression[i + 1]

    return f"Senior {current_role}"

def get_progression_level(score):
    if score >= 85: return "Exceptional - Ready for immediate promotion"
    if score >= 70: return "Strong - On track for advancement"
    if score >= 50: return "Good - Steady progress"
    if score >= 30: return "Needs Improvement - Focus on development"
    return "Critical - Immediate intervention required"

def identify_strengths(emp):
    strengths = []
    if emp.get("engagement_score", 0) > 80:
        strengths.append("High engagement and team participation")
    if emp.get("task_completion", 0) > 80:
        strengths.append("Excellent task completion and productivity")
    if emp.get("sentiment_score", 0) > 0.7:
        strengths.append("Positive attitude and feedback")
    if emp.get("experience_years", 0) > 3:
        strengths.append("Strong experience and domain knowledge")
    return strengths if strengths else ["Consistent performance in core responsibilities"]

def identify_skill_gaps(emp):
    gaps = []
    experience = emp.get("experience_years", 0)
    department = emp.get("department", "").lower()

    if experience < 2:
        gaps.append("Develop foundational skills in " + department)
    if "engineering" in department and experience < 3:
        gaps.append("Learn advanced programming patterns and system design")
    if "marketing" in department:
        gaps.append("Master digital marketing tools and analytics")
    if "sales" in department:
        gaps.append("Improve negotiation and relationship building skills")

    return gaps

def generate_skill_plan(emp):
    plan = []
    experience = emp.get("experience_years", 0)
    department = emp.get("department", "").lower()

    # 3-month plan
    plan.append({
        "timeframe": "Next 3 months",
        "focus": "Foundation Building",
        "actions": [
            "Complete department-specific training modules",
            "Shadow experienced team members",
            "Build relationships with key stakeholders"
        ]
    })

    # 6-month plan
    plan.append({
        "timeframe": "3-6 months",
        "focus": "Skill Development",
        "actions": [
            "Take ownership of complex projects",
            "Seek feedback and implement improvements",
            "Participate in cross-functional initiatives"
        ]
    })

    # 12-month plan
    plan.append({
        "timeframe": "6-12 months",
        "focus": "Leadership Growth",
        "actions": [
            "Mentor junior team members",
            "Lead small team projects",
            "Present work to leadership team"
        ]
    })

    return plan

if __name__ == "__main__":
    port = int(os.environ.get("PORT", 5001))
    log.info(f"🤖 NexaWorks AI starting on port {port}")
    app.run(host="0.0.0.0", port=port, debug=True)
