package org.example;

import org.example.dto.AppointmentDetails;
import org.example.service.OperationResult;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class ClinicFacade {

    private final DoctorRepository doctorRepo = new DoctorRepository();
    private final PatientRepository patientRepo = new PatientRepository();
    private final AppointmentRepository appointmentRepo = new AppointmentRepository();

    // Register a new patient
    public int registerPatient(String name, String phone) {
        return patientRepo.addPatient(name, phone);
    }

    // Get all doctors by specialization
    public List<Doctor> getDoctorsBySpecialization(String specialization) {
        return doctorRepo.findBySpecialization(specialization);
    }

    public List<Doctor> getAllDoctors() {
        return doctorRepo.getAllDoctors();
    }

    // Get all available specializations
    public List<String> getAllSpecializations() {
        return doctorRepo.getAllSpecializations();
    }

    public boolean patientExists(int patientId) {
        return patientRepo.exists(patientId);
    }

    public Patient getPatientById(int patientId) {
        return patientRepo.getById(patientId);
    }

    // Book an appointment for a patient with a doctor
    public boolean bookAppointment(int patientId, int doctorId, String dateTime) {
        return appointmentRepo.book(doctorId, patientId, dateTime);
    }

    // Get all appointments for a patient
    public List<Appointment> getAppointmentsForPatient(int patientId) {
        return appointmentRepo.getForPatient(patientId);
    }

    public List<AppointmentDetails> getAppointmentDetailsForPatient(int patientId) {
        return appointmentRepo.getDetailsForPatient(patientId);
    }

    // Get all appointments for a doctor
    public List<Appointment> getAppointmentsForDoctor(int doctorId) {
        return appointmentRepo.getForDoctor(doctorId);
    }

    public List<AppointmentDetails> getAppointmentDetailsForDoctor(int doctorId) {
        return appointmentRepo.getDetailsForDoctor(doctorId);
    }

    public boolean cancelAppointment(int appointmentId) {
        Appointment a = appointmentRepo.getById(appointmentId);
        if (a == null) {
            return false;
        }

        a.cancel(); // State pattern
        appointmentRepo.updateStatus(appointmentId, a.getStatus());
        return true;
    }

    public boolean cancelAppointmentForPatient(int appointmentId, int patientId) {
        return cancelAppointmentForPatientResult(appointmentId, patientId).isSuccess();
    }

    public boolean completeAppointment(int appointmentId) {
        Appointment a = appointmentRepo.getById(appointmentId);
        if (a == null) {
            return false;
        }

        a.complete(); // State pattern
        appointmentRepo.updateStatus(appointmentId, a.getStatus());
        return true;
    }

    public boolean completeAppointmentForDoctor(int appointmentId, int doctorId) {
        return completeAppointmentForDoctorResult(appointmentId, doctorId).isSuccess();
    }

    public boolean doctorExists(int doctorId) {
        return doctorRepo.exists(doctorId);
    }

    public Doctor getDoctorById(int doctorId) {
        return doctorRepo.getById(doctorId);
    }

    public int addDoctor(String name, String specialization) {
        return doctorRepo.addDoctor(name, specialization);
    }

    public OperationResult<Integer> registerPatientResult(String name, String phone) {
        if (name == null || name.isBlank()) {
            return OperationResult.failure("Patient name is required.");
        }
        if (phone == null || phone.isBlank()) {
            return OperationResult.failure("Patient phone is required.");
        }

        int patientId = patientRepo.addPatient(name.trim(), phone.trim());
        if (patientId < 0) {
            return OperationResult.failure("Patient registration failed.");
        }
        return OperationResult.success("Patient registered successfully.", patientId);
    }

    public OperationResult<Integer> addDoctorResult(String name, String specialization) {
        if (name == null || name.isBlank()) {
            return OperationResult.failure("Doctor name is required.");
        }
        if (specialization == null || specialization.isBlank()) {
            return OperationResult.failure("Doctor specialization is required.");
        }

        int doctorId = doctorRepo.addDoctor(name.trim(), specialization.trim().toLowerCase());
        if (doctorId < 0) {
            return OperationResult.failure("Doctor registration failed.");
        }
        return OperationResult.success("Doctor registered successfully.", doctorId);
    }

    public List<LocalTime> getAvailableSlots(int doctorId, LocalDate date) {

        // If it is a weekend, return an empty list
        DayOfWeek day = date.getDayOfWeek();
        if (day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY) {
            return new ArrayList<>();
        }

        // Get the doctor to access working schedule
        Doctor doctor = doctorRepo.getById(doctorId);
        if (doctor == null) {
            return new ArrayList<>();
        }

        LocalTime workStart = doctor.getWorkStart();
        LocalTime workEnd = doctor.getWorkEnd();
        int slotMinutes = doctor.getSlotMinutes();

        // Get already booked appointments for the selected date
        List<Appointment> booked = appointmentRepo.getForDoctorOnDate(doctorId, date);

        List<LocalTime> bookedTimes = new ArrayList<>();
        for (Appointment a : booked) {
            // Free the slot if the appointment is cancelled or completed
            String status = a.getStatus();
            if ("CANCELLED".equals(status) || "COMPLETED".equals(status)) {
                continue;
            }
            bookedTimes.add(a.getDateTime().toLocalTime());
        }

        // Generate available working slots
        List<LocalTime> available = new ArrayList<>();

        LocalTime time = workStart;

        while (time.isBefore(workEnd)) {
            // If the slot is not occupied, add it
            if (!bookedTimes.contains(time)) {
                available.add(time);
            }

            time = time.plusMinutes(slotMinutes); // move to the next slot
        }

        return available;
    }

    public boolean bookAppointmentInSlot(int patientId,
                                         int doctorId,
                                         LocalDate date,
                                         LocalTime time) {
        return bookAppointmentInSlotResult(patientId, doctorId, date, time).isSuccess();
    }

    public OperationResult<Void> bookAppointmentInSlotResult(int patientId,
                                                             int doctorId,
                                                             LocalDate date,
                                                             LocalTime time) {
        if (!patientRepo.exists(patientId) || !doctorRepo.exists(doctorId)) {
            return OperationResult.failure("Patient or doctor was not found.");
        }

        if (date.isBefore(LocalDate.now())) {
            return OperationResult.failure("Appointments cannot be booked in the past.");
        }

        if (!getAvailableSlots(doctorId, date).contains(time)) {
            return OperationResult.failure("That slot is no longer available.");
        }

        LocalDateTime dateTime = LocalDateTime.of(date, time);
        boolean booked = appointmentRepo.book(doctorId, patientId, dateTime.toString());
        if (!booked) {
            return OperationResult.failure("Booking failed because the slot is already taken.");
        }
        return OperationResult.success("Appointment booked successfully.");
    }

    public OperationResult<Void> cancelAppointmentForPatientResult(int appointmentId, int patientId) {
        Appointment appointment = appointmentRepo.getByIdForPatient(appointmentId, patientId);
        if (appointment == null) {
            return OperationResult.failure("Appointment not found for this patient.");
        }

        String oldStatus = appointment.getStatus();
        appointment.cancel();
        if (oldStatus.equals(appointment.getStatus())) {
            return OperationResult.failure("This appointment cannot be cancelled from its current state.");
        }

        appointmentRepo.updateStatus(appointmentId, appointment.getStatus());
        return OperationResult.success("Appointment cancelled.");
    }

    public OperationResult<Void> completeAppointmentForDoctorResult(int appointmentId, int doctorId) {
        Appointment appointment = appointmentRepo.getByIdForDoctor(appointmentId, doctorId);
        if (appointment == null) {
            return OperationResult.failure("Appointment not found for this doctor.");
        }

        String oldStatus = appointment.getStatus();
        appointment.complete();
        if (oldStatus.equals(appointment.getStatus())) {
            return OperationResult.failure("Only confirmed appointments can be completed.");
        }

        appointmentRepo.updateStatus(appointmentId, appointment.getStatus());
        return OperationResult.success("Appointment marked as completed.");
    }

    public OperationResult<Void> updateDoctorSchedule(int doctorId,
                                                      LocalTime workStart,
                                                      LocalTime workEnd,
                                                      int slotMinutes) {
        if (!doctorRepo.exists(doctorId)) {
            return OperationResult.failure("Doctor not found.");
        }
        if (workStart == null || workEnd == null) {
            return OperationResult.failure("Both start and end times are required.");
        }
        if (!workStart.isBefore(workEnd)) {
            return OperationResult.failure("Work start must be earlier than work end.");
        }
        if (slotMinutes < 10 || slotMinutes > 180) {
            return OperationResult.failure("Slot duration must be between 10 and 180 minutes.");
        }

        long minutes = java.time.Duration.between(workStart, workEnd).toMinutes();
        if (minutes < slotMinutes) {
            return OperationResult.failure("Working hours must be longer than one appointment slot.");
        }

        boolean updated = doctorRepo.updateSchedule(doctorId, workStart.toString(), workEnd.toString(), slotMinutes);
        if (!updated) {
            return OperationResult.failure("Doctor schedule update failed.");
        }
        return OperationResult.success("Doctor schedule updated.");
    }
}
