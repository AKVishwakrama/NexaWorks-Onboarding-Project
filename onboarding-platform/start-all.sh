#!/bin/bash
# ╔══════════════════════════════════════════════════╗
# ║  NexaWorks Quick Start Script                    ║
# ║  Run this to start all 3 services                ║
# ╚══════════════════════════════════════════════════╝

echo ""
echo "╔══════════════════════════════════════════════════╗"
echo "║       NexaWorks Onboarding Platform              ║"
echo "╚══════════════════════════════════════════════════╝"
echo ""

# Check Java
if ! command -v java &> /dev/null; then
    echo "❌ Java not found. Install Java 17+ first."
    exit 1
fi

# Check Node
if ! command -v node &> /dev/null; then
    echo "❌ Node.js not found. Install Node 18+ first."
    exit 1
fi

# Check Python
if ! command -v python3 &> /dev/null; then
    echo "❌ Python3 not found. Install Python 3.9+ first."
    exit 1
fi

echo "✅ All prerequisites found"
echo ""

# Set API key if provided
if [ -n "$1" ]; then
    export ANTHROPIC_API_KEY=$1
    echo "🤖 Anthropic API key set"
fi

# Start Backend
echo "🚀 Starting Spring Boot Backend..."
cd backend
mvn spring-boot:run -q &
BACKEND_PID=$!
echo "   Backend PID: $BACKEND_PID"
cd ..

# Wait for backend
sleep 15

# Start AI Service
echo "🤖 Starting Flask AI Microservice..."
cd ai-service
pip install -r requirements.txt -q
python3 app.py &
AI_PID=$!
echo "   AI Service PID: $AI_PID"
cd ..

sleep 3

# Start Frontend
echo "🎨 Starting React Frontend..."
cd frontend
npm install -q
npm run dev &
FRONT_PID=$!
echo "   Frontend PID: $FRONT_PID"
cd ..

echo ""
echo "╔══════════════════════════════════════════════════════════╗"
echo "║  ✅ All services started!                                ║"
echo "║                                                          ║"
echo "║  🌐 Frontend:   http://localhost:5173                    ║"
echo "║  🔧 Backend:    http://localhost:8080                    ║"
echo "║  🤖 AI Service: http://localhost:5001                    ║"
echo "║                                                          ║"
echo "║  DEMO LOGINS:                                            ║"
echo "║  Employee: aarav.sharma@nexaworks.in / Emp@123456        ║"
echo "║  HR:       sunita.rao@nexaworks.in   / HR@123456         ║"
echo "║  Manager:  vikram.mehta@nexaworks.in / Mgr@123456        ║"
echo "╚══════════════════════════════════════════════════════════╝"
echo ""
echo "Press Ctrl+C to stop all services"

# Wait
wait
