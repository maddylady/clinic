# Clinic Appointment System

A Java 11 clinic management application with a Swing desktop interface, CLI fallback mode, embedded HTTP API, and SQLite persistence.

## Features

- Patient registration and login by generated `patientId`
- Doctor registration and login by generated `doctorId`
- Desktop dashboard for booking, reviewing, cancelling, and completing appointments
- Embedded HTTP API for clinic data access and scheduling operations
- Doctor schedule editing for work hours and slot length
- Readable appointment views with doctor and patient names
- Appointment booking by specialization, doctor, date, and available time slot
- Appointment state management with `CONFIRMED`, `CANCELLED`, and `COMPLETED`
- SQLite-backed persistence that now survives application restarts
- Protection against double-booking the same doctor and time slot
- Ownership checks so patients can only cancel their own appointments and doctors can only complete their own appointments

## Tech Stack

- Java 11
- Maven
- SQLite with `sqlite-jdbc`
- JUnit 5

## Project Structure

```text
src/main/java/org/example
├── Main.java                         Desktop entry point with console fallback
├── ClinicFacade.java                 Application facade
├── Database.java                     SQLite connection management
├── DatabaseInitializer.java          Schema creation
├── Appointment*.java                 Appointment domain and state pattern
├── Doctor*.java                      Doctor hierarchy and factory
├── Patient*.java                     Patient entity and repository
├── *Repository.java                  Persistence layer
├── api/ClinicHttpServer.java         Embedded HTTP API layer
├── dto/AppointmentDetails.java       Readable UI-facing appointment summary
├── service/OperationResult.java      Service result wrapper with messages
└── ui/ClinicDesktopApp.java          Swing user interface
```

## Design Patterns Used

- `Facade`: `ClinicFacade` provides a single application-level API for the CLI.
- `Factory Method`: `DoctorFactory` maps database rows to the correct doctor subtype.
- `State`: `Appointment` delegates status transitions to `AppointmentState` implementations.

## Getting Started

### Prerequisites

- JDK 11+
- Maven 3.9+

### Run the application

```bash
mvn compile
mvn exec:java
```

On a normal local machine, this opens the desktop UI automatically. In a headless environment, the application falls back to the console workflow.

The application creates `clinic.db` in the project root by default.

### Run the HTTP API

```bash
mvn exec:java -Dexec.args="--api --port 8080"
```

Example endpoints:

- `GET /api/health`
- `GET /api/doctors`
- `POST /api/doctors?name=Dr.%20Miles&specialization=therapist`
- `POST /api/patients?name=Alice&phone=%2B7-777-100-2000`
- `GET /api/doctors/1/slots?date=2026-05-04`
- `POST /api/appointments/book?patientId=1&doctorId=1&date=2026-05-04&time=09:00`
- `GET /api/patients/1/appointments`

### Run tests

```bash
mvn test
```

### Build a runnable JAR

```bash
mvn package
java -jar target/ClinicProject-1.0-SNAPSHOT.jar
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
- The same application can also run as an embedded HTTP API server.
- Doctors can update their working hours and slot duration from the UI.
- Patient and doctor appointment lists now show joined human-readable details.

## Documentation

- [Architecture Notes](docs/ARCHITECTURE.md)

## Known Limitations

- Specialization values are still string-based instead of enum-backed.
- The desktop UI is local desktop software, not a web or networked multi-user system.
- The current desktop UI is intentionally local-only and does not provide multi-user concurrency feedback beyond booking failure messages.
