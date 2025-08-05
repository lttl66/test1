#!/bin/bash

# AI Chatbot Widget Quick Start Script
# This script helps you set up and run the AI chatbot system quickly

set -e

echo "🤖 AI Chatbot Widget Quick Start"
echo "================================"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

print_step() {
    echo -e "${BLUE}[STEP]${NC} $1"
}

# Check prerequisites
check_prerequisites() {
    print_step "Checking prerequisites..."
    
    # Check Java
    if ! command -v java &> /dev/null; then
        print_error "Java is not installed. Please install Java 11 or higher."
        exit 1
    fi
    
    java_version=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}' | cut -d'.' -f1)
    if [ "$java_version" -lt 11 ]; then
        print_error "Java 11 or higher is required. Current version: $java_version"
        exit 1
    fi
    print_status "Java version: $(java -version 2>&1 | head -n 1)"
    
    # Check Maven
    if ! command -v mvn &> /dev/null; then
        print_error "Maven is not installed. Please install Maven 3.6 or higher."
        exit 1
    fi
    print_status "Maven version: $(mvn -version | head -n 1)"
    
    # Check Node.js
    if ! command -v node &> /dev/null; then
        print_error "Node.js is not installed. Please install Node.js 14 or higher."
        exit 1
    fi
    print_status "Node.js version: $(node --version)"
    
    # Check npm
    if ! command -v npm &> /dev/null; then
        print_error "npm is not installed. Please install npm."
        exit 1
    fi
    print_status "npm version: $(npm --version)"
    
    print_status "All prerequisites are met!"
}

# Setup environment variables
setup_environment() {
    print_step "Setting up environment variables..."
    
    # Create .env file if it doesn't exist
    if [ ! -f .env ]; then
        cat > .env << EOF
# Backend Configuration
OPENAI_API_KEY=your-openai-api-key-here
JWT_SECRET=mySecretKey123456789012345678901234567890
SPRING_PROFILES_ACTIVE=dev

# Database Configuration (using H2 for quick start)
DB_URL=jdbc:h2:mem:testdb
DB_USERNAME=sa
DB_PASSWORD=password

# Frontend Configuration
VUE_APP_API_URL=http://localhost:8080/api
VUE_APP_WS_URL=ws://localhost:8080/api/ws
EOF
        print_status "Created .env file with default configuration"
        print_warning "Please update the OPENAI_API_KEY in the .env file with your actual API key"
    else
        print_status ".env file already exists"
    fi
    
    # Source the .env file
    if [ -f .env ]; then
        export $(cat .env | grep -v '^#' | xargs)
        print_status "Environment variables loaded"
    fi
}

# Build and run backend
start_backend() {
    print_step "Building and starting the backend..."
    
    if [ ! -d "backend" ]; then
        print_error "Backend directory not found. Please run this script from the project root."
        exit 1
    fi
    
    cd backend
    
    # Build the project
    print_status "Building Spring Boot application..."
    mvn clean package -DskipTests
    
    # Start the backend in background
    print_status "Starting Spring Boot application..."
    nohup java -jar target/ai-chatbot-backend-1.0.0.jar > ../backend.log 2>&1 &
    BACKEND_PID=$!
    echo $BACKEND_PID > ../backend.pid
    
    cd ..
    
    # Wait for backend to start
    print_status "Waiting for backend to start..."
    for i in {1..30}; do
        if curl -s http://localhost:8080/api/chat/health > /dev/null 2>&1; then
            print_status "Backend is running on http://localhost:8080"
            break
        fi
        sleep 2
        if [ $i -eq 30 ]; then
            print_error "Backend failed to start within 60 seconds"
            exit 1
        fi
    done
}

