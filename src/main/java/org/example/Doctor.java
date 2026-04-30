package org.example;

import java.time.LocalTime;

public abstract class Doctor {
    protected int id;
    protected String name;
    protected String specialization;

    // schedule
    protected LocalTime workStart;
    protected LocalTime workEnd;
    protected int slotMinutes;

    public Doctor(int id,
                  String name,
                  String specialization,
                  LocalTime workStart,
                  LocalTime workEnd,
                  int slotMinutes) {
        this.id = id;
        this.name = name;
        this.specialization = specialization;
        this.workStart = workStart;
        this.workEnd = workEnd;
        this.slotMinutes = slotMinutes;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getSpecialization() {
        return specialization;
    }

    public LocalTime getWorkStart() {
        return workStart;
    }

    public LocalTime getWorkEnd() {
        return workEnd;
    }

    public int getSlotMinutes() {
        return slotMinutes;
    }

    public abstract void showInfo();

    @Override
    public String toString() {
        return name + " (" + specialization + ")";
    }
}