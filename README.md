# Document Signing System

A full-stack web application that allows users to upload documents, request electronic signatures from other users, preview documents, and sign them digitally using SHA-based hashing and RSA key pairs — with status automatically updated throughout the process. The cryptographic signature also allows the system to detect if a document has been tampered with after signing.
---

##  Features

-  **User Authentication** — Register/login with JWT-based stateless auth
-  **Document Upload** — Upload documents for signing
-  **Signature Requests** — Request signatures from one or more signers
-  **Document Preview** — Preview the document before signing
-  **Sign Documents** — Signers can sign, with the document status updated accordingly across the workflow

---

##  Tech Stack

**Backend**
- Java 21
- Spring Boot 3.5 (Spring Framework 6)
- Spring Security
- Spring Data JPA (Hibernate)
- MySQL (`mysql-connector-j`)
- JWT (`jjwt` 0.12.3)
- Lombok
- Maven

**Frontend**
- React (Vite)

**Infrastructure**
- Backend & Frontend deployed on **Render**
- Database hosted on **Railway (MySQL)**
- CI/CD via GitHub Actions

---

##  Live App

Frontend: [https://documentsigningsystem-1.onrender.com](https://documentsigningsystem-1.onrender.com)

---

##  Getting Started

### Prerequisites
- Java 21+
- Node.js 18+
- MySQL instance (local or hosted)
- Maven

### Backend Setup

```bash
git clone <your-repo-url>
cd backend

mvn clean install
mvn spring-boot:run
```

### Frontend Setup

```bash
cd frontend
npm install
npm run dev
```
