# Atlas Hospital Project Technical + Problem-Solving Guide

This document is a beginner-friendly, end-to-end walkthrough of the project.
It explains:
- what problem each module solves
- how frontend and backend are connected
- where each functionality is implemented (exact file paths)
- the request flow from login to database update
- how Angular, Spring Boot, packages, and Docker are used in this project

This file is intentionally detailed so you can use it for technical presentation and problem-solving presentation.

---

## 1. Problem This Project Solves

Hospitals need one workflow where:
- Admin can register patients and assign doctors
- Doctors can treat only currently assigned patients
- Referrals can transfer ownership between doctors in real time
- Case closure can detach patient from doctor and card access workflow
- Patients can view prescriptions/history and request appointments when unassigned
- Security prevents session confusion and stale access

This project solves those with role-based flows and a full-stack architecture.

---

## 2. High-Level Architecture

- Frontend: Angular SPA (role-based dashboards)
- Backend: Spring Boot REST API (auth + business logic)
- Database: PostgreSQL (users, patients, appointments, visits, cards, referrals, prescriptions)

### Core flow shape
1. User action in Angular component
2. Angular service sends HTTP request
3. Spring controller receives endpoint call
4. Service layer applies business rules
5. Repository persists to PostgreSQL
6. DTO response returns to frontend
7. UI updates immediately

---

## 3. Repository Map (Where to Find Things)

### Root
- `README.md`
- `Atlas_Project_Functionality_Guide.md` (this file)

### Frontend
- `frontend/src/app/app.routes.ts` -> route-to-page mapping
- `frontend/src/app/app.config.ts` -> router + HTTP interceptor wiring
- `frontend/src/app/interceptors/auth.interceptor.ts` -> attaches JWT + handles 401
- `frontend/src/app/services/auth.service.ts` -> login/logout/session storage
- `frontend/src/app/services/api.service.ts` -> reusable API wrapper
- `frontend/src/app/services/session-security.service.ts` -> inactivity logout timer
- `frontend/src/app/guards/auth.guard.ts` -> auth gate
- `frontend/src/app/guards/role.guard.ts` -> role gate

### Backend
- `Backend/src/main/java/com/hospital/card/config/SecurityConfig.java` -> security rules
- `Backend/src/main/java/com/hospital/card/config/JwtAuthenticationFilter.java` -> JWT validation filter
- `Backend/src/main/java/com/hospital/card/service/JwtService.java` -> JWT generate/parse
- `Backend/src/main/java/com/hospital/card/controller/*` -> REST endpoints
- `Backend/src/main/java/com/hospital/card/service/*` -> business logic
- `Backend/src/main/java/com/hospital/card/repository/*` -> JPA repositories
- `Backend/src/main/java/com/hospital/card/entity/*` -> DB model

---

## 4. Angular for Beginners (How It Is Used Here)

### 4.1 Routing and role-based page access
- File: `frontend/src/app/app.routes.ts`
- Each page has:
  - URL path
  - lazy-loaded component
  - guards (`authGuard`, `roleGuard`)
  - allowed roles in `data.roles`

Example: `/doctor-patients` can only be accessed by role `USER`.

### 4.2 Guards
- `auth.guard.ts`: blocks unauthenticated users and redirects to `/login`.
- `role.guard.ts`: blocks wrong roles and redirects them to their own portal.

### 4.3 Interceptor
- File: `frontend/src/app/interceptors/auth.interceptor.ts`
- Adds `Authorization: Bearer <token>` automatically to every API request.
- If backend returns `401`, it logs out and redirects to login.

### 4.4 Services
- `auth.service.ts` handles:
  - login
  - logout
  - current user in memory (`BehaviorSubject`)
  - session state in `sessionStorage` (tab-scoped)
- `api.service.ts` is a generic GET/POST/PUT/DELETE wrapper using `/api` base path.

### 4.5 Session inactivity
- `session-security.service.ts` tracks user activity events.
- If no activity for 10 minutes, frontend logs out and redirects to `/login?reason=session-timeout`.

---

## 5. Spring Boot for Beginners (How It Is Used Here)

### 5.1 Layered backend design

1. Controller layer (`controller/`)
- Receives HTTP calls
- Validates role via `@PreAuthorize`
- Passes work to service layer

2. Service layer (`service/`)
- Real business rules live here
- Example: referral updates assigned doctor immediately

