package org.example;

public class CompletedState implements AppointmentState {

    @Override
    public void confirm(Appointment appointment) {
        System.out.println("The appointment is already completed — confirmation is not possible.");
    }

    @Override
    public void cancel(Appointment appointment) {
        System.out.println("A completed appointment cannot be cancelled.");
    }

    @Override
    public void complete(Appointment appointment) {
        // Already completed
    }

    @Override
    public String getName() {
        return "COMPLETED";
    }
}