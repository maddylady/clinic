# Clinic Appointment System

A Java 22 clinic management application with a desktop Swing interface, a console fallback mode, and SQLite persistence.

## Features

- Patient registration and login by generated `patientId`
- Doctor registration and login by generated `doctorId`
- Desktop dashboard for booking, reviewing, cancelling, and completing appointments
- Appointment booking by specialization, doctor, date, and available time slot
- Appointment state management with `CONFIRMED`, `CANCELLED`, and `COMPLETED`
- SQLite-backed persistence that now survives application restarts
- Protection against double-booking the same doctor and time slot
- Ownership checks so patients can only cancel their own appointments and doctors can only complete their own appointments

## Tech Stack

- Java 22
- Maven
- SQLite with `sqlite-jdbc`
- JUnit 5

## Project Structure

```text
src/main/java/org/example
├── Main.java                  Desktop entry point with console fallback
├── ClinicDesktopApp.java      Swing user interface
├── ClinicFacade.java          Application facade
├── Database.java              SQLite connection management
├── DatabaseInitializer.java   Schema creation
├── Appointment*.java          Appointment domain and state pattern
├── Doctor*.java               Doctor hierarchy and factory
├── Patient*.java              Patient entity and repository
└── *Repository.java           Persistence layer
```

## Design Patterns Used

- `Facade`: `ClinicFacade` provides a single application-level API for the CLI.
- `Factory Method`: `DoctorFactory` maps database rows to the correct doctor subtype.
- `State`: `Appointment` delegates status transitions to `AppointmentState` implementations.

## Getting Started

### Prerequisites

- JDK 22
- Maven 3.9+

### Run the application

```bash
mvn compile
mvn exec:java
```

On a normal local machine, this opens the desktop UI automatically. In a headless environment, the application falls back to the console workflow.

The application creates `clinic.db` in the project root by default.

### Run tests

```bash
mvn test
```

## Database Configuration

The app supports overriding the SQLite target during runtime:

- `-Dclinic.db.path=/absolute/or/relative/path/to/file.db`
- `-Dclinic.db.url=jdbc:sqlite:/absolute/path/to/file.db`

`clinic.db.url` takes precedence over `clinic.db.path`.

This is mainly useful for tests and isolated local runs.

## Current Behavior

- Doctors are seeded automatically on the first launch if the database does not already contain any doctors.
- Weekend bookings are rejected.
- Past-date bookings are rejected.
- Restarting the app no longer clears existing records.
- The default run path opens a desktop Swing dashboard for patients and doctors.

## Documentation

- [Architecture Notes](docs/ARCHITECTURE.md)

## Known Limitations

- The desktop UI does not yet support editing doctor schedules.
- Appointment listings show IDs rather than joined human-readable patient or doctor names.
- The current desktop UI is intentionally local-only and does not provide multi-user concurrency feedback beyond booking failure messages.
