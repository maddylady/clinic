package org.example;

import java.time.LocalTime;

public class SurgeonDoctor extends Doctor {

    public SurgeonDoctor(int id,
                         String name,
                         LocalTime workStart,
                         LocalTime workEnd,
                         int slotMinutes) {
        super(id, name, "surgeon", workStart, workEnd, slotMinutes);
    }

    @Override
    public void showInfo() {
        System.out.println("Surgeon: " + name);
    }
}