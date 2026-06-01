<p align="center">
  <img src="assets/ticketpigeon-banner.png" width="100%">
</p>

# TicketPigeon

A cloud-native movie ticket booking backend built using Java, Spring Boot, Spring Cloud, Eureka Service Discovery, API Gateway, JWT Authentication, and PostgreSQL.

![Release Pipeline](https://github.com/sha5git/ticketpigeon/actions/workflows/release.yml/badge.svg)

---

## 🚀 Project Overview

TicketPigeon is a microservices-based backend platform for movie ticket booking systems.

The platform is composed of independently deployable services communicating through service discovery and API gateway routing.

### Services

| Service           | Port | Purpose                  |
| ----------------- | ---- | ------------------------ |
| Service Discovery | 8761 | Eureka Server            |
| API Gateway       | 8080 | Entry point for all APIs |
| Auth Service      | 8081 | Authentication & JWT     |
| Movie Service     | 8082 | Movie Management         |
| Theater Service   | 8083 | Theater Management       |

---

## 🏗️ Architecture

```text
Client
   │
   ▼
API Gateway (8080)
   │
   ├─────────────► Auth Service (8081)
   │
   ├─────────────► Movie Service (8082)
   │
   └─────────────► Theater Service (8083)

             ▲
             │
      Eureka Service Discovery (8761)
```

---

## 🛠️ Tech Stack

### Backend

* Java 17
* Spring Boot 3
* Spring Cloud Gateway
* Netflix Eureka
* Spring Security
* JWT Authentication
* Spring Data JPA
* PostgreSQL

### DevOps & Automation

* GitHub Actions
* GitHub Releases
* Automated Deployment Scripts
* Termux
* Linux Shell Scripting

---

## ⚙️ CI/CD Pipeline

TicketPigeon uses a GitHub Release based deployment workflow.

```text
git push
    ↓
GitHub Release
    ↓
GitHub Actions Build
    ↓
JAR Artifacts Published
    ↓
Samsung Galaxy A71 (Termux)
    ↓
Automatic Deployment
    ↓
Service Verification
    ↓
Production Ready
```

### Deployment Features

* Automated JAR downloads from GitHub Releases
* Automatic service restart
* Eureka registration verification
* Version tracking
* Background update watcher
* Zero-touch deployments

---

## 📦 Current Deployment Model

TicketPigeon is deployed on a self-hosted Android environment using:

* Samsung Galaxy A71
* Termux
* OpenJDK 17

The deployment pipeline automatically:

1. Detects new GitHub releases
2. Downloads updated JARs
3. Stops running services
4. Replaces artifacts
5. Starts services
6. Verifies successful deployment

---

## 🔍 Service Discovery

Services register themselves with Eureka using IP-based registration:

```yaml
eureka:
  instance:
    prefer-ip-address: true
    instance-id: ${spring.application.name}:${server.port}
```

Example registrations:

```text
api-gateway:8080
auth-service:8081
movie-service:8082
theater-service:8083
```

---

## 📂 Repository Structure

```text
ticketpigeon
│
├── service-discovery
├── api-gateway
├── auth-service
├── movie-service
├── theater-service
├── common-lib
│
└── .github
    └── workflows
        └── release.yml
```

---

## 🚧 Future Enhancements

* Booking Service
* Seat Management
* Show Scheduling
* Payment Integration
* Docker Deployment
* Kubernetes Support
* Monitoring Dashboard
* Automated Health Reporting

---

## 👨‍💻 Author

### Shashank Shekhar

Java Backend Developer | Spring Boot | AWS | Microservices | Distributed Systems

- 📧 Email: shashank.jfsd@gmail.com
- 💼 LinkedIn: https://www.linkedin.com/in/shashank-2906/
- 🐙 GitHub: https://github.com/sha5git