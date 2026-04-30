package org.example;

public interface AppointmentState {
    void confirm(Appointment appointment);
    void cancel(Appointment appointment);
    void complete(Appointment appointment);
    String getName();
}