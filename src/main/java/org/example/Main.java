package org.example;

import org.example.api.ClinicHttpServer;
import org.example.service.OperationResult;
import org.example.ui.ClinicDesktopApp;

import java.awt.GraphicsEnvironment;
import java.time.LocalTime;
import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;

public class Main {
    private static final Scanner scanner = new Scanner(System.in);
    public static void main(String[] args) {
        DatabaseInitializer.init();
        ClinicFacade facade = new ClinicFacade();
        seedDoctorsIfNeeded(facade);

        if (isApiMode(args)) {
            int port = parsePort(args);
            ClinicHttpServer server = new ClinicHttpServer(facade, port);
            server.start();
            System.out.println("Clinic HTTP API started on http://localhost:" + server.getPort());
            return;
        }

        if (!GraphicsEnvironment.isHeadless()) {
            ClinicDesktopApp.startApplication(facade);
            return;
        }

        mainMenu(facade);
    }

    private static void mainMenu(ClinicFacade facade) {
        while (true) {
            System.out.println("\n=== Clinic Appointment System ===");
            System.out.println("1. Login as patient");
            System.out.println("2. Login as doctor");
            System.out.println("0. Exit");
            System.out.print("Choose an option: ");

            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    patientMenu(facade);
                    break;
                case "2":
                    doctorMenu(facade);
                    break;
                case "0":
                    System.out.println("Goodbye!");
                    return;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    // PATIENT MENU

    private static void patientMenu(ClinicFacade facade) {
        System.out.println("\n=== Patient Login ===");
        int patientId;

        while (true) {
            System.out.print("If you are already registered, enter your patientId; otherwise enter 0: ");
            patientId = readInt();

            if (patientId == 0) {
                System.out.print("Enter your name: ");
                String name = scanner.nextLine();
                System.out.print("Enter your phone number: ");
                String phone = scanner.nextLine();
                OperationResult<Integer> result = facade.registerPatientResult(name, phone);
                if (!result.isSuccess()) {
                    System.out.println(result.getMessage());
                    continue;
                }
                patientId = result.getPayload();
                System.out.println(result.getMessage() + " Your patientId = " + patientId);
                break;
            }

            if (facade.patientExists(patientId)) {
                break;
            }

            System.out.println("No patient found with this ID. Try again or enter 0 to register.");
        }

        while (true) {
            System.out.println("\n=== Patient Menu ===");
            System.out.println("1. Book an appointment");
            System.out.println("2. View my appointments");
            System.out.println("3. Cancel an appointment");
            System.out.println("0. Back");
            System.out.print("Your choice: ");

            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    bookAppointmentAsPatient(facade, patientId);
                    break;
                case "2":
                    showAppointmentsForPatient(facade, patientId);
                    break;
                case "3":
                    cancelAppointmentAsPatient(facade, patientId);
                    break;
                case "0":
                    return;
                default:
                    System.out.println("Invalid choice.");
            }
        }
    }

    private static void bookAppointmentAsPatient(ClinicFacade facade, int patientId) {
        System.out.println("=== Appointment Booking ===");

        // Choose specialization and doctor
        List<String> specs = facade.getAllSpecializations();
        if (specs.isEmpty()) {
            System.out.println("There are no doctors in the system.");
            return;
        }

        System.out.println("Available specializations:");
        for (int i = 0; i < specs.size(); i++) {
            System.out.println((i + 1) + ". " + specs.get(i));
        }

        System.out.print("Choose a specialization: ");
        int specChoice = readInt();
        if (specChoice < 1 || specChoice > specs.size()) {
            System.out.println("Invalid selection.");
            return;
        }
        String chosenSpec = specs.get(specChoice - 1);

        List<Doctor> doctors = facade.getDoctorsBySpecialization(chosenSpec);
        if (doctors.isEmpty()) {
            System.out.println("No doctors available for this specialization.");
            return;
        }

        System.out.println("Available doctors:");
        for (int i = 0; i < doctors.size(); i++) {
            System.out.println((i + 1) + ". " + doctors.get(i));
        }

        System.out.print("Choose a doctor: ");
        int docChoice = readInt();
        if (docChoice < 1 || docChoice > doctors.size()) {
            System.out.println("Invalid selection.");
            return;
        }

        Doctor doctor = doctors.get(docChoice - 1);
        int doctorId = doctor.getId();

        // Enter the date
        System.out.print("Enter the appointment date (format: 2025-12-15): ");
        String dateStr = scanner.nextLine().trim();
        LocalDate date;
        try {
            date = LocalDate.parse(dateStr);
        } catch (Exception e) {
            System.out.println("Invalid date format.");
            return;
        }

        if (date.isBefore(LocalDate.now())) {
            System.out.println("Appointments cannot be booked in the past.");
            return;
        }

        // Get available time slots
        List<LocalTime> slots = facade.getAvailableSlots(doctorId, date);
        if (slots.isEmpty()) {
            System.out.println("No available slots on this day (weekend or fully booked).");
            return;
        }

        System.out.println("Available slots:");
        for (int i = 0; i < slots.size(); i++) {
            System.out.println((i + 1) + ". " + slots.get(i));
        }

        System.out.print("Choose a slot: ");
        int slotChoice = readInt();
        if (slotChoice < 1 || slotChoice > slots.size()) {
            System.out.println("Invalid slot selection.");
            return;
        }

        LocalTime chosenTime = slots.get(slotChoice - 1);

        // Attempt to book
        OperationResult<Void> result = facade.bookAppointmentInSlotResult(patientId, doctorId, date, chosenTime);
        System.out.println(result.getMessage());
    }

    private static void showAppointmentsForPatient(ClinicFacade facade, int patientId) {
        System.out.println("\n=== My Appointments ===");
        List<Appointment> list = facade.getAppointmentsForPatient(patientId);
        if (list.isEmpty()) {
            System.out.println("You have no appointments.");
            return;
        }
        for (Appointment a : list) {
            System.out.println(a);
        }
    }

    private static void cancelAppointmentAsPatient(ClinicFacade facade, int patientId) {
        System.out.println("\n=== Cancel Appointment ===");
        List<Appointment> list = facade.getAppointmentsForPatient(patientId);
        if (list.isEmpty()) {
            System.out.println("You have no appointments.");
            return;
        }

        System.out.println("Your appointments:");
        for (Appointment a : list) {
            System.out.println(a.getId() + ": " + a);
        }

        System.out.print("Enter the appointment ID you want to cancel: ");
        int appId = readInt();

        boolean cancelled = facade.cancelAppointmentForPatientResult(appId, patientId).isSuccess();
        if (cancelled) {
            System.out.println("If the current status allowed it, the appointment is now CANCELLED.");
        } else {
            System.out.println("Appointment not found for your account.");
        }
    }

    // DOCTOR MENU
    private static void doctorMenu(ClinicFacade facade) {
        System.out.println("\n=== Doctor Login ===");
        int doctorId;

        while (true) {
            System.out.print("Enter your doctorId (or 0 to register): ");
            doctorId = readInt();

            if (doctorId == 0) {
                System.out.print("Enter your name: ");
                String name = scanner.nextLine();
                System.out.print("Enter specialization (e.g., therapist or surgeon): ");
                String specialization = scanner.nextLine().trim();
                OperationResult<Integer> result = facade.addDoctorResult(name, specialization);
                if (!result.isSuccess()) {
                    System.out.println(result.getMessage());
                    continue;
                }
                doctorId = result.getPayload();

                if (doctorId > 0) {
                    System.out.println("Doctor registered successfully. Your doctorId = " + doctorId);
                    break;
                } else {
                    System.out.println("Failed to register doctor. Please try again.");
                    continue;
                }
            }

            if (facade.doctorExists(doctorId)) {
                break;
            }

            System.out.println("No doctor found with this ID. Try again or enter 0 to register.");
        }

        while (true) {
            System.out.println("\n=== Doctor Menu ===");
            System.out.println("1. View my appointments");
            System.out.println("2. Mark an appointment as completed");
            System.out.println("0. Back");
            System.out.print("Your choice: ");

            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    showAppointmentsForDoctor(facade, doctorId);
                    break;
                case "2":
                    completeAppointmentAsDoctor(facade, doctorId);
                    break;
                case "0":
                    return;
                default:
                    System.out.println("Invalid choice.");
            }
        }
    }