3. Repository layer (`repository/`)
- Interfaces extending `JpaRepository`
- Handles DB queries and custom finders

4. Entity layer (`entity/`)
- Java classes mapped to DB tables

### 5.2 Security
- `SecurityConfig.java`:
  - allows public auth endpoints
  - secures all other endpoints
  - injects JWT filter before username/password filter
- `JwtAuthenticationFilter.java`:
  - validates token signature/expiry
  - validates single active session token (new login invalidates old token)

### 5.3 DTO usage
- DTOs decouple API responses from entities.
- Used in controllers/services to avoid sending full entity internals.

---

## 6. Key Functionalities and Exact Paths

## 6.1 Authentication and Security

### What it solves
- Secure login by role
- Prevent two simultaneous active logins for same user
- Auto-logout on inactivity

### Frontend paths
- `frontend/src/app/pages/login/login.component.ts`
- `frontend/src/app/services/auth.service.ts`
- `frontend/src/app/interceptors/auth.interceptor.ts`
- `frontend/src/app/services/session-security.service.ts`
- `frontend/src/app/app.ts`

### Backend paths
- `Backend/src/main/java/com/hospital/card/controller/AuthController.java`
- `Backend/src/main/java/com/hospital/card/service/AuthService.java`
- `Backend/src/main/java/com/hospital/card/service/JwtService.java`
- `Backend/src/main/java/com/hospital/card/config/JwtAuthenticationFilter.java`
- `Backend/src/main/java/com/hospital/card/entity/User.java` (`activeSessionToken`)
- `Backend/src/main/java/com/hospital/card/service/UserService.java`

### Login flow (end-to-end)
1. Angular login form submits username/password.
2. `auth.service.ts` calls `POST /api/auth/login`.
3. `AuthService.login()` authenticates via Spring Security.
4. Backend rotates `activeSessionToken` and signs JWT with `sessionToken` claim.
5. Frontend stores token in `sessionStorage`.
6. Every next API call passes token through interceptor.
7. JWT filter checks:
   - token valid
   - token session claim matches DB active session token
8. If user logs in elsewhere, old token fails and old client gets 401.

---

## 6.2 Patient Registration and Update (Admin)

### What it solves
- Admin creates patient account + profile + card
- Admin updates both user-level and patient-level data

### Frontend paths
- `frontend/src/app/pages/patient-registration/patient-registration.component.ts`
- `frontend/src/app/pages/patient-registration/patient-registration.component.html`

### Backend paths
- `Backend/src/main/java/com/hospital/card/controller/AdminController.java`
- `Backend/src/main/java/com/hospital/card/controller/PatientController.java`
- `Backend/src/main/java/com/hospital/card/controller/UserController.java`
- `Backend/src/main/java/com/hospital/card/service/PatientService.java`
- `Backend/src/main/java/com/hospital/card/service/HospitalCardService.java`

### Update flow
1. Admin opens patient edit page with `editId` query param.
2. UI loads patient data from `GET /api/patients/{id}`.
3. Save triggers two updates in parallel:
   - `PUT /api/users/{userId}`
   - `PUT /api/patients/{id}`
4. UI shows success/error message and resets loading state.

---

## 6.3 Doctor Queue, Referral, and Case Closure

### What it solves
- Doctor sees only currently assigned patients
- Referral transfers ownership immediately
- Case closure detaches patient from doctor and card workflow

### Frontend paths
- `frontend/src/app/pages/doctor-patients/doctor-patients.component.ts`
- `frontend/src/app/pages/doctor-patients/doctor-patients.component.html`

### Backend paths
- `Backend/src/main/java/com/hospital/card/controller/PatientController.java`
- `Backend/src/main/java/com/hospital/card/controller/ReferralController.java`
- `Backend/src/main/java/com/hospital/card/controller/MedicalVisitController.java`
- `Backend/src/main/java/com/hospital/card/service/ReferralService.java`
- `Backend/src/main/java/com/hospital/card/service/MedicalVisitService.java`

### Referral flow
1. Doctor submits referral to `POST /api/referrals`.
2. `ReferralService` sets patient assigned doctor to referred doctor.
3. Current doctor queue removes patient on reload.
4. Referred doctor queue includes patient immediately.

