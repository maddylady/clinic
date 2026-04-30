package org.example;

import java.time.LocalTime;

public class CardiologistDoctor extends Doctor {

    public CardiologistDoctor(int id, String name,
                              LocalTime workStart, LocalTime workEnd,
                              int slotMinutes) {
        super(id, name, "cardiologist", workStart, workEnd, slotMinutes);
    }

    @Override
    public void showInfo() {
        System.out.println("Cardiologist: " + name);
    }
}