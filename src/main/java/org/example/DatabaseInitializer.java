package org.example;

import java.sql.Connection;
import java.sql.Statement;

public class DatabaseInitializer {

    private DatabaseInitializer() {
    }

    public static void init() {
        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute("CREATE TABLE IF NOT EXISTS doctors ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "name TEXT NOT NULL, "
                    + "specialization TEXT NOT NULL, "
                    + "work_start TEXT NOT NULL, "
                    + "work_end TEXT NOT NULL, "
                    + "slot_minutes INTEGER NOT NULL"
                    + ")");

            stmt.execute("CREATE TABLE IF NOT EXISTS patients ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "name TEXT NOT NULL, "
                    + "phone TEXT NOT NULL"
                    + ")");

            stmt.execute("CREATE TABLE IF NOT EXISTS appointments ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "doctor_id INTEGER NOT NULL, "
                    + "patient_id INTEGER NOT NULL, "
                    + "date_time TEXT NOT NULL, "
                    + "status TEXT NOT NULL DEFAULT 'CONFIRMED', "
                    + "FOREIGN KEY (doctor_id) REFERENCES doctors(id), "
                    + "FOREIGN KEY (patient_id) REFERENCES patients(id)"
                    + ")");

            stmt.execute("CREATE UNIQUE INDEX IF NOT EXISTS idx_appointments_doctor_slot "
                    + "ON appointments(doctor_id, date_time)");

            stmt.execute("CREATE INDEX IF NOT EXISTS idx_appointments_patient "
                    + "ON appointments(patient_id)");

            stmt.execute("CREATE INDEX IF NOT EXISTS idx_appointments_doctor "
                    + "ON appointments(doctor_id)");

            System.out.println("Database schema is ready.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