### Close-case flow
1. Doctor marks case close in UI and submits visit.
2. `POST /api/visits` with closed/completed status.
3. `MedicalVisitService`:
   - stores visit
   - unassigns doctor from patient
   - marks card inactive
4. Patient leaves active doctor queue and active card list.

---

## 6.4 Prescriptions and Auto Case History

### What it solves
- Prescription should not be isolated record only
- Every prescription also appears in clinical case history

### Frontend paths
- `frontend/src/app/pages/doctor-patients/doctor-patients.component.ts`
- `frontend/src/app/pages/patient-portal/patient-portal.component.ts`

### Backend paths
- `Backend/src/main/java/com/hospital/card/controller/PrescriptionController.java`
- `Backend/src/main/java/com/hospital/card/service/PrescriptionService.java`
- `Backend/src/main/java/com/hospital/card/service/MedicalVisitService.java`

### Flow
1. Doctor creates prescription -> `POST /api/prescriptions`.
2. `PrescriptionService` saves prescription.
3. Same service auto-creates `MedicalVisit` entry tagged for prescription history.
4. Patient sees it in both:
   - prescriptions list
   - clinical history list

---

## 6.5 Appointment Request (Patient) and Assignment (Admin)

### What it solves
- Unassigned patient can request appointment
- Admin gets actionable pending requests and assigns doctors

### Frontend paths
- Patient side:
  - `frontend/src/app/pages/patient-portal/patient-portal.component.ts`
  - `frontend/src/app/pages/patient-portal/patient-portal.component.html`
- Admin side:
  - `frontend/src/app/pages/patients/patients.component.ts`
  - `frontend/src/app/pages/patients/patients.component.html`

### Backend paths
- `Backend/src/main/java/com/hospital/card/controller/AppointmentController.java`
- `Backend/src/main/java/com/hospital/card/service/AppointmentService.java`
- `Backend/src/main/java/com/hospital/card/repository/AppointmentRepository.java`
- `Backend/src/main/java/com/hospital/card/service/NotificationService.java`

### Rules implemented
- Request button appears only if patient has no assigned doctor.
- Backend enforces same rule to prevent bypass.
- Duplicate pending requests are blocked.
- Admin endpoint returns only pending (`REQUESTED`) requests.
- Admin can assign doctor from request list.

### Flow
1. Unassigned patient clicks request.
2. Frontend calls `POST /api/appointments/request`.
3. Backend validates:
   - patient exists
   - no assigned doctor
   - no existing pending request
4. Backend saves request as `REQUESTED`.
5. Admin UI loads `GET /api/appointments/requests`.
6. Admin selects doctor and clicks assign.
7. Frontend calls `PUT /api/appointments/{id}/assign?doctorId=X`.
8. Backend updates:
   - appointment -> `ASSIGNED`
   - patient.assignedDoctor -> chosen doctor

---

## 6.6 Card Management Lifecycle

### What it solves
- Active access-card view should reflect true treatment lifecycle

### Frontend paths
- `frontend/src/app/pages/card-management/card-management.component.ts`
- `frontend/src/app/pages/card-management/card-management.component.html`

### Backend paths
- `Backend/src/main/java/com/hospital/card/controller/HospitalCardController.java`
- `Backend/src/main/java/com/hospital/card/service/HospitalCardService.java`

### Behavior
- Active card list filters to currently assigned and active-card patients.
- Reassign/unassign endpoints support admin lifecycle actions.
- Case closure flow marks card inactive.

---

## 7. End-to-End Role Journey (Presentation Friendly)

### 7.1 Admin journey
1. Login as admin.
2. Register patient and issue card.
3. Assign doctor.
4. Monitor patient list and pending appointment requests.
5. Assign doctor from appointment request panel.

### 7.2 Doctor journey
1. Login as doctor.
2. See assigned patients only.
3. Add prescription (auto-synced to case history).
4. Either refer patient or close case.

### 7.3 Patient journey
1. Login as patient.
2. See assigned doctor, history, prescriptions.
3. If unassigned, request appointment.
4. Wait for admin assignment.

---

## 8. Packages Used and Why

## 8.1 Backend (`Backend/pom.xml`)

- `spring-boot-starter-web`
  - REST API controllers and HTTP stack
- `spring-boot-starter-security`
  - authentication, authorization, filters
- `spring-boot-starter-data-jpa`
  - ORM and repositories
