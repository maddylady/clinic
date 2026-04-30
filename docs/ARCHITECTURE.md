# Architecture Notes

## Overview

The application is a layered desktop system with a console fallback:

1. `Main`
   Starts the app and selects desktop or headless console mode.
2. `ui`
   Swing screens and user interactions.
3. `ClinicFacade`
   Enforces business rules and returns operation results.
4. Repositories
   Read and write SQLite data.
5. Domain objects and DTOs
   Represent doctors, patients, appointments, states, and joined UI summaries.

## Main Components

### `Main`

- Starts the database initializer
- Seeds doctors on first run
- Opens the Swing UI when graphics are available
- Falls back to CLI menus in headless environments
- Delegates business operations to `ClinicFacade`

### `ui`

- `ClinicDesktopApp` owns the desktop dashboard
- Uses `OperationResult` messages from the service layer
- Displays joined appointment summaries via `AppointmentDetails`
- Allows doctors to update schedules directly from the UI

### `ClinicFacade`

- Validates patient and doctor existence
- Calculates available appointment slots
- Rejects past-date and unavailable-slot bookings
- Restricts cancellation and completion actions to the owning patient or doctor
- Validates doctor schedule updates
- Returns message-oriented `OperationResult` values for UI and CLI flows

This is the main place for business rules.

### Persistence Layer

- `Database` centralizes JDBC connection creation
- `DatabaseInitializer` creates the schema and indexes if they do not exist
- `DoctorRepository`, `PatientRepository`, and `AppointmentRepository` isolate SQL from the rest of the app
- `AppointmentRepository` also provides joined appointment summary queries for the UI

## Package Structure

- `org.example`
  Core application entry point, facade, repositories, and domain classes
- `org.example.ui`
  Desktop Swing UI
- `org.example.dto`
  Joined read models for display
- `org.example.service`
  Result types for application operations

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
- can validate schedule updates and joined appointment summaries

## Recommended Next Improvements

- Replace `String` status values with an enum-backed persistence strategy
- Add patient and doctor search instead of ID-only login
- Support deleting or archiving doctors and patients safely
- Add export/import or backup tools for `clinic.db`
