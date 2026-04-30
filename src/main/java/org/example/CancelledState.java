package org.example;

public class CancelledState implements AppointmentState {

    @Override
    public void confirm(Appointment appointment) {
        System.out.println("Cannot confirm a cancelled appointment.");
    }

    @Override
    public void cancel(Appointment appointment) {
        // Already cancelled
    }
    @Override
    public void complete(Appointment appointment) {
        System.out.println("Cannot complete a cancelled appointment.");
    }

    @Override
    public String getName() {
        return "CANCELLED";
    }
}