    private static void showAppointmentsForDoctor(ClinicFacade facade, int doctorId) {
        System.out.println("\n=== Doctor Appointments ===");
        List<Appointment> list = facade.getAppointmentsForDoctor(doctorId);
        if (list.isEmpty()) {
            System.out.println("You have no appointments yet.");
            return;
        }
        for (Appointment a : list) {
            System.out.println(a);
        }
    }

    private static void completeAppointmentAsDoctor(ClinicFacade facade, int doctorId) {
        System.out.println("\n=== Complete Appointment ===");
        List<Appointment> list = facade.getAppointmentsForDoctor(doctorId);
        if (list.isEmpty()) {
            System.out.println("You have no appointments.");
            return;
        }

        System.out.println("Your appointments:");
        for (Appointment a : list) {
            System.out.println(a.getId() + ": " + a);
        }

        System.out.print("Enter the appointment ID you want to complete: ");
        int appId = readInt();

        boolean completed = facade.completeAppointmentForDoctorResult(appId, doctorId).isSuccess();
        if (completed) {
            System.out.println("If the status was CONFIRMED, the appointment is now COMPLETED.");
        } else {
            System.out.println("Appointment not found for your account.");
        }
    }

    private static int readInt() {
        while (true) {
            String line = scanner.nextLine();
            try {
                return Integer.parseInt(line.trim());
            } catch (NumberFormatException e) {
                System.out.print("Enter a number: ");
            }
        }
    }

    private static boolean isApiMode(String[] args) {
        for (String arg : args) {
            if ("--api".equals(arg)) {
                return true;
            }
        }
        return false;
    }

    private static int parsePort(String[] args) {
        for (int i = 0; i < args.length - 1; i++) {
            if ("--port".equals(args[i])) {
                try {
                    return Integer.parseInt(args[i + 1]);
                } catch (NumberFormatException ignored) {
                    return 8080;
                }
            }
        }
        return 8080;
    }

    // One-time seeding of doctors
    private static void seedDoctorsIfNeeded(ClinicFacade facade) {
        // Check: if there is at least one specialization, doctors already exist
        List<String> specs = facade.getAllSpecializations();
        if (!specs.isEmpty()) {
            return; // doctors already exist
        }
        facade.addDoctor("Dr. Alice", "therapist");
        facade.addDoctor("Dr. Bob", "surgeon");
        facade.addDoctor("Dr. John", "therapist");
        facade.addDoctor("Dr. Emma", "surgeon");
        facade.addDoctor("Dr. David", "therapist");
        facade.addDoctor("Dr. Smith", "cardiologist");
        facade.addDoctor("Dr. Anna", "cardiologist");
    }
}
