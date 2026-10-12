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
$env:SPRING_DATASOURCE_URL = "jdbc:mysql://localhost:3306/mediconnect"
$env:SPRING_DATASOURCE_USERNAME = "root"
$env:SPRING_DATASOURCE_PASSWORD = "your-password"
```

## Run the frontend

```powershell
cd frontend
npm install
npm run dev
```

The frontend runs on the Vite development server and proxies `/api` requests to the backend. For a deployed frontend, set `VITE_API_URL` to the backend API base URL, including `/api` (for example, `https://your-backend.example.com/api`).

The patient dashboard includes illustrative profiles, department filters, and an appointment planner. Profile names, departments, and listed times are illustrative and are not verified healthcare providers; planner entries remain in the current browser session, are cleared on sign-out or reload, and are not sent to a clinic or saved as real appointments. The Vinay/Mentalist profile is for entertainment only and is not medical or mental-health care. Real provider verification, appointment booking, persisted schedules, and live availability require corresponding backend services.

## Patient reports and admissions

Signed-in patient accounts can view staff-entered blood test results and staff-uploaded PDF/PNG/JPEG report files (up to 10 MB), and submit admission requests by selecting a single, sharing, general, or deluxe room and a doctor from the illustrative care directory. Patients cannot upload reports. Staff sign in from the **Staff login** tab and can search registered patients, enter test results, upload reports to patient records, view existing reports, and review admission requests in the **Admissions** workspace. Staff can accept requests by assigning an available room or bed of the requested type, or put patients on the waitlist; waitlisted requests can later be accepted when a matching room or bed is available. The initial inventory is seeded idempotently with 30 Single rooms (`S1`–`S30`), 30 Sharing rooms / 60 places (`S1A`–`S30B`), 50 General beds (`G1`–`G50`), and 30 Deluxe rooms (`D1`–`D30`). Administrators can create staff accounts, manage bed inventory, review and decline admission requests, and discharge admitted patients. An admission request does not reserve a bed or confirm an admission.

The features use role-restricted backend endpoints and short-lived bearer sessions (12 hours). New registrations are patients. To bootstrap the first administrator, configure both `MEDICONNECT_ADMIN_EMAIL` and `MEDICONNECT_ADMIN_PASSWORD` in the backend environment before startup. If the configured email already belongs to a patient, startup promotes it and replaces its password with the configured administrator password. The main staff administrator signs in using the **Staff login** tab and can create staff accounts from the dedicated **Create staff** sidebar workspace; staff accounts can access patient reports and admission review, while bed inventory and staff administration remain administrator-only.

Uploaded reports are stored outside the database in `data/lab-reports` under the backend working directory by default. Set `MEDICONNECT_LAB_REPORT_DIR` to a private, access-controlled, durable storage location for deployments; protect and back up that storage alongside the database. Do not expose the upload directory as a public static directory. Configure HTTPS, secure infrastructure, backups, and applicable privacy/compliance controls before storing real patient data. MediConnect is a project feature implementation, not a claim of clinical or regulatory certification.

## Deployment

The frontend is configured for the existing Vercel project rooted at `frontend/`. Keep its `VITE_API_URL` environment variable set to the backend URL ending in `/api`, then deploy the frontend from that directory. `frontend/vercel.json` routes client-side paths to the SPA entry point.

The backend uses the Railway Docker configuration in `backend/railway.json` and `backend/Dockerfile`. In Railway, create or select a project, deploy this GitHub repository as a service with the root directory set to `backend`, and add a Railway MySQL service in the same project. Configure `SPRING_DATASOURCE_URL` with the JDBC MySQL URL using Railway's private MySQL host, port, and database, and set `SPRING_DATASOURCE_USERNAME` and `SPRING_DATASOURCE_PASSWORD` from the MySQL service variables. Set `MEDICONNECT_ADMIN_EMAIL` and a new strong `MEDICONNECT_ADMIN_PASSWORD` as Railway secrets. Do not commit or share secret values.

Attach a Railway volume to the backend service at `/app/data/lab-reports` and set `MEDICONNECT_LAB_REPORT_DIR` to that exact path; uploaded files are not stored in MySQL. Set `MEDICONNECT_CORS_ALLOWED_ORIGINS` to the exact Vercel production origin (and any custom production origin), comma-separated, without trailing slashes. After Railway generates the backend public domain, set Vercel's `VITE_API_URL` to that domain plus `/api`. After configuring these values, verify `/api/health`, patient and staff sign-in, report upload/download, and admission assignment before using the deployment.
