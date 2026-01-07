# 🚗 Smart Parking Management System

![Project Status](https://img.shields.io/badge/Status-Active-brightgreen)
![Java Version](https://img.shields.io/badge/Java-17-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.2-success)
![React](https://img.shields.io/badge/React-19.0-blue)
![License](https://img.shields.io/badge/License-MIT-blue)

## 📖 Project Overview

The **Smart Parking Management System** is a full-stack web application designed to solve the urban parking crisis. It connects parking slot owners with drivers looking for parking space, enabling seamless booking, payment, and management of parking slots.

### 🌟 Key Features
*   **User Role Management**: Distinct dashboards for Users, Parking Owners, and Admins.
*   **Real-time Booking**: Users can search, view availability, and book slots instantly.
*   **Interactive Map**: Visual parking slot location using Leaflet maps.
*   **Secure Payment**: Integrated Razorpay and UPI QR code generation for payments.
*   **JWT Authentication**: Secure, stateless authentication for all API requests.

---

## 🛠️ Tech Stack

### Backend
| Technology | Badge | Purpose |
| :--- | :--- | :--- |
| **Java** | ![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white) | Core Programming Language (Version 17) |
| **Spring Boot** | ![Spring Boot](https://img.shields.io/badge/Spring_Boot-F2F4F9?style=for-the-badge&logo=spring-boot) | Backend Framework |
| **MySQL** | ![MySQL](https://img.shields.io/badge/MySQL-00000F?style=for-the-badge&logo=mysql&logoColor=white) | Relational Database |
| **Hibernate/JPA** | ![Hibernate](https://img.shields.io/badge/Hibernate-59666C?style=for-the-badge&logo=hibernate&logoColor=white) | ORM for Database Interaction |
| **JWT** | ![JWT](https://img.shields.io/badge/JWT-black?style=for-the-badge&logo=JSON%20web%20tokens) | Security & Authentication |

### Frontend
| Technology | Badge | Purpose |
| :--- | :--- | :--- |
| **React.js** | ![React](https://img.shields.io/badge/React-20232A?style=for-the-badge&logo=react&logoColor=61DAFB) | Frontend Library |
| **Axios** | ![Axios](https://img.shields.io/badge/Axios-5A29E4?style=for-the-badge&logo=axios&logoColor=white) | HTTP Client for API Calls |
| **Leaflet** | ![Leaflet](https://img.shields.io/badge/Leaflet-199900?style=for-the-badge&logo=Leaflet&logoColor=white) | Interactive Map Components |
| **CSS** | ![CSS3](https://img.shields.io/badge/CSS3-1572B6?style=for-the-badge&logo=css3&logoColor=white) | Custom Styling |

### Deployment
| Platform | Badge |
| :--- | :--- |
| **Render** | ![Render](https://img.shields.io/badge/Render-46E3B7?style=for-the-badge&logo=render&logoColor=white) | Backend Hosting |
| **Vercel** | ![Vercel](https://img.shields.io/badge/Vercel-000000?style=for-the-badge&logo=vercel&logoColor=white) | Frontend Hosting |
| **TiDB Cloud** | ![TiDB](https://img.shields.io/badge/TiDB-444444?style=for-the-badge&logo=tidb&logoColor=white) | MySQL-Compatible Cloud Database |

---

## 🏗️ System Architecture

The application follows a standard **MVC (Model-View-Controller)** architecture with a separate frontend and backend.

```mermaid
graph TD
    Client[React Frontend] -->|REST API Calls (JSON)| Controller[Spring Controllers]
    Controller -->|Business Logic| Service[Service Layer]
    Service -->|CRUD Operations| Repository[JPA Repository]
    Repository -->|SQL Queries| Database[(MySQL Database)]
    
    subgraph Security
    AuthFilter[JWT Auth Filter] --> Controller
    end
```

### 🔄 Data Flow (Booking Example)
1.  **User** selects a slot and clicks "Book" on Frontend.
2.  **Frontend** sends `POST /api/bookings` request with JWT token.
3.  **Backend Security** validates the token.
4.  **BookingService** checks slot availability (concurrency check).
5.  **Repository** saves the booking to MySQL.
6.  **Response** is sent back to Frontend to show "Booking Confirmed".

---

## 📂 Project Structure

```bash
Smart-Parking-System/
├── smart-parking-backend/       # Spring Boot Application
│   ├── src/main/java/com.../    # Java Source Code
│   │   ├── controller/          # API Endpoints
│   │   ├── model/               # Database Entities
│   │   ├── service/             # Business Logic
│   │   └── repository/          # DB Interaction
│   └── src/main/resources/      # Config (application.properties)
│
└── smart-parking-frontend/      # React Application
    ├── public/                  # Static assets
    └── src/
        ├── components/          # Reusable UI Components
        ├── pages/               # Main Page Views
        ├── services/            # API Integration Files
        └── styles/              # CSS Files
```

---

## 🔌 API Documentation

| Method | Endpoint | Description | Interaction |
| :--- | :--- | :--- | :--- |
| **Auth** | | | |
| `POST` | `/api/users/login` | Authenticates user & returns JWT | User/Owner/Admin |
| `POST` | `/api/users/signup` | Registers a new user | Public |
| **Parking Slots** | | | |
| `GET` | `/api/slots` | Get all available parking slots | Public |
| `POST` | `/api/slots` | Create a new slot | Admin/Owner |
| `GET` | `/api/slots/{id}/availability` | Check if a slot is free | User |
| **Bookings** | | | |
| `POST` | `/api/bookings` | Book a specific slot | User |
| `GET` | `/api/bookings/me` | Get current user's history | User |
| `POST` | `/api/bookings/{id}/pay` | Process payment for booking | User |

---

## 🚀 Getting Started (Local Setup)

### Prerequisites (Strict Versioning)
*   **Java Development Kit (JDK)**: Version **17** or higher.
*   **Node.js**: Version **18.x** or **20.x**.
*   **MySQL**: Version **8.0+** running on port `3306`.
*   **Maven**: For building the backend.

### 1️⃣ Database Setup
1.  Open MySQL Workbench or CLI.
2.  Create the database:
    ```sql
    CREATE DATABASE smart_parking_refractored;
    ```
3.  (Optional) The tables will be auto-generated by Hibernate.

### 2️⃣ Backend Setup
1.  Navigate to the backend folder:
    ```bash
    cd smart-parking-backend
    ```
2.  Update `src/main/resources/application.properties` with your credentials:
    ```properties
    spring.datasource.username=YOUR_MYSQL_USERNAME
    spring.datasource.password=YOUR_MYSQL_PASSWORD
    ```
3.  Run the application:
    ```bash
    mvn spring-boot:run
    ```
    *The server will start on `http://localhost:8080`*

### 3️⃣ Frontend Setup
1.  Navigate to the frontend folder:
    ```bash
    cd ../smart-parking-frontend
    ```
2.  Install dependencies:
    ```bash
    npm install
    ```
3.  Start the development server:
    ```bash
    npm start
    ```
    *The app will open at `http://localhost:3000`*

---

---

# ☁️ Deployment Guide (Step-by-Step for Beginners)

This guide will take you from "Zero" to "Live Production" using free tools: **TiDB (Database)**, **Render (Backend)**, and **Vercel (Frontend)**.

## 1️⃣ Pre-Deployment Checklist
Before deploying, ensure:
*   [ ] **GitHub Repo**: Your project is pushed to GitHub.
*   [ ] **Clean Code**: Remove any hardcoded passwords (like `Your root DB password`) from `application.properties` before committing, if possible. *We will inject them safely using Environment Variables.*
*   [ ] **Database Clean**: You don't need to migrate data manually; the backend will create tables automatically on the cloud database.

---

## 2️⃣ Database Deployment (TiDB Cloud)
We use **TiDB Cloud** because it's a free, MySQL-compatible Serverless database.

### **Step 1: Create Cluster**
1.  Go to [TiDB Cloud](https://tidbcloud.com/) and Sign Up.
2.  Click **"Create Cluster"** -> Select **"Serverless"** (Free Forever).
3.  Region: Select the one closest to you (e.g., **AWS Mumbai** or **Oregon**).
4.  Give your cluster a name (e.g., `SmartParkingDB`) and click **Create**.

### **Step 2: Get Connection Details**
1.  Once created, click on your cluster name.
2.  Click **"Connect"** (Top Right).
3.  Select **"Generate Password"** -> **Copy and Save this Password safely!**
4.  Look for the **"Connect with Your Client"** tab (or "Standard Connection").
5.  Note down the following values:

| Field | Example Value (Yours will be different) |
| :--- | :--- |
| **Host** | `gateway01.ap-southeast-1.prod.aws.tidbcloud.com` |
| **Port** | `4000` |
| **User** | `2GFKs24.root` |
| **Password** | `(The one you generated)` |
| **Database Name** | `test` (We will rename this in the connection string) |

---

## 3️⃣ Backend Deployment (Render)
Render will host our Spring Boot backend.

### **Step 1: Create Service**
1.  Go to [Render.com](https://render.com/) and Log In with GitHub.
2.  Click **"New +"** -> **"Web Service"**.
3.  Select your **Smart-Parking-System** repository.
4.  **Root Directory**: `smart-parking-backend` (Important! Don't leave empty).
5.  **Runtime**: `Java` (It will auto-detect Maven).
6.  **Build Command**: `mvn clean package`
7.  **Start Command**: `java -jar target/smart-parking-backend-0.0.1-SNAPSHOT.jar`
8.  **Instance Type**: Free.

### **Step 2: Environment Variables (The Magic Bridge)**
Scroll down to **"Environment Variables"** and click **"Add Environment Variable"**. Add these EXACT keys and your specific values from TiDB:

| Key | Value to Paste | Description |
| :--- | :--- | :--- |
| `SPRING_DATASOURCE_URL` | `jdbc:mysql://YOUR_TIDB_HOST:4000/smart_parking_refractored?sslMode=VERIFY_IDENTITY&useSSL=true` | **Replace `YOUR_TIDB_HOST`** with the Host from TiDB. We added `/smart_parking_refractored` to create that specific DB. |
| `SPRING_DATASOURCE_USERNAME` | `YOUR_TIDB_USER` | Paste your TiDB User (e.g., `2GFKs24.root`) |
| `SPRING_DATASOURCE_PASSWORD` | `YOUR_TIDB_PASSWORD` | Paste the password you saved |
| `JAVA_VERSION` | `17` | Ensures Render uses Java 17 |

> [!IMPORTANT]
> **Double Check**: Did you replace `YOUR_TIDB_HOST` in the URL?

Click **"Create Web Service"**. Render will start building.
*   **Success**: You will see "Build Successful" and "Deploy Live" in the logs.
*   **Copy URL**: Top left, it looks like `https://smart-parking-backend.onrender.com`. **Copy this!**

---

## 4️⃣ Frontend Deployment (Vercel)
Vercel will host our React frontend and talk to the Render backend.

### **Step 1: Import Project**
1.  Go to [Vercel.com](https://vercel.com/) and Log In with GitHub.
2.  Click **"Add New..."** -> **"Project"**.
3.  Import **Smart-Parking-System**.

### **Step 2: Configuration**
1.  **Framework Preset**: Create React App (Auto-detected).
2.  **Root Directory**: Click "Edit" and select `smart-parking-frontend`.
3.  **Build Command**: `npm run build` (Default).

### **Step 3: Environment Variables**
Expand the **"Environment Variables"** section.

| Key | Value | Description |
| :--- | :--- | :--- |
| `REACT_APP_API_URL` | `https://your-backend-name.onrender.com` | **Paste the Render Backend URL** here. Do NOT add a trailing slash `/`. |

Click **"Deploy"**.
*   Vercel will build the React app.
*   Once done, you will get a **Production URL** (e.g., `https://smart-parking-frontend.vercel.app`).

---

## 5️⃣ Verification & Testing

### **How flows connect now:**
`User (Browser)` ➡️ `Vercel (Frontend)` ➡️ `Render (Backend)` ➡️ `TiDB (Database)`

### **Test It:**
1.  Open your **Vercel URL**.
2.  Go to **Signup**: Create a new user.
    *   *If this works, Frontend -> Backend -> Database connection is perfect.*
3.  **Login**: Try logging in.
    *   *If this works, JWT Token generation is working.*
4.  **Dashboard**: Check if data loads.

---

## 6️⃣ Common Deployment Errors

| Error | Fix |
| :--- | :--- |
| **White Screen on Vercel** | Check if you set the `Root Directory` to `smart-parking-frontend`. |
| **API Errors (Network Error)** | check if `REACT_APP_API_URL` in Vercel is `https` (not http) and has NO trailing slash. |
| **Backend "CrashLoopBackOff"** | Check Render Logs. Usually means Database Connection failed. Verify TiDB password and URL format. |
| **CORS Error** | Ensure your Backend Controller accepts origins (or use `*` for testing). |

---

## ⚠️ Troubleshooting & Common Errors

| Error | Possible Cause | Solution |
| :--- | :--- | :--- |
| **`Connection Refused` (Backend)** | MySQL is not running or wrong credentials | Check MySQL service and verify `application.properties`. |
| **`403 Forbidden` (Frontend)** | JWT Token missing or expired | Logout and Login again to refresh the token. |
| **`CORS Error`** | Frontend/Backend port mismatch | Ensure `@CrossOrigin` is enabled in Controllers or global config. |
| **`'npm' is not recognized`** | Node.js not installed | Install Node.js v18+ and restart terminal. |
| **Map not loading** | Leaflet CSS missing | Ensure leaflet CSS is imported in `index.js`. |

---

## 🔮 Future Scope
*   **IoT Integration**: Smart sensors to detect car presence automatically.
*   **Mobile App**: React Native version for iOS/Android.
*   **Subscription Models**: Monthly passes for regular commuters.
*   **Dynamic Pricing**: Surge pricing during peak hours.

---

### 👨‍💻 Contributors
Developed with ❤️ by **Jayamurugan**.