# Setup and run frontend demo
start_frontend_demo() {
    print_step "Setting up frontend demo..."
    
    # Create a simple demo directory
    mkdir -p demo
    cd demo
    
    # Initialize package.json if it doesn't exist
    if [ ! -f package.json ]; then
        cat > package.json << EOF
{
  "name": "ai-chatbot-demo",
  "version": "1.0.0",
  "description": "AI Chatbot Widget Demo",
  "scripts": {
    "serve": "http-server . -p 3000 -c-1",
    "dev": "npm run serve"
  },
  "dependencies": {
    "http-server": "^14.1.1"
  }
}
EOF
        print_status "Created package.json for demo"
    fi
    
    # Install dependencies
    if [ ! -d node_modules ]; then
        print_status "Installing demo dependencies..."
        npm install
    fi
    
    # Create demo HTML file
    if [ ! -f index.html ]; then
        cat > index.html << 'EOF'
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>AI Chatbot Widget Demo</title>
    <link rel="stylesheet" href="https://unpkg.com/element-ui/lib/theme-chalk/index.css">
    <style>
        body {
            font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif;
            margin: 0;
            padding: 20px;
            background-color: #f5f5f5;
        }
        .demo-container {
            max-width: 1200px;
            margin: 0 auto;
            background: white;
            padding: 30px;
            border-radius: 8px;
            box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
        }
        .header {
            text-align: center;
            margin-bottom: 30px;
        }
        .demo-content {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
            gap: 20px;
            margin-bottom: 30px;
        }
        .demo-card {
            padding: 20px;
            border: 1px solid #e4e7ed;
            border-radius: 4px;
            background: #fafafa;
        }
        .chatbot-info {
            position: fixed;
            bottom: 100px;
            right: 30px;
            background: #409eff;
            color: white;
            padding: 15px;
            border-radius: 8px;
            max-width: 300px;
            box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
            animation: bounce 2s infinite;
        }
        @keyframes bounce {
            0%, 20%, 50%, 80%, 100% { transform: translateY(0); }
            40% { transform: translateY(-10px); }
            60% { transform: translateY(-5px); }
        }
        .api-test {
            margin-top: 20px;
            padding: 15px;
            background: #f0f9ff;
            border-radius: 4px;
            border-left: 4px solid #409eff;
        }
    </style>
</head>
<body>
    <div class="demo-container">
        <div class="header">
            <h1>🤖 AI Chatbot Widget Demo</h1>
            <p>This demo shows how the AI chatbot widget integrates with your management system.</p>
            <p><strong>Look for the chat button in the bottom-right corner!</strong></p>
        </div>
        
        <div class="demo-content">
            <div class="demo-card">
                <h3>📊 Dashboard Stats</h3>
                <p>Total Users: <strong>1,234</strong></p>
                <p>Active Sessions: <strong>89</strong></p>
                <p>Revenue: <strong>$45,678</strong></p>
                <p>Try asking the chatbot: "Show me the dashboard statistics"</p>
            </div>
            
            <div class="demo-card">
                <h3>👥 User Management</h3>
                <p>Manage your users efficiently</p>
                <p>Recent registrations: <strong>23</strong></p>
                <p>Pending approvals: <strong>5</strong></p>
                <p>Try asking: "How many users do we have?"</p>
            </div>
            
            <div class="demo-card">
                <h3>📦 Order Processing</h3>
                <p>Track and manage orders</p>
                <p>Pending orders: <strong>12</strong></p>
                <p>Completed today: <strong>45</strong></p>
                <p>Try asking: "Show me recent orders"</p>
            </div>
            
            <div class="demo-card">
                <h3>📈 Analytics</h3>
                <p>Business intelligence and reports</p>
                <p>Conversion rate: <strong>3.2%</strong></p>
                <p>Growth: <strong>+15%</strong></p>
                <p>Try asking: "Generate a sales report"</p>
            </div>
        </div>
        
        <div class="api-test">
            <h3>🧪 Test the API</h3>
            <p>You can test the chatbot API directly:</p>
            <button onclick="testAPI()" style="padding: 8px 16px; background: #409eff; color: white; border: none; border-radius: 4px; cursor: pointer;">
                Test Demo Message
            </button>
            <div id="api-result" style="margin-top: 10px; font-family: monospace; background: #f5f5f5; padding: 10px; border-radius: 4px; display: none;"></div>
        </div>
    </div>
    
    <div class="chatbot-info">
        <strong>💬 AI Assistant Ready!</strong><br>
        Click the chat button to start a conversation with your AI assistant.
    </div>

    <script>
        async function testAPI() {
            const resultDiv = document.getElementById('api-result');
            resultDiv.style.display = 'block';
            resultDiv.innerHTML = 'Testing API...';
            
            try {
                const response = await fetch('http://localhost:8080/api/chat/public/demo', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify({
                        message: 'Hello! This is a test message from the demo page.',
                        currentPage: '/demo',
                        systemContext: {
                            demo: true,
                            stats: {
                                users: 1234,
                                sessions: 89,
                                revenue: 45678
                            }
                        }
                    })
                });
                
                const data = await response.json();
                resultDiv.innerHTML = '<strong>API Response:</strong><br>' + JSON.stringify(data, null, 2);
            } catch (error) {
                resultDiv.innerHTML = '<strong>Error:</strong><br>' + error.message;
            }
        }
        
        // Simulate some dynamic data updates
        setInterval(() => {
            const stats = document.querySelectorAll('.demo-card p strong');
            stats.forEach(stat => {
                if (stat.textContent.includes(',')) {
                    const current = parseInt(stat.textContent.replace(/,/g, ''));
                    const newValue = current + Math.floor(Math.random() * 10) - 5;
                    stat.textContent = newValue.toLocaleString();
                }
            });
        }, 5000);
    </script>
