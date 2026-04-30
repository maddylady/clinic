package org.example.dto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AppointmentDetails {
    private static final DateTimeFormatter DISPLAY_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final int appointmentId;
    private final int doctorId;
    private final String doctorName;
    private final String doctorSpecialization;
    private final int patientId;
    private final String patientName;
    private final LocalDateTime dateTime;
    private final String status;

    public AppointmentDetails(int appointmentId,
                              int doctorId,
                              String doctorName,
                              String doctorSpecialization,
                              int patientId,
                              String patientName,
                              LocalDateTime dateTime,
                              String status) {
        this.appointmentId = appointmentId;
        this.doctorId = doctorId;
        this.doctorName = doctorName;
        this.doctorSpecialization = doctorSpecialization;
        this.patientId = patientId;
        this.patientName = patientName;
        this.dateTime = dateTime;
        this.status = status;
    }

    public int getAppointmentId() {
        return appointmentId;
    }

    public int getDoctorId() {
        return doctorId;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public String getDoctorSpecialization() {
        return doctorSpecialization;
    }

    public int getPatientId() {
        return patientId;
    }

    public String getPatientName() {
        return patientName;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public String getStatus() {
        return status;
    }

    @Override
    public String toString() {
        return "#" + appointmentId
                + "  " + dateTime.format(DISPLAY_FORMAT)
                + "  Dr. " + doctorName
                + " (" + doctorSpecialization + ")"
                + "  Patient: " + patientName
                + "  [" + status + "]";
    }
}
