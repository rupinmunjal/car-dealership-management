# Car Dealership Management System

A full-stack web application for managing car dealerships, built with Spring Boot (backend) and Angular (frontend). The application provides comprehensive features for managing car inventory, dealer information, and user authentication.

## 🚀 Features

- **Car Management**: Add, view, update, and delete car inventory
- **Dealer Management**: Manage dealer information and profiles
- **User Authentication**: JWT-based authentication and authorization
- **RESTful API**: Well-structured REST endpoints for all operations
- **Responsive UI**: Modern Angular frontend with responsive design
- **Database**: PostgreSQL for reliable data persistence
- **Docker Support**: Containerized deployment for staging and production environments
- **Real-time Health Monitoring**: Spring Boot Actuator with frontend health indicator displaying system status

## 🛠️ Technology Stack

### Backend
- **Java 21**
- **Spring Boot 3.5.7**
- **Spring Security** (JWT authentication)
- **Spring Data JPA**
- **PostgreSQL**
- **Lombok**
- **Maven**

### Frontend
- **Angular 21**
- **TypeScript**
- **RxJS**
- **Angular Router**

### DevOps
- **Docker** & **Docker Compose**
- **Maven Wrapper**

## 📋 Prerequisites

- **Java 21** or higher
- **Node.js** 20+ and **npm** 10+
- **PostgreSQL 13+**
- **Docker** & **Docker Compose** (for containerized deployment)
- **Maven 3.9+** (or use the included Maven wrapper)

## 🔧 Installation & Setup

### Option 1: Docker Compose (Recommended)

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd car-dealership-management
   ```

2. **Build and run with Docker Compose**
   
   **For Staging Environment:**
   ```bash
   docker-compose up staging -d
   ```
   Access at: http://localhost:5000

   **For Production Environment:**
   ```bash
   docker-compose up production -d
   ```
   Access at: http://localhost:6001

3. **View logs**
   ```bash
   docker-compose logs -f staging
   # or
   docker-compose logs -f production
   ```

4. **Stop services**
   ```bash
   docker-compose down
   ```

### Option 2: Manual Setup

#### 1. Database Setup

Create a PostgreSQL database:
```sql
CREATE DATABASE carsdb;
CREATE USER cars WITH PASSWORD 'dealer';
GRANT ALL PRIVILEGES ON DATABASE carsdb TO cars;
```

#### 2. Backend Setup

1. **Configure database connection**
   
   Edit `src/main/resources/application.yml`:
   ```yaml
   spring:
     datasource:
       url: jdbc:postgresql://localhost:5432/carsdb
       username: cars
       password: dealer
   ```

2. **Build and run the backend**
   ```bash
   # Using Maven wrapper (recommended)
   ./mvnw clean install
   ./mvnw spring-boot:run
   
   # Or using Maven
   mvn clean install
   mvn spring-boot:run
   ```

   The backend will start at: http://localhost:8080

#### 3. Frontend Setup

1. **Navigate to the webapp directory**
   ```bash
   cd src/main/webapp
   ```

2. **Install dependencies**
   ```bash
   npm install
   ```

3. **Start the development server**
   ```bash
   npm start
   ```
   
   The frontend will start at: http://localhost:4200

4. **Build for production**
   ```bash
   npm run build
   ```
   This will build the Angular app and copy the static files to `src/main/resources/static/`

## 🧪 Testing

### Run All Tests
```bash
./mvnw test
```

### Run Specific Test Suites

The project includes multiple test suites:
- **Smoke Tests**: Basic functionality checks
- **Functional Tests**: Feature-level testing
- **Performance Tests**: Load and performance testing

Run specific tests:
```bash
./mvnw test -Dtest=SmokeTest
./mvnw test -Dtest=FunctionalTest
./mvnw test -Dtest=PerformanceTest
```

## 📁 Project Structure

```
car-dealership-management/
├── src/
│   ├── main/
│   │   ├── java/ca/sheridancollege/munjalru/
│   │   │   ├── A2Application.java          # Main application class
│   │   │   ├── beans/                      # Bean configurations
│   │   │   ├── config/                     # Security & app configurations
│   │   │   ├── controllers/                # REST controllers
│   │   │   │   ├── AuthenticationController.java
│   │   │   │   ├── CarRestController.java
│   │   │   │   └── DealerRestController.java
│   │   │   ├── models/                     # Entity models
│   │   │   ├── repositories/               # JPA repositories
│   │   │   └── services/                   # Business logic
│   │   ├── resources/
│   │   │   ├── application.yml             # Application configuration
│   │   │   └── static/                     # Built frontend files
│   │   └── webapp/                         # Angular frontend
│   │       ├── src/
│   │       │   ├── app/
│   │       │   │   ├── components/         # Reusable components
│   │       │   │   ├── guards/             # Route guards
│   │       │   │   ├── interceptors/       # HTTP interceptors
│   │       │   │   ├── pages/              # Page components
│   │       │   │   └── services/           # Angular services
│   │       │   ├── index.html
│   │       │   ├── main.ts
│   │       │   └── styles.css
│   │       ├── angular.json
│   │       ├── proxy-conf.json             # Dev server proxy config
│   │       └── package.json
│   └── test/                               # Test files
├── compose.yaml                             # Docker Compose configuration
├── Dockerfile                               # Container definition
├── pom.xml                                  # Maven configuration
└── README.md
```

## 🔒 Security

The application uses JWT (JSON Web Tokens) for authentication and authorization:

- **JWT Secret**: Configure in `application.yml` or environment variables
- **Token Expiration**: Customizable token lifetime
- **Password Encryption**: Bcrypt password encoding
- **CORS**: Configured for frontend-backend communication

## 🌐 API Endpoints

### Authentication
- `POST /api/auth/login` - User login (returns JWT token)
- `POST /api/auth/register` - User registration

### Cars
- `GET /api/cars` - Get all cars
- `GET /api/cars/{id}` - Get car by ID
- `POST /api/cars` - Create new car
- `PUT /api/cars/{id}` - Update car
- `DELETE /api/cars/{id}` - Delete car

### Dealers
- `GET /api/dealers` - Get all dealers
- `GET /api/dealers/{id}` - Get dealer by ID
- `POST /api/dealers` - Create new dealer
- `PUT /api/dealers/{id}` - Update dealer
- `DELETE /api/dealers/{id}` - Delete dealer

### Health Check
- `GET /actuator/health` - Application health status

## 🏥 Health Monitoring

The application includes a comprehensive health monitoring system that provides real-time visibility into the application's status.

### Backend - Spring Boot Actuator

The health endpoint is configured in `application.yml`:
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health
  endpoint:
    health:
      show-details: when-authorized
  health:
    defaults:
      enabled: true
```

