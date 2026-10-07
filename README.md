# MediConnect

MediConnect is a full-stack application with a Spring Boot backend and a React/Vite frontend.

## Requirements

- Java 25+
- Maven 3.9+ or the included Maven wrapper
- Node.js 18+

## Run the backend

```powershell
cd backend
./mvnw spring-boot:run
```

On Windows PowerShell, use `./mvnw.cmd spring-boot:run`.

Before starting the backend, create a MySQL database named `mediconnect` and configure the connection variables if your local values differ:

```powershell
$env:DB_HOST = "localhost"
$env:DB_PORT = "3306"
$env:DB_NAME = "mediconnect"
$env:DB_USERNAME = "root"
$env:DB_PASSWORD = "your-password"
```

## Run the frontend

```powershell
cd frontend
npm install
npm run dev
```

The frontend runs on the Vite development server and proxies `/api` requests to the backend.

The patient dashboard includes fictional profiles, department filters, and an appointment planner. Use **Explore the care directory** on the sign-in page to preview the frontend without an account; this preview access does not authenticate against the backend. Profile names, departments, and listed times are illustrative and are not verified healthcare providers; planner entries remain in the current browser session, are cleared on sign-out or reload, and are not sent to a clinic or saved as real appointments. The Vinay/Mentalist profile is for entertainment only and is not medical or mental-health care. Real provider verification, appointment booking, persisted schedules, and live availability require corresponding backend services.
