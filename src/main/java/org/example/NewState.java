package org.example;

public class NewState implements AppointmentState {

    @Override
    public void confirm(Appointment appointment) {
        appointment.setState(new ConfirmedState());
    }

    @Override
    public void cancel(Appointment appointment) {
        appointment.setState(new CancelledState());
    }

    @Override
    public void complete(Appointment appointment) {
        System.out.println("Cannot complete an appointment that has not been confirmed yet.");
    }

    @Override
    public String getName() {
        return "NEW";
    }
}