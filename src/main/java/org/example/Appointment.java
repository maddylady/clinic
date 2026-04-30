package org.example;

import java.time.LocalDateTime;

public class Appointment {
    private int id;
    private int doctorId;
    private int patientId;
    private LocalDateTime dateTime;
    private AppointmentState state;

    // Constructor for loading an appointment from the database
    public Appointment(int id, int doctorId, int patientId, LocalDateTime dateTime, String status) {
        this.id = id;
        this.doctorId = doctorId;
        this.patientId = patientId;
        this.dateTime = dateTime;
        this.state = statusToState(status);
    }

    // Factory method for creating a new appointment
    public static Appointment createNew(int doctorId, int patientId, LocalDateTime dateTime) {
        Appointment a = new Appointment(0, doctorId, patientId, dateTime, "NEW");
        a.state = new NewState();
        return a;
    }

    // Converts status string from the database into a State object
    private AppointmentState statusToState(String status) {
        switch (status) {
            case "NEW":
                return new NewState();
            case "CONFIRMED":
                return new ConfirmedState();
            case "CANCELLED":
                return new CancelledState();
            case "COMPLETED":
                return new CompletedState();
            default:
                throw new IllegalArgumentException("Unknown status: " + status);
        }
    }

    public void setState(AppointmentState state) {
        this.state = state;
    }

    public String getStatus() {
        return state.getName();
    }

    public void confirm() {
        state.confirm(this);
    }
    public void cancel() {
        state.cancel(this);
    }
    public void complete() {
        state.complete(this);
    }

    public int getId() {
        return id;
    }
    public int getDoctorId() {
        return doctorId;
    }
    public int getPatientId() {
        return patientId;
    }
    public LocalDateTime getDateTime() {
        return dateTime;
    }

    @Override
    public String toString() {
        return "Appointment ID=" + id +
                ", doctor=" + doctorId +
                ", patient=" + patientId +
                ", time=" + dateTime +
                ", status=" + getStatus();
    }
}