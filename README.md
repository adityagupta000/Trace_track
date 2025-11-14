# 🔍 Lost & Found Management Platform

<div align="center">

Enterprise-grade full-stack platform for managing lost and found items with Spring Boot & React, featuring JWT authentication, advanced security, and role-based access control.

• [Screenshots](#-screenshots) • [Features](#-features) • [Installation](#-installation) • [API Documentation](#-api-endpoints) • [Tech Stack](#-tech-stack) 
</div>

---

## 📋 Overview

A comprehensive Lost & Found management system built with **Spring Boot** and **React**, designed for campus organizations and enterprises. Features secure JWT authentication, multi-tier rate limiting, real-time messaging, and a powerful admin dashboard. The platform implements industry-standard security practices including CSRF protection, optimistic locking, and HTTP-only cookie sessions.

---

## 📸 Screenshots

### 🔐 Register
![Register Page](https://github.com/adityagupta000/Lost_And_Found_Dbms/blob/920ad653d0447d985e2882857dd9f86a49977dca/db_lab/2.png)

### 🔐 Login
![Login Page](https://github.com/adityagupta000/Lost_And_Found_Dbms/blob/main/db_lab/1.png?raw=true)

### 📝 Item Registration
![Item Registration Form](https://github.com/adityagupta000/Lost_And_Found_Dbms/blob/920ad653d0447d985e2882857dd9f86a49977dca/db_lab/4.png)

### 📊 Item Dashboard
![Item Dashboard](https://github.com/adityagupta000/Lost_And_Found_Dbms/blob/437c414a2c774a256fcb03cd4c3bf2d64998e5f3/db_lab/5.png)

### 💬 Message Popup
![Message Popup](https://github.com/adityagupta000/Lost_And_Found_Dbms/blob/437c414a2c774a256fcb03cd4c3bf2d64998e5f3/db_lab/6.png)

### 🎯 Claim Popup
![Claim Popup](https://github.com/adityagupta000/Lost_And_Found_Dbms/blob/437c414a2c774a256fcb03cd4c3bf2d64998e5f3/db_lab/7.png)

### 🏠 User Dashboard
![User Dashboard](https://github.com/adityagupta000/Lost_And_Found_Dbms/blob/437c414a2c774a256fcb03cd4c3bf2d64998e5f3/db_lab/8.png)

### 🛡️ Admin Dashboard 
![Admin Dashboard](https://github.com/adityagupta000/Lost_And_Found_Dbms/blob/437c414a2c774a256fcb03cd4c3bf2d64998e5f3/db_lab/9.png)

---

## ✨ Features

### 👥 User Features
- **🔐 Secure Authentication** – JWT tokens with refresh token rotation and HTTP-only cookies
- **📝 Item Registration** – Report lost/found items with image upload (5MB limit, validation)
- **🔍 Advanced Search** – Real-time search with filters by status (LOST/FOUND/CLAIMED)
- **📋 Claim Management** – Submit claims with race condition handling and ownership verification
- **💬 Threaded Messaging** – Direct communication with item owners
- **📊 Personal Dashboard** – View your items, claims, and messages in one place
- **💡 Feedback System** – Submit platform improvement suggestions

### 🛠️ Admin Features
- **🎛️ Admin Dashboard** – Real-time statistics with pagination and analytics
- **👥 User Management** – View, manage, and remove users (admin protection)
- **📦 Item Management** – Oversee all items with status tracking
- **✅ Claim Resolution** – Monitor and manage all claim requests
- **💌 Feedback Review** – Access all user feedback submissions
- **🔧 Rate Limit Control** – Clear rate limits and view statistics

### 🔒 Security Features
- **🛡️ Multi-tier Rate Limiting** – Bucket4j implementation (5-200 req/min by endpoint)
- **🔐 Spring Security** – Role-based access control (USER/ADMIN)
- **🍪 Session Management** – HTTP-only cookies with secure flags
- **🔑 JWT Authentication** – Access tokens (15 min) + refresh tokens (7 days)
- **🛡️ CSRF Protection** – Token-based validation for state-changing operations
- **📝 Input Sanitization** – SQL injection and XSS prevention
- **🔒 Security Headers** – XSS, HSTS, CSP, Clickjacking protection
- **⚡ Optimistic Locking** – Race condition handling for concurrent claims
- **🧹 Scheduled Cleanup** – Automated expired token and claim removal

### 🏗️ System Features
- **⚙️ RESTful API** – Spring Boot backend with comprehensive endpoints
- **🔄 Scheduled Tasks** – Automated cleanup jobs with @Scheduled
- **📸 Image Management** – Secure file storage with validation
- **🎯 AOP Implementation** – Cross-cutting concerns (rate limiting, logging)
- **📊 ModelMapper DTOs** – Clean separation between entities and responses
- **🚨 Global Exception Handling** – Centralized error management with @RestControllerAdvice
- **🗄️ JPA/Hibernate** – ORM with entity relationships and query optimization

---

## 🚀 Tech Stack

### Backend
| Technology | Version | Purpose |
|------------|---------|---------|
| **Spring Boot** | 3.x | Application framework |
| **Java** | 17+ | Programming language |
| **MySQL** | 8.0+ | Relational database |
| **Spring Security** | 6.x | Authentication & authorization |
| **JWT (jjwt)** | 0.12.x | Token-based authentication |
| **Bucket4j** | 8.x | Rate limiting implementation |
| **JPA/Hibernate** | 6.x | ORM and data persistence |
| **ModelMapper** | 3.x | DTO mapping |
| **BCrypt** | - | Password hashing |
| **Maven** | 3.8+ | Build and dependency management |

### Frontend
| Technology | Version | Purpose |
|------------|---------|---------|
| **React** | 19.1 | UI framework |
| **React Router** | 7.x | Client-side routing |
| **Tailwind CSS** | 3.4 | Utility-first styling |
| **Lucide React** | 0.525 | Icon library |
| **React Hot Toast** | 2.5 | Toast notifications |
| **SweetAlert2** | 11.x | Beautiful alerts |
| **Day.js** | 1.11 | Date formatting |
| **Fetch API** | - | HTTP client |

---

## 📁 Project Structure

### Backend Structure
```
lost-and-found-backend/
├── src/main/java/com/lostandfound/
│   ├── annotation/              # Custom annotations
│   │   └── RateLimit.java
│   ├── aspect/                  # AOP aspects
│   │   └── RateLimitAspect.java
│   ├── config/                  # Configuration classes
│   │   ├── SecurityConfig.java
│   │   ├── RateLimitConfig.java
│   │   ├── WebConfig.java
│   │   └── FileStorageProperties.java
│   ├── controller/              # REST controllers
│   │   ├── AuthController.java
│   │   ├── ItemController.java
│   │   ├── ClaimController.java
│   │   ├── MessageController.java
│   │   ├── FeedbackController.java
│   │   ├── DashboardController.java
│   │   └── AdminController.java
│   ├── dto/                     # Data Transfer Objects
│   │   ├── request/
│   │   └── response/
│   ├── exception/               # Exception handling
│   │   ├── GlobalExceptionHandler.java
│   │   ├── ResourceNotFoundException.java
│   │   ├── BadRequestException.java
│   │   └── UnauthorizedException.java
│   ├── model/                   # JPA entities
│   │   ├── User.java
│   │   ├── Item.java
│   │   ├── Claim.java
│   │   ├── Message.java
│   │   ├── Feedback.java
│   │   └── RefreshToken.java
│   ├── repository/              # JPA repositories
│   ├── security/                # Security components
│   │   ├── JwtTokenProvider.java
│   │   ├── JwtAuthenticationFilter.java
│   │   ├── RateLimitFilter.java
│   │   ├── CustomUserDetailsService.java
│   │   └── UserPrincipal.java
│   ├── service/                 # Business logic
│   ├── scheduler/               # Scheduled tasks
│   │   ├── ClaimCleanupScheduler.java
│   │   └── TokenCleanupScheduler.java
│   └── util/                    # Utility classes
│       └── CookieUtil.java
└── src/main/resources/
    └── application.properties
```

### Frontend Structure
```
lost-and-found-frontend/
├── src/
│   ├── components/              # Reusable components
│   │   ├── Header.js
│   │   ├── ItemTable.js
│   │   ├── ClaimsTable.js
│   │   ├── MessageList.js
│   │   ├── AdminTable.js
│   │   └── FeedbackForm.js
│   ├── pages/                   # Page components
│   │   ├── LoginPage.js
│   │   ├── RegisterPage.js
│   │   ├── HomePage.js
│   │   ├── ItemsPage.js
│   │   ├── ItemRegisterPage.js
│   │   └── AdminPage.js
│   ├── config/                  # Configuration
│   │   └── api.js               # API endpoints & helpers
│   ├── App.js
│   ├── App.css
│   └── index.js
├── public/
├── package.json
└── tailwind.config.js
```

---

## 🔧 Installation

### 🧩 Prerequisites
- **Java** 17 or higher
- **Node.js** 18+ and npm
- **MySQL** 8.0+
- **Maven** 3.8+

### 🖥️ Backend Setup (Spring Boot)

```bash
# Clone repository
git clone https://github.com/adityagupta000/lost-and-found-platform.git
cd lost-and-found-platform/backend

# Create MySQL database
mysql -u root -p
```

```sql
CREATE DATABASE lostandfound;
CREATE USER 'lostandfound_user'@'localhost' IDENTIFIED BY 'your_password';
GRANT ALL PRIVILEGES ON lostandfound.* TO 'lostandfound_user'@'localhost';
FLUSH PRIVILEGES;
EXIT;
```

```bash
# Configure application.properties
# Edit src/main/resources/application.properties with your database credentials

# Build and run
mvn clean install
mvn spring-boot:run

# Backend runs on http://localhost:8080
```

### 📝 application.properties Configuration

```properties
# Database Configuration
spring.datasource.url=jdbc:mysql://localhost:3306/lostandfound
spring.datasource.username=lostandfound_user
spring.datasource.password=your_password
spring.jpa.hibernate.ddl-auto=update

# JWT Configuration
jwt.secret=your-256-bit-secret-key-minimum-32-characters-long
jwt.expiration=900000
jwt.refresh.expiration=604800000

# File Upload
file.upload-dir=./uploads
spring.servlet.multipart.max-file-size=5MB
spring.servlet.multipart.max-request-size=5MB

# CORS
cors.allowed-origins=http://localhost:3000

# Rate Limiting
rate.limit.enabled=true

# Security
security.csrf.enabled=true

# Cookie Configuration
cookie.domain=localhost
cookie.secure=false
cookie.same-site=Lax
```

### 🌐 Frontend Setup (React)

```bash
cd ../frontend

# Install dependencies
npm install

# Create .env file
echo "REACT_APP_API_URL=http://localhost:8080" > .env

# Start development server
npm start

# Frontend runs on http://localhost:3000
```

---

## 🔌 API Endpoints

### Authentication Endpoints
```
POST   /api/auth/register       - Register new user
POST   /api/auth/login          - Login user (sets HTTP-only cookies)
POST   /api/auth/logout         - Logout user (clears tokens)
POST   /api/auth/refresh        - Refresh access token
GET    /api/auth/validate       - Validate current token
GET    /api/auth/me             - Get current user info
```

### Item Endpoints
```
GET    /items                   - Get all items (search & filter)
GET    /items/{id}              - Get item by ID
POST   /items                   - Create new item (multipart/form-data)
PUT    /items/{id}              - Update item
DELETE /items/{id}              - Delete item (owner/admin only)
```

### Claim Endpoints
```
GET    /claims                  - Get user's claims
POST   /claims/item/{itemId}    - Claim an item
DELETE /claims/{id}             - Delete claim (owner/admin only)
```

### Message Endpoints
```
GET    /messages                - Get received messages
GET    /messages/sent           - Get sent messages
POST   /messages                - Send new message
POST   /messages/reply          - Reply to message
DELETE /messages/{id}           - Delete message
```

### Feedback Endpoints
```
POST   /feedback                - Submit feedback
```

### Dashboard Endpoints
```
GET    /dashboard               - Get user dashboard data
```

### Admin Endpoints
```
GET    /admin/dashboard         - Get admin dashboard (paginated)
GET    /admin/items             - Get all items
GET    /admin/claims            - Get all claims
GET    /admin/users             - Get all users
GET    /admin/feedback          - Get all feedback
DELETE /admin/items/{id}        - Delete any item
DELETE /admin/claims/{id}       - Delete any claim
DELETE /admin/users/{id}        - Delete user (non-admin)
DELETE /admin/feedback/{id}     - Delete feedback
GET    /admin/rate-limit/stats  - Get rate limit statistics
DELETE /admin/rate-limit/clear/{ip}    - Clear rate limit for IP
DELETE /admin/rate-limit/clear-all     - Clear all rate limits
```

---

## 🔐 Security & Authentication

### Rate Limiting Configuration
| Endpoint Type | Requests | Duration | Protected Routes |
|--------------|----------|----------|------------------|
| **AUTH** | 5 | 1 minute | /api/auth/* |
| **API** | 100 | 1 minute | General API |
| **ADMIN** | 50 | 1 minute | /admin/* |
| **UPLOAD** | 10 | 5 minutes | File uploads |
| **PUBLIC** | 200 | 1 minute | /uploads/*, /static/* |

### Authentication Flow
1. **Registration** → Password hashed with BCrypt (strength 12)
2. **Login** → JWT access token (15 min) + refresh token (7 days)
3. **Token Storage** → HTTP-only cookies (secure, SameSite)
4. **Request Auth** → Access token validated via JwtAuthenticationFilter
5. **Token Refresh** → Automatic renewal using refresh token
6. **Logout** → All user tokens revoked and cookies cleared

### Security Features
- ✅ **CSRF Protection** with cookie-based tokens
- ✅ **XSS Prevention** with Content Security Policy
- ✅ **Clickjacking Protection** with frame-deny headers
- ✅ **HSTS** enforced (1 year max-age)
- ✅ **Input Validation** on all endpoints
- ✅ **SQL Injection Prevention** via JPA/Hibernate
- ✅ **File Upload Validation** (type, size, path traversal)
- ✅ **IP-based Rate Limiting** with proxy handling
- ✅ **Optimistic Locking** for concurrent operations

---

## 🗄️ Database Schema

### Entity Relationships
```
User (1) ──────> (*) Item
User (1) ──────> (*) Claim
User (1) ──────> (*) Message (as sender)
User (1) ──────> (*) Message (as receiver)
User (1) ──────> (*) Feedback
User (1) ──────> (1) RefreshToken
Item (1) ──────> (*) Claim
Item (1) ──────> (*) Message
```

### Key Tables
| Table | Description | Key Features |
|-------|-------------|--------------|
| **users** | User accounts | Role enum (USER/ADMIN), BCrypt password |
| **items** | Lost/found items | Status enum (LOST/FOUND/CLAIMED), version for locking |
| **claims** | Item claims | Claimant info, timestamps |
| **messages** | User messaging | Sender/receiver, item reference |
| **feedback** | Platform feedback | User submissions, timestamps |
| **refresh_tokens** | JWT tokens | Expiry tracking, revocation, IP/user agent |

---

## 🎯 Usage

### 👤 For Users
1. **Register/Login** at `http://localhost:3000`
2. **Report Item** → Navigate to "Register Item"
3. **Browse Items** → Search and filter at "Browse Items"
4. **Claim Item** → Click "Claim Item" on found items
5. **Message Owner** → Use messaging for lost items
6. **Dashboard** → View your items, claims, and messages
7. **Feedback** → Submit suggestions on homepage

### 🧑‍💼 For Admins
1. **Login as Admin** → Use admin credentials
2. **Dashboard** → View statistics and analytics
3. **Manage Users** → View/delete users (admin protected)
4. **Manage Items** → Oversee all posted items
5. **Review Claims** → Monitor claim activity
6. **View Feedback** → Access user suggestions
7. **Rate Limits** → Clear limits if needed

---

## 🧪 Testing

### Backend Testing
```bash
cd backend
mvn test
```

### Frontend Testing
```bash
cd frontend
npm test
```

### Manual API Testing
Use **Postman** or **cURL** to test endpoints:

```bash
# Login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com","password":"password123"}' \
  -c cookies.txt

# Get items (with auth cookie)
curl -X GET http://localhost:8080/items \
  -b cookies.txt
```

---

## 🐛 Known Issues & Future Enhancements

### 🔮 Planned Features
- [ ] **Email Notifications** for claims and messages
- [ ] **Real-time Chat** with WebSocket integration
- [ ] **Image Compression** before upload
- [ ] **Advanced Filters** (date range, categories)
- [ ] **Export to CSV/PDF** for reports
- [ ] **Mobile App** with React Native
- [ ] **Redis Integration** for distributed rate limiting
- [ ] **Elasticsearch** for advanced search
- [ ] **Two-Factor Authentication** (2FA)
- [ ] **Push Notifications** for updates

### 🔧 Known Issues
- [ ] Large file uploads may timeout (increase max size if needed)
- [ ] Rate limiting is in-memory (use Redis for distributed systems)

---

### 📝 Coding Standards
- Follow **Java Code Conventions** for backend
- Use **ESLint** for frontend code
- Write **meaningful commit messages**
- Add **tests** for new features
- Update **documentation** as needed

---

## 👨‍💻 Author

**Aditya Gupta**
- 📧 Email: adityagupta.d7@gmail.com
- 🐙 GitHub: [@adityagupta000](https://github.com/adityagupta000)
- 📍 Location: Mangaluru, Karnataka, India

---

## 🙏 Acknowledgments

- [Spring Boot](https://spring.io/projects/spring-boot) - Application framework
- [React](https://react.dev) - UI library
- [Bucket4j](https://bucket4j.com) - Rate limiting
- [JWT.io](https://jwt.io) - Token debugging
- [Tailwind CSS](https://tailwindcss.com) - Styling framework
- [Lucide](https://lucide.dev) - Icon library

---

## 📞 Support

For support, email **adityagupta.d7@gmail.com** or open an issue in the repository.

---

<div align="center">



[⬆ Back to Top](#-lost--found-management-platform)

</div>