**Endpoint**: `GET /actuator/health`

**Response Example**:
```json
{
  "status": "UP"
}
```

### Frontend - Real-time Health Indicator

The Angular frontend includes a health indicator component that automatically monitors the backend status.

#### Features:
- **Automatic Polling**: Checks health status every 30 seconds
- **Visual Indicators**: Color-coded status display
  - 🟢 **Green (UP)**: System is healthy and operational
  - 🔴 **Red (DOWN)**: System is down or unreachable
  - ⚫ **Gray (UNKNOWN)**: Status not yet determined
- **Real-time Updates**: Uses RxJS observables for reactive updates
- **Multiple Display Locations**:
  - Navigation bar (when logged in)
  - Fixed footer (visible on all pages)

#### Implementation:

**Health Service** (`src/main/webapp/src/app/services/health.ts`):
- Polls the `/actuator/health` endpoint
- Broadcasts status changes using BehaviorSubject
- Handles errors gracefully

**Health Indicator Component** (`src/main/webapp/src/app/components/health-indicator/`):
- Subscribes to health status updates
- Displays color-coded status badge
- Automatically updates without page refresh

#### Development Server Configuration

For the Angular development server (localhost:5000), the proxy configuration ensures health checks work correctly:

**File**: `src/main/webapp/proxy-conf.json`
```json
{
  "/api/v1": {
    "target": "http://localhost:8080",
    "secure": false
  },
  "/actuator": {
    "target": "http://localhost:8080",
    "secure": false
  }
}
```

This proxies actuator requests to the Spring Boot backend during development.

### Testing Health Monitoring

1. **Check backend health directly**:
   ```bash
   curl http://localhost:8080/actuator/health
   ```

2. **View in browser**:
   - Production: http://localhost:6001 (health indicator in UI)
   - Development: http://localhost:5000 (health indicator in UI)

3. **Test failure scenarios**:
   - Stop the Spring Boot backend
   - Observe the health indicator turn red and display "DOWN"
   - Restart the backend
   - Watch the indicator automatically update to green "UP"

## 🐳 Docker Services

The Docker Compose setup includes:

### Services
- **staging**: Staging environment (Port 5000)
- **production**: Production environment (Port 6001)
- **database**: PostgreSQL database (Port 5432)

### Networking
All services communicate through the `app-network` bridge network.

### Volumes
- `postgres-data`: Persistent storage for PostgreSQL data

## 📝 Environment Variables

Key environment variables for Docker deployment:

```env
SPRING_DATASOURCE_URL=jdbc:postgresql://database:5432/carsdb
SPRING_DATASOURCE_USERNAME=cars
SPRING_DATASOURCE_PASSWORD=dealer
SPRING_JPA_HIBERNATE_DDL_AUTO=update
MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE=health
```

## 🔧 Development

### Hot Reload

**Backend**: Spring Boot DevTools is included for automatic restart on code changes.

**Frontend**: Angular CLI provides hot reload during development:
```bash
cd src/main/webapp
npm start
```

### Code Quality

The project includes templates for:
- Code reviews (`docs/CODE_REVIEW_TEMPLATE.md`)
- Issue reporting (`docs/ISSUE_TEMPLATE.md`)

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📄 License

This project is part of academic coursework at Sheridan College.

## 👥 Authors

- **Rupin Munjal** - *Initial work*
- **Amninder Kaur** - *Initial work*


**Note**: This is an educational project developed as part of coursework at Sheridan College.