</body>
</html>
EOF
        print_status "Created demo HTML file"
    fi
    
    # Start the demo server
    print_status "Starting demo server..."
    nohup npm run serve > ../demo.log 2>&1 &
    DEMO_PID=$!
    echo $DEMO_PID > ../demo.pid
    
    cd ..
    
    # Wait for demo server to start
    sleep 3
    print_status "Demo server is running on http://localhost:3000"
}

# Display final instructions
show_instructions() {
    print_step "Setup complete! 🎉"
    echo ""
    echo "🌐 Access your demo at: http://localhost:3000"
    echo "🔧 Backend API at: http://localhost:8080/api"
    echo "📊 Health check: http://localhost:8080/api/chat/health"
    echo ""
    echo "📝 Important Notes:"
    echo "  • Update your OpenAI API key in the .env file"
    echo "  • The demo uses a public endpoint (no authentication required)"
    echo "  • Check backend.log and demo.log for any issues"
    echo ""
    echo "🛠️  Available Commands:"
    echo "  • Stop services: ./quick-start.sh stop"
    echo "  • View logs: ./quick-start.sh logs"
    echo "  • Restart: ./quick-start.sh restart"
    echo ""
    echo "💡 Try asking the chatbot:"
    echo "  • 'Show me the dashboard statistics'"
    echo "  • 'How many users do we have?'"
    echo "  • 'Help me with user management'"
    echo ""
}

# Stop services
stop_services() {
    print_step "Stopping services..."
    
    if [ -f backend.pid ]; then
        BACKEND_PID=$(cat backend.pid)
        if kill -0 $BACKEND_PID 2>/dev/null; then
            kill $BACKEND_PID
            print_status "Backend stopped"
        fi
        rm -f backend.pid
    fi
    
    if [ -f demo.pid ]; then
        DEMO_PID=$(cat demo.pid)
        if kill -0 $DEMO_PID 2>/dev/null; then
            kill $DEMO_PID
            print_status "Demo server stopped"
        fi
        rm -f demo.pid
    fi
    
    print_status "All services stopped"
}

# Show logs
show_logs() {
    print_step "Service logs:"
    echo ""
    echo "=== Backend Logs ==="
    if [ -f backend.log ]; then
        tail -n 20 backend.log
    else
        echo "No backend logs found"
    fi
    
    echo ""
    echo "=== Demo Logs ==="
    if [ -f demo.log ]; then
        tail -n 20 demo.log
    else
        echo "No demo logs found"
    fi
}

# Main script logic
case "${1:-start}" in
    "start")
        check_prerequisites
        setup_environment
        start_backend
        start_frontend_demo
        show_instructions
        ;;
    "stop")
        stop_services
        ;;
    "restart")
        stop_services
        sleep 2
        check_prerequisites
        setup_environment
        start_backend
        start_frontend_demo
        show_instructions
        ;;
    "logs")
        show_logs
        ;;
    "status")
        print_step "Checking service status..."
        if curl -s http://localhost:8080/api/chat/health > /dev/null 2>&1; then
            print_status "Backend is running"
        else
            print_error "Backend is not running"
        fi
        
        if curl -s http://localhost:3000 > /dev/null 2>&1; then
            print_status "Demo server is running"
        else
            print_error "Demo server is not running"
        fi
        ;;
    *)
        echo "Usage: $0 {start|stop|restart|logs|status}"
        echo ""
        echo "Commands:"
        echo "  start   - Start all services (default)"
        echo "  stop    - Stop all services"
        echo "  restart - Restart all services"
        echo "  logs    - Show service logs"
        echo "  status  - Check service status"
        exit 1
        ;;
esac