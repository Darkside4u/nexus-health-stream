# Patient Management System with JWT Security & Kafka Integration

A comprehensive Spring Boot application for patient management with JWT-based authentication and Apache Kafka event streaming.

## 📋 Table of Contents
- [Features](#features)
- [Technology Stack](#technology-stack)
- [Architecture Overview](#architecture-overview)
- [Prerequisites](#prerequisites)
- [Installation & Setup](#installation--setup)
- [JWT Security Implementation](#jwt-security-implementation)
- [Kafka Integration](#kafka-integration)
- [API Documentation](#api-documentation)
- [Testing with Postman](#testing-with-postman)
- [Troubleshooting](#troubleshooting)

---

## 🎯 Features

### Core Features
- **Patient Management**: Complete CRUD operations for patient records
- **Diagnosis Tracking**: Maintain patient diagnosis history
- **Blood Group Management**: Enum-based blood group classification

### Security Features
- **JWT Authentication**: Stateless authentication using JSON Web Tokens
- **Password Encryption**: BCrypt password encoding
- **Role-Based Access Control**: ADMIN and USER roles
- **Secured Endpoints**: All APIs protected except authentication endpoint

### Kafka Integration
- **Event-Driven Architecture**: Publishes patient events to Kafka topics
- **Async Processing**: Non-blocking message publishing
- **Event Types**: CREATED, UPDATED, DELETED events
- **Multiple Consumers**: Support for different event processing patterns
- **Manual Acknowledgment**: Reliable message processing with manual acks

---

## 🛠 Technology Stack

| Technology | Version | Purpose |
|-----------|---------|---------|
| Java | 21 | Programming Language |
| Spring Boot | 4.0.1 | Framework |
| Spring Security | 7.0.2 | Authentication & Authorization |
| Spring Kafka | Latest | Kafka Integration |
| JWT (JJWT) | 0.11.5 | Token Generation & Validation |
| PostgreSQL | Latest | Database |
| Lombok | Latest | Boilerplate Code Reduction |
| Gradle | Latest | Build Tool |
| Apache Kafka | 2.x+ | Event Streaming Platform |

---

## 🏗 Architecture Overview

```
┌─────────────┐
│   Client    │
└──────┬──────┘
       │ HTTP + JWT Token
       ▼
┌─────────────────────────────────────┐
│      Spring Boot Application        │
│                                     │
│  ┌──────────────────────────────┐  │
│  │   Security Filter Chain      │  │
│  │  - JWT Request Filter        │  │
│  │  - Authentication Manager    │  │
│  └──────────────────────────────┘  │
│                                     │
│  ┌──────────────────────────────┐  │
│  │   REST Controllers           │  │
│  │  - Patient Controller        │  │
│  │  - Auth Controller           │  │
│  └──────────────────────────────┘  │
│                                     │
│  ┌──────────────────────────────┐  │
│  │   Services Layer             │  │
│  │  - Patient Service           │  │
│  │  - Kafka Producer Service    │  │
│  │  - Kafka Consumer Service    │  │
│  └──────────────────────────────┘  │
│                                     │
│  ┌──────────────────────────────┐  │
│  │   Repository Layer           │  │
│  │  - Patient Repository        │  │
│  └──────────────────────────────┘  │
└─────────────────────────────────────┘
       │                    │
       ▼                    ▼
┌──────────────┐    ┌──────────────┐
│  PostgreSQL  │    │    Kafka     │
│   Database   │    │   Cluster    │
└──────────────┘    └──────────────┘
```

---

## ✅ Prerequisites

Before you begin, ensure you have the following installed:

1. **Java Development Kit (JDK) 21**
   ```bash
   java -version
   ```

2. **PostgreSQL Database**
   - Host: localhost
   - Port: 5433
   - Database: mydatabase
   - Username: mynewuser
   - Password: secret

3. **Apache Kafka**
   - Broker: localhost:9092
   - Zookeeper (if required): localhost:2181

4. **Gradle** (or use included gradlew)

---

## 🚀 Installation & Setup

### Step 1: Clone the Repository
```bash
git clone <repository-url>
cd test
```

### Step 2: Start PostgreSQL
Ensure PostgreSQL is running on port 5433 with the configured credentials.

### Step 3: Start Kafka

#### Using Docker (Recommended)
```bash
# Start Kafka with Docker Compose (Recommended - uses KRaft mode, no Zookeeper needed)
docker-compose up -d
```

This will start:
- PostgreSQL on port 5433
- Kafka (KRaft mode) on port 9092
- Kafka UI on port 8090

**Note:** This setup uses KRaft mode (Kafka without Zookeeper), which is the modern, recommended approach.

### Step 4: Build the Application
```bash
./gradlew clean build
```

### Step 5: Run the Application
```bash
./gradlew bootRun
```

The application will start on `http://localhost:8080`

---

## 🔐 JWT Security Implementation

### How JWT Security Works

#### 1. **Authentication Flow**
```
User → POST /authenticate → AuthController
                              ↓
                    Validates Credentials
                              ↓
                    Generates JWT Token
                              ↓
                    Returns Token to User
```

#### 2. **Authorization Flow**
```
Client Request with JWT → JwtRequestFilter
                              ↓
                    Extracts & Validates Token
                              ↓
                    Sets Security Context
                              ↓
                    Proceeds to Controller
```

#### 3. **Key Components**

##### **JwtUtil.java**
- Generates JWT tokens with 24-hour expiration
- Uses 256-bit secret key for HS256 algorithm
- Extracts username from token
- Validates token signature and expiration

```java
// Token structure
{
  "sub": "user",              // Username
  "iat": 1643723400,          // Issued at
  "exp": 1643809800           // Expiration (24 hours later)
}
```

##### **JwtRequestFilter.java**
- Intercepts every request
- Extracts JWT from Authorization header
- Validates token and loads user details
- Sets authentication in SecurityContext

##### **SecurityConfig.java**
- Configures security filter chain
- Defines public and protected endpoints
- Stateless session management
- Integrates JWT filter

#### 4. **Security Features**

| Feature | Implementation |
|---------|---------------|
| Password Encryption | BCrypt with strength 10 |
| Token Algorithm | HS256 (HMAC with SHA-256) |
| Token Expiration | 24 hours (86400000 ms) |
| Session Management | Stateless (no server-side sessions) |
| CSRF Protection | Disabled (using JWT) |

---

## 📡 Kafka Integration

### Kafka Architecture in the Application

```
Patient CRUD Operations
         ↓
    PatientService
         ↓
  KafkaProducerService
         ↓
    ┌────────────────────────────────┐
    │      Kafka Topics              │
    ├────────────────────────────────┤
    │  • patient.created             │
    │  • patient.updated             │
    │  • patient.deleted             │
    │  • patient.events (all events) │
    └────────────────────────────────┘
         ↓
  KafkaConsumerService
         ↓
    Business Logic Processing
```

### Kafka Topics Configuration

| Topic Name | Partitions | Replicas | Purpose |
|-----------|------------|----------|---------|
| patient.created | 3 | 1 | New patient registrations |
| patient.updated | 3 | 1 | Patient information updates |
| patient.deleted | 3 | 1 | Patient deletions |
| patient.events | 5 | 1 | All patient events (consolidated) |

### Event Structure

```json
{
  "eventType": "CREATED",
  "timestamp": "2026-01-28T10:30:00",
  "patientId": 1,
  "name": "John Doe",
  "email": "john@example.com",
  "bloodGroup": "A_POSITIVE",
  "diagnosisDetails": "Fever and cold",
  "diagnosisDate": "2026-01-28",
  "triggeredBy": "user",
  "active": true
}
```

### Producer Configuration

```properties
# Key serialization: String (patient ID)
# Value serialization: JSON (PatientEventDto)
# Idempotence: Enabled (prevents duplicates)
# Acks: All (waits for all replicas)
# Retries: 3 attempts
```

### Consumer Configuration

```properties
# Group ID: patient-service-group
# Auto-offset reset: earliest
# Enable auto-commit: false
# Ack mode: manual (reliable processing)
# Trusted packages: * (all for JSON deserialization)
```

### Kafka Security Integration

The Kafka events capture the authenticated user from JWT token:
- `triggeredBy` field contains username from SecurityContext
- Links events to the user who performed the action
- Enables audit trail and user activity tracking

---

## 📚 API Documentation

### Authentication APIs

#### 1. Authenticate User
**Endpoint:** `POST /authenticate`  
**Access:** Public (no authentication required)  
**Purpose:** Generate JWT token

**Request Parameters:**
```
username: user
password: pass
```

**Request (Form Data):**
```bash
POST http://localhost:8080/authenticate
Content-Type: application/x-www-form-urlencoded

username=user&password=pass
```

**Response:**
```
eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyIiwiaWF0IjoxNjQzNzIzNDAwLCJleHAiOjE2NDM4MDk4MDB9.xyz...
```

---

### Patient APIs (Requires JWT Token)

All patient APIs require the JWT token in the Authorization header:
```
Authorization: Bearer <your-jwt-token>
```

#### 2. Create Patient
**Endpoint:** `POST /api/patients`  
**Access:** Authenticated users only

**Request Body:**
```json
{
  "name": "John Doe",
  "email": "john.doe@example.com",
  "bloodGroup": "A_POSITIVE",
  "patientDiagnosis": "Fever and cold symptoms",
  "diagnosisDate": "2026-01-28"
}
```

**Response:** `201 CREATED`
```json
{
  "id": 1,
  "name": "John Doe",
  "email": "john.doe@example.com",
  "bloodGroup": "A_POSITIVE",
  "patientDiagnosis": "Fever and cold symptoms",
  "diagnosisDate": "2026-01-28"
}
```

**Kafka Event Published:**
- Topic: `patient.created` and `patient.events`
- Event Type: CREATED

#### 3. Get All Patients
**Endpoint:** `GET /api/patients`  
**Access:** Authenticated users only

**Response:** `200 OK`
```json
[
  {
    "id": 1,
    "name": "John Doe",
    "email": "john.doe@example.com",
    "bloodGroup": "A_POSITIVE",
    "patientDiagnosis": "Fever and cold symptoms",
    "diagnosisDate": "2026-01-28"
  }
]
```

#### 4. Get Patient by ID
**Endpoint:** `GET /api/patients/{id}`  
**Access:** Authenticated users only

**Response:** `200 OK`
```json
{
  "id": 1,
  "name": "John Doe",
  "email": "john.doe@example.com",
  "bloodGroup": "A_POSITIVE",
  "patientDiagnosis": "Fever and cold symptoms",
  "diagnosisDate": "2026-01-28"
}
```

#### 5. Update Patient
**Endpoint:** `PUT /api/patients/{id}`  
**Access:** Authenticated users only

**Request Body:**
```json
{
  "name": "John Doe Updated",
  "email": "john.updated@example.com",
  "bloodGroup": "B_POSITIVE",
  "patientDiagnosis": "Recovered",
  "diagnosisDate": "2026-01-29"
}
```

**Response:** `200 OK`
```json
{
  "id": 1,
  "name": "John Doe Updated",
  "email": "john.updated@example.com",
  "bloodGroup": "B_POSITIVE",
  "patientDiagnosis": "Recovered",
  "diagnosisDate": "2026-01-29"
}
```

**Kafka Event Published:**
- Topic: `patient.updated` and `patient.events`
- Event Type: UPDATED

#### 6. Delete Patient
**Endpoint:** `DELETE /api/patients/{id}`  
**Access:** Authenticated users only

**Response:** `204 NO CONTENT`

**Kafka Event Published:**
- Topic: `patient.deleted` and `patient.events`
- Event Type: DELETED

---

## 🧪 Testing with Postman

### Setup

1. **Create a Postman Collection** named "Patient Management API"

2. **Set Collection Variables:**
   - `baseUrl`: `http://localhost:8080`
   - `token`: (will be set after authentication)

### Test Flow

#### Step 1: Authenticate and Get Token

```
POST {{baseUrl}}/authenticate
Body (x-www-form-urlencoded):
  username: user
  password: pass
```

**Save the response token to the `token` variable.**

#### Step 2: Set Authorization Header

For all subsequent requests, add:
```
Header:
  Authorization: Bearer {{token}}
```

#### Step 3: Create a Patient

```
POST {{baseUrl}}/api/patients
Headers:
  Authorization: Bearer {{token}}
  Content-Type: application/json
Body (JSON):
{
  "name": "Alice Smith",
  "email": "alice@example.com",
  "bloodGroup": "O_NEGATIVE",
  "patientDiagnosis": "Regular checkup",
  "diagnosisDate": "2026-01-28"
}
```

#### Step 4: Verify Kafka Events

Check your application logs to see the Kafka consumer processing the events:

```
=========================================
Consumed PATIENT CREATED event:
Patient ID: 1
Name: Alice Smith
Email: alice@example.com
Blood Group: O_NEGATIVE
Triggered By: user
=========================================
```

---

## 🔍 Monitoring Kafka

### Using Kafka Console Consumer

Monitor events in real-time:

```bash
# Monitor patient.created topic
kafka-console-consumer.sh --bootstrap-server localhost:9092 \
  --topic patient.created --from-beginning

# Monitor all patient events
kafka-console-consumer.sh --bootstrap-server localhost:9092 \
  --topic patient.events --from-beginning
```

### Using Kafka Tool / Offset Explorer

1. Download and install Kafka Tool
2. Connect to localhost:9092
3. Browse topics and view messages visually

---

## 🐛 Troubleshooting

### Common Issues and Solutions

#### 1. JWT Token Errors

**Issue:** `JWT signature does not match`
- **Cause:** Secret key mismatch between token generation and validation
- **Solution:** Ensure the same SECRET_KEY is used throughout the application lifecycle

**Issue:** `JWT expired`
- **Cause:** Token has exceeded 24-hour expiration
- **Solution:** Re-authenticate to get a new token

#### 2. Kafka Connection Issues

**Issue:** `Connection refused to localhost:9092`
- **Cause:** Kafka broker is not running
- **Solution:**
  ```bash
  # Check if Kafka is running
  netstat -an | grep 9092
  
  # Start Kafka if not running
  docker-compose up -d kafka
  # or manually start Kafka broker
  ```

**Issue:** `Topic not found`
- **Cause:** Topics not created automatically
- **Solution:** Topics are auto-created by the application. Ensure `auto.create.topics.enable=true` in Kafka config

#### 3. Database Connection Issues

**Issue:** `Connection refused to localhost:5433`
- **Cause:** PostgreSQL is not running on the configured port
- **Solution:**
  ```bash
  # Check PostgreSQL status
  sudo systemctl status postgresql
  
  # Start PostgreSQL
  sudo systemctl start postgresql
  ```

#### 4. Circular Dependency Error

**Issue:** Circular reference between SecurityConfig and JwtRequestFilter
- **Solution:** Use constructor injection and avoid @Autowired on filters. Already resolved in current implementation.

#### 5. Weak Key Exception

**Issue:** `The signing key's size is 88 bits which is not secure enough`
- **Cause:** JWT secret key is too short
- **Solution:** Use a 256-bit (32 characters minimum) secret key. Already fixed in JwtUtil.java

---

## 📝 Configuration Reference

### Application Properties

```properties
# Server Configuration
server.port=8080

# Database Configuration
spring.datasource.url=jdbc:postgresql://localhost:5433/mydatabase
spring.datasource.username=mynewuser
spring.datasource.password=secret

# JPA Configuration
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

# Kafka Configuration
spring.kafka.bootstrap-servers=localhost:9092

# Producer Settings
spring.kafka.producer.acks=all
spring.kafka.producer.retries=3

# Consumer Settings
spring.kafka.consumer.group-id=patient-service-group
spring.kafka.consumer.auto-offset-reset=earliest

# Security
spring.security.user.name=user
spring.security.user.password=pass
```

---

## 🎓 Learning Resources

### JWT Authentication
- [JWT.io](https://jwt.io/) - Token debugger and documentation
- [JJWT Library](https://github.com/jwtk/jjwt) - Java JWT library documentation

### Apache Kafka
- [Kafka Documentation](https://kafka.apache.org/documentation/)
- [Spring Kafka](https://docs.spring.io/spring-kafka/reference/)
- [Event-Driven Architecture](https://martinfowler.com/articles/201701-event-driven.html)

### Spring Security
- [Spring Security Reference](https://docs.spring.io/spring-security/reference/)
- [Baeldung Spring Security](https://www.baeldung.com/spring-security-tutorial)

---

## 🔒 Security Best Practices

1. **Never commit sensitive data:**
   - Store JWT secret in environment variables
   - Use externalized configuration for production

2. **Token Management:**
   - Implement token refresh mechanism
   - Add token blacklisting for logout
   - Use shorter expiration times in production

3. **Kafka Security:**
   - Enable SASL/SSL for production
   - Use authentication and authorization
   - Encrypt sensitive data in events

4. **Database Security:**
   - Use connection pooling
   - Enable SSL connections
   - Implement parameterized queries (already done by JPA)

---

## 📦 Project Structure

```
src/
├── main/
│   ├── java/
│   │   └── com/query/test/
│   │       ├── config/
│   │       │   └── KafkaConfig.java
│   │       ├── controller/
│   │       │   └── PatientController.java
│   │       ├── dto/
│   │       │   ├── PatientEventDto.java
│   │       │   ├── PatientRequestDto.java
│   │       │   └── PatientResponseDto.java
│   │       ├── entity/
│   │       │   ├── Patient.java
│   │       │   └── PatientDiagnosis.java
│   │       ├── exception/
│   │       │   ├── GlobalExceptionHandler.java
│   │       │   └── PatientNotFoundException.java
│   │       ├── repository/
│   │       │   ├── PatientRepository.java
│   │       │   └── PatientDiagnosisRepository.java
│   │       ├── security/
│   │       │   ├── AuthController.java
│   │       │   ├── JwtRequestFilter.java
│   │       │   ├── JwtUtil.java
│   │       │   └── SecurityConfig.java
│   │       ├── service/
│   │       │   ├── PatientService.java
│   │       │   ├── KafkaProducerService.java
│   │       │   └── KafkaConsumerService.java
│   │       └── TestApplication.java
│   └── resources/
│       └── application.properties
└── test/
    └── java/
        └── com/query/test/
            └── TestApplicationTests.java
```

---

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Open a pull request

---

## 📄 License

This project is licensed under the MIT License.

---

## 👥 Support

For issues and questions:
- Check the [Troubleshooting](#troubleshooting) section
- Review application logs
- Check Kafka consumer logs for event processing

---

## 🎉 Conclusion

You now have a fully functional Patient Management System with:
- ✅ Secure JWT authentication
- ✅ Complete CRUD operations
- ✅ Event-driven architecture with Kafka
- ✅ Reliable message processing
- ✅ Audit trail through events

Happy coding! 🚀
