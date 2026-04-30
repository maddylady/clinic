package org.example;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalTime;

public class DoctorFactory {
    // Factory Method: creates the appropriate Doctor subclass
    // based on the specialization value retrieved from the database
    public static Doctor fromResultSet(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String name = rs.getString("name");
        String specialization = rs.getString("specialization");

        String workStartStr = rs.getString("work_start");
        String workEndStr   = rs.getString("work_end");
        int slotMinutes     = rs.getInt("slot_minutes");

        LocalTime workStart = LocalTime.parse(workStartStr);
        LocalTime workEnd   = LocalTime.parse(workEndStr);

        if (specialization == null) {
            throw new IllegalArgumentException(
                    "Specialization is null for doctor id=" + id
            );
        }

        switch (specialization.toLowerCase()) {
            case "therapist":
                return new TherapistDoctor(id, name, workStart, workEnd, slotMinutes);
            case "surgeon":
                return new SurgeonDoctor(id, name, workStart, workEnd, slotMinutes);
            case "cardiologist":
                return new CardiologistDoctor(id, name, workStart, workEnd, slotMinutes);
            default:
                throw new IllegalArgumentException(
                        "Unknown specialization: " + specialization
                );
        }
    }
}