- `spring-boot-starter-validation`
  - request validation annotations
- `postgresql`
  - PostgreSQL JDBC driver
- `jjwt-api`, `jjwt-impl`, `jjwt-jackson`
  - JWT token generation and parsing
- `lombok`
  - reduces boilerplate (`@Data`, `@RequiredArgsConstructor`)
- `spring-dotenv`
  - environment variable support
- test dependencies
  - Spring Boot and Spring Security test support

## 8.2 Frontend (`frontend/package.json`)

- `@angular/*`
  - SPA framework and core modules
- `rxjs`
  - observables for async flow
- `tailwindcss`, `postcss`, `autoprefixer`
  - styling pipeline
- `typescript`
  - typed application code

---

## 9. Docker (How and Why)

### Existing Docker support
- File: `Backend/Dockerfile`
- Multi-stage build:
1. Maven stage builds jar
2. JDK runtime stage runs jar

### Commands (backend image)

```bash
cd Backend
docker build -t atlas-backend .
docker run -p 8080:8080 --env-file .env atlas-backend
```

Required env vars for runtime:
- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`
- `JWT_SECRET`

### What Docker gives in this project
- reproducible backend runtime
- easier deployment to cloud VM/container platform
- isolated Java + Maven environment

---

## 10. API Reference (Most Used)

### Auth
- `POST /api/auth/login`
- `POST /api/auth/register-patient`
- `GET /api/auth/me`

### Patient and doctor assignment
- `GET /api/patients`
- `GET /api/patients/{id}`
- `PUT /api/patients/{id}`

### Appointment requests
- `POST /api/appointments/request` (PATIENT)
- `GET /api/appointments/requests` (ADMIN)
- `PUT /api/appointments/{id}/assign?doctorId={id}` (ADMIN)

### Referrals and treatment
- `POST /api/referrals`
- `POST /api/prescriptions`
- `POST /api/visits`

### Cards
- `GET /api/cards/assigned-patients`
- `POST /api/cards/reassign-patient`
- `POST /api/cards/unassign-patient`

---

## 11. Common Beginner Questions (Quick Answers)

### Why both `User` and `Patient` tables?
- `User` stores auth/identity.
- `Patient` stores clinical profile and assigned doctor.
- This keeps security identity separate from domain details.

### Why use both guards and backend `@PreAuthorize`?
- Frontend guards improve UX and navigation.
- Backend authorization is the real security boundary.

### Why session invalidation in backend and timeout in frontend?
- Backend invalidation protects against concurrent stale logins.
- Frontend timeout reduces risk of unattended open sessions.

### Why service layer instead of logic in controller?
- Easier maintenance, testing, and reuse.

---

## 12. Suggested Presentation Sequence

For a demo/presentation:
1. Explain architecture and role model.
2. Show login and role-based routing.
3. Demonstrate admin assigning doctor.
4. Demonstrate doctor referral and case closure.
5. Demonstrate prescription auto-history.
6. Demonstrate patient unassigned request flow and admin assignment.
7. Show security: login from second browser invalidates first session.
8. Close with Docker deployment path.

---

## 13. Important Paths Index (Fast Lookup)

- Routes: `frontend/src/app/app.routes.ts`
- Auth frontend: `frontend/src/app/services/auth.service.ts`
- Inactivity security: `frontend/src/app/services/session-security.service.ts`
- Patient portal: `frontend/src/app/pages/patient-portal/patient-portal.component.ts`
- Admin patient page: `frontend/src/app/pages/patients/patients.component.ts`
- Doctor patients page: `frontend/src/app/pages/doctor-patients/doctor-patients.component.ts`
- Backend auth service: `Backend/src/main/java/com/hospital/card/service/AuthService.java`
- JWT filter: `Backend/src/main/java/com/hospital/card/config/JwtAuthenticationFilter.java`
- Appointment service: `Backend/src/main/java/com/hospital/card/service/AppointmentService.java`
- Prescription service: `Backend/src/main/java/com/hospital/card/service/PrescriptionService.java`
- Visit service: `Backend/src/main/java/com/hospital/card/service/MedicalVisitService.java`
- Card controller: `Backend/src/main/java/com/hospital/card/controller/HospitalCardController.java`
- Docker: `Backend/Dockerfile`

---

This guide reflects the current implemented behavior in the repository as of February 14, 2026.
