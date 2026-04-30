# Architecture Notes

## Overview

The application is a layered console system:

1. `Main`
   Handles input and output for patients and doctors.
2. `ClinicFacade`
   Enforces application rules and coordinates repositories.
3. Repositories
   Read and write SQLite data.
4. Domain objects
   Represent doctors, patients, appointments, and appointment states.

## Main Components

### `Main`

- Starts the database initializer
- Seeds doctors on first run
- Presents CLI menus
- Delegates business operations to `ClinicFacade`

### `ClinicFacade`

- Validates patient and doctor existence
- Calculates available appointment slots
- Rejects past-date and unavailable-slot bookings
- Restricts cancellation and completion actions to the owning patient or doctor

This is the main place for business rules.

### Persistence Layer

- `Database` centralizes JDBC connection creation
- `DatabaseInitializer` creates the schema and indexes if they do not exist
- `DoctorRepository`, `PatientRepository`, and `AppointmentRepository` isolate SQL from the rest of the app

## Database Design

### `doctors`

- `id`
- `name`
- `specialization`
- `work_start`
- `work_end`
- `slot_minutes`

### `patients`

- `id`
- `name`
- `phone`

### `appointments`

- `id`
- `doctor_id`
- `patient_id`
- `date_time`
- `status`

## Integrity Rules

- Foreign keys are enabled on every connection.
- A unique index on `(doctor_id, date_time)` prevents double-booking.
- Database initialization is non-destructive and preserves existing data.

## State Management

Appointments use the State pattern:

- `NewState`
- `ConfirmedState`
- `CancelledState`
- `CompletedState`

Each state controls which transitions are allowed.

## Testing Strategy

Tests use temporary SQLite database files via `clinic.db.path` so they:

- do not modify the real `clinic.db`
- can verify persistence behavior across initializer calls
- can validate booking and ownership rules deterministically

## Recommended Next Improvements

- Replace `String` status values with an enum-backed persistence strategy
- Add joined appointment views with doctor and patient names
- Support configurable doctor schedules in the UI
- Introduce service-level exceptions or result objects instead of boolean-only outcomes
