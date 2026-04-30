package org.example;

import java.sql.Connection;
import java.sql.Statement;

public class DatabaseInitializer {

    public static void init() {
        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement()) {

            // Drop all tables first
            stmt.execute("DROP TABLE IF EXISTS appointments");
            stmt.execute("DROP TABLE IF EXISTS patients");
            stmt.execute("DROP TABLE IF EXISTS doctors");

            // DOCTORS
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS doctors (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL,
                    specialization TEXT NOT NULL,
                    work_start TEXT NOT NULL,
                    work_end   TEXT NOT NULL,
                    slot_minutes INTEGER NOT NULL
                );
                """);

            // PATIENTS
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS patients (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name  TEXT NOT NULL,
                    phone TEXT NOT NULL
                );
                """);

            // APPOINTMENTS
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS appointments (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    doctor_id  INTEGER NOT NULL,
                    patient_id INTEGER NOT NULL,
                    date_time  TEXT NOT NULL,
                    status     TEXT NOT NULL DEFAULT 'SCHEDULED'
                );
                """);

            System.out.println("Database and tables have been successfully initialized!");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}