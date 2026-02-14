# Atlas Hospital Card Management

Atlas is a full-stack hospital workflow system for patient lifecycle management, doctor assignment, referrals, prescriptions, medical case history, and access-card tracking.

It includes:
- Angular frontend (`frontend/`)
- Spring Boot backend (`Backend/`)
- PostgreSQL persistence

## Overview

The system is role-driven:
- `ADMIN` manages users, patients, assignments, and card/access operations.
- `USER` (Doctor) handles currently assigned patients, treatment, referrals, prescriptions, and case closure.
- `PATIENT` views profile/history/prescriptions and can request appointments when unassigned.

## Current Workflow Logic

### Admin to Doctor assignment
- Admin assigns a doctor to a patient.
- Patient appears immediately in that doctor's patient queue.
- Access-card list includes patients with active cards and active doctor assignment.

### Doctor treatment flow
- Doctor can add prescriptions.
- Prescription is automatically written to prescription history and also auto-logged into medical case history.
- Doctor can:
1. Refer patient to another doctor:
   - Patient is removed from current doctor queue.
   - Patient appears in referred doctor queue immediately.
   - Assigned doctor updates on patient/admin views.
2. Close case:
   - Case is saved with closed/completed status.
   - Patient is detached from doctor assignment.
   - Card is set inactive and removed from active access-card workflow.

### Patient appointment request flow
- "Request Appointment" appears only when patient has no assigned doctor.
- Request creates a pending appointment request for admin.
- Admin sees pending requests and can assign a doctor directly from admin patient interface.
- Assignment updates both:
  - appointment request status (`ASSIGNED`)
  - patient's assigned doctor

## Security Model

### Authentication and authorization
- JWT-based authentication via Spring Security.
- Role-based endpoint protection (`ADMIN`, `USER`, `PATIENT`).

### Single active session per account
- New login rotates the user's active session token.
- Previous JWT becomes invalid and is rejected on next request.
- Result: latest login wins, old login is terminated.

### Frontend session hardening
- Session state stored in `sessionStorage` (tab-scoped).
- Automatic logout after 10 minutes of inactivity.

## Tech Stack

### Backend
- Java 21
- Spring Boot 3.x
- Spring Security + JWT
- Spring Data JPA / Hibernate
- PostgreSQL

### Frontend
- Angular (standalone components)
- RxJS
- Tailwind CSS

## Repository Structure

```text
Backend/     Spring Boot API and domain logic
frontend/    Angular client application
README.md    Project documentation
```

## Setup

### Prerequisites
- JDK 21
- Node.js + npm
- PostgreSQL

### Backend
1. Configure environment variables or `Backend/src/main/resources/application.properties`:
   - `DB_URL`
   - `DB_USERNAME`
   - `DB_PASSWORD`
   - `JWT_SECRET`
2. Run backend:
   - Windows:
     ```bash
     cd Backend
     .\\mvnw.cmd spring-boot:run
     ```
   - Unix/macOS:
     ```bash
     cd Backend
     ./mvnw spring-boot:run
     ```

### Frontend
```bash
cd frontend
npm install
npm run start
```

Default URL: `http://localhost:4200`

## Build and Test

### Backend
```bash
cd Backend
.\\mvnw.cmd -DskipTests compile
.\\mvnw.cmd test
```

### Frontend
```bash
cd frontend
npm run build
```

## Main API Endpoints

### Auth
- `POST /api/auth/login`
- `POST /api/auth/register-patient`
- `GET /api/auth/me`

### Patients and assignment
- `GET /api/patients`
- `GET /api/patients/{id}`
- `PUT /api/patients/{id}`

### Appointments
- `POST /api/appointments/request` (patient)
- `GET /api/appointments/requests` (admin pending requests)
- `PUT /api/appointments/{id}/assign?doctorId={doctorId}` (admin assignment)

### Doctor operations
- `POST /api/referrals`
- `POST /api/prescriptions`
- `POST /api/visits`

### Cards
- `GET /api/cards/assigned-patients`

## Notes

- Database schema auto-updates via Hibernate (`spring.jpa.hibernate.ddl-auto=update`).
- Existing active sessions are invalidated when same user logs in from another place.
