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

The patient dashboard includes fictional profiles, department filters, and an appointment planner. Use **Explore the care directory** on the sign-in page to preview the frontend without an account; this preview access does not authenticate against the backend. Profile names, departments, and listed times are illustrative and are not verified healthcare providers; planner entries remain in the current browser session, are cleared on sign-out or reload, and are not sent to a clinic or saved as real appointments. The Vinay/Mentalist profile is for entertainment only and is not medical or mental-health care. Real provider verification, appointment booking, persisted schedules, and live availability require corresponding backend services.

## Patient reports and admissions

Signed-in patient accounts can view staff-entered blood test results, upload their own PDF/PNG/JPEG report files (up to 10 MB), and submit admission requests. Staff accounts can search registered patients, enter test results, view uploaded reports, create bed inventory, review requests, assign available beds, decline requests, and discharge admitted patients. An admission request does not reserve a bed or confirm an admission.

The features use role-restricted backend endpoints and short-lived bearer sessions (12 hours). New registrations are patients. To bootstrap the first administrator, configure both `MEDICONNECT_ADMIN_EMAIL` and `MEDICONNECT_ADMIN_PASSWORD` in the backend environment before startup. If the configured email already belongs to a patient, startup promotes it and replaces its password with the configured administrator password. An administrator can create staff accounts in the reports workspace.

Uploaded reports are stored outside the database in `data/lab-reports` under the backend working directory by default. Set `MEDICONNECT_LAB_REPORT_DIR` to a private, access-controlled, durable storage location for deployments; protect and back up that storage alongside the database. Do not expose the upload directory as a public static directory. Configure HTTPS, secure infrastructure, backups, and applicable privacy/compliance controls before storing real patient data. MediConnect is a project feature implementation, not a claim of clinical or regulatory certification.

Before deploying, configure the backend datasource, bootstrap-admin credentials, a durable private report-storage volume, and the frontend's `VITE_API_URL`. The backend CORS allowlist currently contains the default Vercel URL `https://frontend-three-rho-24.vercel.app` and local Vite URLs; update the allowlist if the deployed frontend uses a different domain.
