package org.example;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AppointmentRepository {

    public boolean book(int doctorId, int patientId, String dateTime) {
        String sql = "INSERT INTO appointments " +
                "(doctor_id, patient_id, date_time, status) " +
                "VALUES (?, ?, ?, ?)";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, doctorId);
            ps.setInt(2, patientId);
            ps.setString(3, dateTime);
            ps.setString(4, "CONFIRMED");

            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Appointment> getForPatient(int patientId) {
        List<Appointment> list = new ArrayList<>();
        String sql = "SELECT * FROM appointments WHERE patient_id = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, patientId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                int id     = rs.getInt("id");
                int docId  = rs.getInt("doctor_id");
                int patId  = rs.getInt("patient_id");
                String dt  = rs.getString("date_time");
                String st  = rs.getString("status");

                LocalDateTime dateTime = LocalDateTime.parse(dt);

                Appointment a = new Appointment(id, docId, patId, dateTime, st);
                list.add(a);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    public Appointment getById(int id) {
        String sql = "SELECT * FROM appointments WHERE id = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                // 1. Read date-time value as a string from the database
                String dt = rs.getString("date_time");

                // 2. Convert the string into LocalDateTime
                LocalDateTime dateTime = LocalDateTime.parse(dt);

                // 3. Create and return Appointment object
                return new Appointment(
                        rs.getInt("id"),
                        rs.getInt("doctor_id"),
                        rs.getInt("patient_id"),
                        dateTime,                 // already converted to LocalDateTime
                        rs.getString("status")
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public void updateStatus(int id, String status) {
        String sql = "UPDATE appointments SET status = ? WHERE id = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, status);
            ps.setInt(2, id);
            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Appointment> getForDoctor(int doctorId) {
        List<Appointment> list = new ArrayList<>();

        String sql = "SELECT id, doctor_id, patient_id, date_time, status " +
                "FROM appointments WHERE doctor_id = ? ORDER BY date_time";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, doctorId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                int id     = rs.getInt("id");
                int docId  = rs.getInt("doctor_id");
                int patId  = rs.getInt("patient_id");
                String dt  = rs.getString("date_time");
                String st  = rs.getString("status");

                // Convert date-time string to LocalDateTime
                LocalDateTime dateTime = LocalDateTime.parse(dt);

                Appointment a = new Appointment(id, docId, patId, dateTime, st);
                list.add(a);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    public List<Appointment> getForDoctorOnDate(int doctorId, LocalDate date) {
        List<Appointment> result = new ArrayList<>();

        String sql = "SELECT id, doctor_id, patient_id, date_time, status " +
                "FROM appointments " +
                "WHERE doctor_id = ? AND date_time LIKE ? " +
                "ORDER BY date_time";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, doctorId);
            ps.setString(2, date.toString() + "%"); // e.g. "2025-12-15%"

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int id = rs.getInt("id");
                int docId = rs.getInt("doctor_id");
                int patId = rs.getInt("patient_id");
                String dt = rs.getString("date_time");
                String status = rs.getString("status");

                LocalDateTime dateTime = LocalDateTime.parse(dt);

                // Create Appointment object using parsed data
                Appointment a = new Appointment(id, docId, patId, dateTime, status);
                result.add(a);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }
}