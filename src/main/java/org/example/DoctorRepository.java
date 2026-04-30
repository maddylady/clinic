package org.example;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DoctorRepository {

    public int addDoctor(String name, String specialization) {
        String sql = "INSERT INTO doctors(name, specialization, work_start, work_end, slot_minutes) " +
                "VALUES(?, ?, ?, ?, ?)";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, name);
            ps.setString(2, specialization);
            ps.setString(3, "09:00"); // work start time
            ps.setString(4, "17:00"); // work end time
            ps.setInt(5, 30);         // slot duration in minutes

            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) {
                return keys.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return -1;
    }

    public List<Doctor> findBySpecialization(String specialization) {
        List<Doctor> doctors = new ArrayList<>();
        String sql = "SELECT * FROM doctors WHERE specialization = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, specialization);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                doctors.add(DoctorFactory.fromResultSet(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return doctors;
    }

    public List<String> getAllSpecializations() {
        List<String> list = new ArrayList<>();
        String sql = "SELECT DISTINCT specialization FROM doctors";

        try (Connection conn = Database.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                list.add(rs.getString("specialization"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    public Doctor getById(int id) {
        String sql = "SELECT * FROM doctors WHERE id = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return DoctorFactory.fromResultSet(rs);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public boolean exists(int id) {
        String sql = "SELECT 1 FROM doctors WHERE id = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            return rs.next();

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}