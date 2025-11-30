# 📌 JobPortal – Spring Boot Backend (Dockerized + Production Ready)

This is the backend of **JobPortal**, a production-ready job search platform built using **Spring Boot**, **MySQL**, **Docker**, and **JWT Authentication**. It provides APIs for users, companies, job postings, interviews, applications, and more.

---

## 🚀 Features

### 🧑‍💼 User Module

* User registration & login (JWT)
* Explore & apply to jobs
* Save jobs
* Receive email notifications
* Weekly recommended jobs

### 🏢 Company Module

* Company login & registration
* Create & manage job posts
* View applicants
* Schedule interviews
* Announcements & alerts

### 🔐 Security

* JWT Authentication (User + Company roles)
* Password hashing
* Role-based access

### 🛠 Tech Stack

* Java 17
* Spring Boot
* MySQL 8
* Spring Security
* Spring Data JPA
* Docker + Docker Compose
* Hibernate
* JWT
* Maven

---

## 🐳 Docker Setup

This backend runs with **two containers**:

1. Spring Boot API
2. MySQL 8 Database

### ▶️ Start Backend

```
docker-compose up --build
```

### 🛑 Stop Containers

```
docker-compose down
```

---

## 📁 Project Structure

```
JobPortal/
│── src/
│── uploads/                # File uploads (ignored)
│── mysql_data/             # MySQL volume data (ignored)
│── docker-compose.yml
│── Dockerfile
│── .gitignore
│── .env                    # Secrets (ignored)
│── pom.xml
│── README.md
```

---

## 🔧 Environment Variables (`.env`)

Create a `.env` file (DO NOT upload to GitHub):

```
SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/jobportal?allowPublicKeyRetrieval=true&useSSL=false
SPRING_DATASOURCE_USERNAME=root
SPRING_DATASOURCE_PASSWORD=tiger

SPRING_MAIL_USERNAME=your-email@gmail.com
SPRING_MAIL_PASSWORD=your-app-password

SECRET_KEY=your-jwt-secret-key
```

---

## 📬 API Highlights

### 🔑 Authentication

* POST `/public/userLogin`
* POST `/public/companyLogin`
* POST `/public/userSignup`
* POST `/public/companySignup`

### 🧑‍💼 Jobs

* GET `/public/getJobs`
* POST `/company/createJob`
* DELETE `/company/deleteJob/{id}`

### 📝 Applications

* POST `/user/applyJob`
* GET `/user/getApplications`
* GET `/company/getApplicants/{jobId}`

### 📅 Interviews

* POST `/company/scheduleInterview`
* GET `/user/interviewDetails`

---

## 📦 Dockerfile Used

```dockerfile
FROM eclipse-temurin:17-jdk
WORKDIR /app
COPY target/*.jar app.jar
EXPOSE 8085
ENTRYPOINT ["java", "-jar", "app.jar"]
```

---

## ☁️ Cloud Deployment

This backend works perfectly on:

* AWS EC2
* Render
* Railway
* DigitalOcean
* Docker Hub + ECS
* Kubernetes

---

## 👨‍💻 Author

**Darshan S V**
Backend Developer – Java | Spring Boot | MySQL | Docker

---

If you want more sections like **API documentation, screenshots, UML/ER diagrams, or CI/CD setup**, I can add them!
