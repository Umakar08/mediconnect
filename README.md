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

The patient dashboard includes sample profiles, department filters, and a demo-booking flow. Use **Explore the demo dashboard** on the sign-in page to preview the frontend without an account; this demo entry does not authenticate against the backend. Profile names, departments, and schedules are demonstration data, not verified healthcare providers; bookings remain in the current browser session and are cleared on sign-out or reload. The Vinay/Mentalist profile is for entertainment only and is not medical or mental-health care. Persisted appointments and live schedules require corresponding backend APIs.
