package org.example;

import java.time.LocalTime;

public class TherapistDoctor extends Doctor {

    public TherapistDoctor(int id,
                           String name,
                           LocalTime workStart,
                           LocalTime workEnd,
                           int slotMinutes) {
        super(id, name, "therapist", workStart, workEnd, slotMinutes);
    }

    @Override
    public void showInfo() {
        System.out.println("Therapist: " + name);
    }
}