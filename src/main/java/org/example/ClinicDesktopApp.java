package org.example;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerDateModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ClinicDesktopApp {
    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final String[] SPECIALIZATIONS = {"therapist", "surgeon", "cardiologist"};

    private final ClinicFacade facade;

    private JFrame frame;

    private Integer currentPatientId;
    private JLabel patientStatusLabel;
    private JTextField patientLoginIdField;
    private JTextField patientNameField;
    private JTextField patientPhoneField;
    private JComboBox<String> specializationComboBox;
    private JComboBox<Doctor> doctorComboBox;
    private JSpinner appointmentDateSpinner;
    private JComboBox<LocalTime> slotComboBox;
    private JList<Appointment> patientAppointmentsList;

    private Integer currentDoctorId;
    private JLabel doctorStatusLabel;
    private JTextField doctorLoginIdField;
    private JTextField doctorNameField;
    private JComboBox<String> doctorSpecializationComboBox;
    private JList<Appointment> doctorAppointmentsList;

    public ClinicDesktopApp(ClinicFacade facade) {
        this.facade = facade;
    }

    public static void startApplication(ClinicFacade facade) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }

        SwingUtilities.invokeLater(() -> new ClinicDesktopApp(facade).show());
    }

    private void show() {
        frame = new JFrame("Clinic Appointment System");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setMinimumSize(new Dimension(1100, 760));
        frame.setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout(20, 20));
        root.setBorder(new EmptyBorder(20, 20, 20, 20));
        root.setBackground(new Color(245, 247, 250));

        root.add(buildHeroPanel(), BorderLayout.NORTH);
        root.add(buildTabs(), BorderLayout.CENTER);

        frame.setContentPane(root);
        frame.setVisible(true);

        refreshDoctorSelections();
    }

    private JComponent buildHeroPanel() {
        JPanel hero = new JPanel();
        hero.setLayout(new BoxLayout(hero, BoxLayout.Y_AXIS));
        hero.setBackground(new Color(22, 52, 77));
        hero.setBorder(new EmptyBorder(24, 24, 24, 24));

        JLabel title = new JLabel("Clinic Appointment Dashboard");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Serif", Font.BOLD, 32));

        JLabel subtitle = new JLabel("Register, book, review, cancel, and complete appointments from one desktop workspace.");
        subtitle.setForeground(new Color(211, 223, 234));
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 15));

        hero.add(title);
        hero.add(Box.createVerticalStrut(8));
        hero.add(subtitle);
        return hero;
    }

    private JComponent buildTabs() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("SansSerif", Font.BOLD, 15));
        tabs.addTab("Patient Workspace", buildPatientTab());
        tabs.addTab("Doctor Workspace", buildDoctorTab());
        return tabs;
    }

    private JComponent buildPatientTab() {
        JPanel panel = new JPanel(new BorderLayout(16, 16));
        panel.setOpaque(false);

        JPanel top = new JPanel(new BorderLayout(16, 16));
        top.setOpaque(false);
        top.add(buildPatientAccountPanel(), BorderLayout.WEST);
        top.add(buildBookingPanel(), BorderLayout.CENTER);

        panel.add(top, BorderLayout.NORTH);
        panel.add(buildPatientAppointmentsPanel(), BorderLayout.CENTER);
        return panel;
    }

    private JComponent buildDoctorTab() {
        JPanel panel = new JPanel(new BorderLayout(16, 16));
        panel.setOpaque(false);

        panel.add(buildDoctorAccountPanel(), BorderLayout.NORTH);
        panel.add(buildDoctorAppointmentsPanel(), BorderLayout.CENTER);
        return panel;
    }

    private JComponent buildPatientAccountPanel() {
        JPanel card = createCard("Patient Access");
        card.setPreferredSize(new Dimension(360, 260));

        patientStatusLabel = createStatusLabel("No patient selected");
        patientLoginIdField = new JTextField();
        patientNameField = new JTextField();
        patientPhoneField = new JTextField();

        JButton loginButton = new JButton("Login with ID");
        stylePrimaryButton(loginButton);
        loginButton.addActionListener(e -> loginPatient());

        JButton registerButton = new JButton("Register Patient");
        styleSecondaryButton(registerButton);
        registerButton.addActionListener(e -> registerPatient());

        card.add(labeledField("Patient ID", patientLoginIdField));
        card.add(buttonRow(loginButton));
        card.add(Box.createVerticalStrut(10));
        card.add(labeledField("Name", patientNameField));
        card.add(labeledField("Phone", patientPhoneField));
        card.add(buttonRow(registerButton));
        card.add(Box.createVerticalStrut(10));
        card.add(patientStatusLabel);

        return card;
    }

    private JComponent buildBookingPanel() {
        JPanel card = createCard("Book Appointment");

        specializationComboBox = new JComboBox<>();
        doctorComboBox = new JComboBox<>();
        slotComboBox = new JComboBox<>();
        appointmentDateSpinner = buildDateSpinner();

        specializationComboBox.addActionListener(e -> refreshDoctorSelections());

        JButton refreshSlotsButton = new JButton("Refresh Slots");
        styleSecondaryButton(refreshSlotsButton);
        refreshSlotsButton.addActionListener(e -> refreshAvailableSlots());

        JButton bookButton = new JButton("Book Selected Slot");
        stylePrimaryButton(bookButton);
        bookButton.addActionListener(e -> bookSelectedSlot());

        card.add(labeledField("Specialization", specializationComboBox));
        card.add(labeledField("Doctor", doctorComboBox));
        card.add(labeledField("Date", appointmentDateSpinner));
        card.add(buttonRow(refreshSlotsButton));
        card.add(labeledField("Available Slots", slotComboBox));
        card.add(buttonRow(bookButton));

        return card;
    }

    private JComponent buildPatientAppointmentsPanel() {
        JPanel card = createCard("Patient Appointments");
        card.setLayout(new BorderLayout(12, 12));

        patientAppointmentsList = new JList<>();
        patientAppointmentsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        patientAppointmentsList.setFont(new Font("Monospaced", Font.PLAIN, 13));

        JScrollPane scrollPane = new JScrollPane(patientAppointmentsList);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(222, 228, 235)));

        JButton refreshButton = new JButton("Refresh");
        styleSecondaryButton(refreshButton);
        refreshButton.addActionListener(e -> refreshPatientAppointments());

        JButton cancelButton = new JButton("Cancel Selected");
        stylePrimaryButton(cancelButton);
        cancelButton.addActionListener(e -> cancelSelectedAppointment());

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        actions.setOpaque(false);
        actions.add(refreshButton);
        actions.add(cancelButton);

        card.add(scrollPane, BorderLayout.CENTER);
        card.add(actions, BorderLayout.SOUTH);
        return card;
    }

    private JComponent buildDoctorAccountPanel() {
        JPanel card = createCard("Doctor Access");

        doctorStatusLabel = createStatusLabel("No doctor selected");
        doctorLoginIdField = new JTextField();
        doctorNameField = new JTextField();
        doctorSpecializationComboBox = new JComboBox<>(SPECIALIZATIONS);

        JButton loginButton = new JButton("Login with ID");
        stylePrimaryButton(loginButton);
        loginButton.addActionListener(e -> loginDoctor());

        JButton registerButton = new JButton("Register Doctor");
        styleSecondaryButton(registerButton);
        registerButton.addActionListener(e -> registerDoctor());

        card.add(labeledField("Doctor ID", doctorLoginIdField));
        card.add(buttonRow(loginButton));
        card.add(Box.createVerticalStrut(10));
        card.add(labeledField("Name", doctorNameField));
        card.add(labeledField("Specialization", doctorSpecializationComboBox));
        card.add(buttonRow(registerButton));
        card.add(Box.createVerticalStrut(10));
        card.add(doctorStatusLabel);

        return card;
    }

    private JComponent buildDoctorAppointmentsPanel() {
        JPanel card = createCard("Doctor Appointments");
        card.setLayout(new BorderLayout(12, 12));

        doctorAppointmentsList = new JList<>();
        doctorAppointmentsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        doctorAppointmentsList.setFont(new Font("Monospaced", Font.PLAIN, 13));

        JScrollPane scrollPane = new JScrollPane(doctorAppointmentsList);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(222, 228, 235)));

        JButton refreshButton = new JButton("Refresh");
        styleSecondaryButton(refreshButton);
        refreshButton.addActionListener(e -> refreshDoctorAppointments());

        JButton completeButton = new JButton("Complete Selected");
        stylePrimaryButton(completeButton);
        completeButton.addActionListener(e -> completeSelectedAppointment());

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        actions.setOpaque(false);
        actions.add(refreshButton);
        actions.add(completeButton);

        card.add(scrollPane, BorderLayout.CENTER);
        card.add(actions, BorderLayout.SOUTH);
        return card;
    }

    private JPanel createCard(String title) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 226, 232)),
                new EmptyBorder(18, 18, 18, 18)
        ));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        titleLabel.setForeground(new Color(24, 36, 48));
        titleLabel.setAlignmentX(JComponent.LEFT_ALIGNMENT);

        card.add(titleLabel);
        card.add(Box.createVerticalStrut(14));
        return card;
    }

    private JComponent labeledField(String label, JComponent field) {
        JPanel panel = new JPanel(new BorderLayout(0, 6));
        panel.setOpaque(false);
        panel.setAlignmentX(JComponent.LEFT_ALIGNMENT);

        JLabel labelComponent = new JLabel(label);
        labelComponent.setFont(new Font("SansSerif", Font.BOLD, 12));
        labelComponent.setForeground(new Color(77, 90, 104));
        panel.add(labelComponent, BorderLayout.NORTH);
        panel.add(field, BorderLayout.CENTER);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        return panel;
    }

    private JComponent buttonRow(JButton button) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        panel.setOpaque(false);
        panel.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        panel.add(button);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        return panel;
    }

    private JLabel createStatusLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.BOLD, 13));
        label.setForeground(new Color(15, 96, 108));
        label.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        return label;
    }

    private JSpinner buildDateSpinner() {
        SpinnerDateModel model = new SpinnerDateModel();
        JSpinner spinner = new JSpinner(model);
        JSpinner.DateEditor editor = new JSpinner.DateEditor(spinner, "yyyy-MM-dd");
        spinner.setEditor(editor);
        spinner.setValue(new Date());
        return spinner;
    }

    private void stylePrimaryButton(JButton button) {
        button.setFocusPainted(false);
        button.setBackground(new Color(11, 122, 117));
        button.setForeground(Color.WHITE);
    }

    private void styleSecondaryButton(JButton button) {
        button.setFocusPainted(false);
        button.setBackground(new Color(228, 236, 243));
        button.setForeground(new Color(32, 52, 73));
    }

    private void loginPatient() {
        Integer patientId = parseId(patientLoginIdField.getText(), "patient ID");
        if (patientId == null) {
            return;
        }

        Patient patient = facade.getPatientById(patientId);
        if (patient == null) {
            showMessage("No patient found for that ID.");
            return;
        }

        currentPatientId = patient.getId();
        patientStatusLabel.setText("Active patient: " + patient.getName() + " (#" + patient.getId() + ")");
        refreshPatientAppointments();
    }

    private void registerPatient() {
        String name = patientNameField.getText().trim();
        String phone = patientPhoneField.getText().trim();

        if (name.isEmpty() || phone.isEmpty()) {
            showMessage("Patient name and phone are required.");
            return;
        }

        int patientId = facade.registerPatient(name, phone);
        currentPatientId = patientId;
        patientStatusLabel.setText("Active patient: " + name + " (#" + patientId + ")");
        patientLoginIdField.setText(String.valueOf(patientId));
        patientNameField.setText("");
        patientPhoneField.setText("");
        refreshPatientAppointments();
        showMessage("Patient registered with ID " + patientId + ".");
    }

    private void loginDoctor() {
        Integer doctorId = parseId(doctorLoginIdField.getText(), "doctor ID");
        if (doctorId == null) {
            return;
        }

        Doctor doctor = facade.getDoctorById(doctorId);
        if (doctor == null) {
            showMessage("No doctor found for that ID.");
            return;
        }

        currentDoctorId = doctor.getId();
        doctorStatusLabel.setText("Active doctor: " + doctor.getName() + " (#" + doctor.getId() + ")");
        refreshDoctorAppointments();
    }

    private void registerDoctor() {
        String name = doctorNameField.getText().trim();
        String specialization = (String) doctorSpecializationComboBox.getSelectedItem();

        if (name.isEmpty() || specialization == null || specialization.isBlank()) {
            showMessage("Doctor name and specialization are required.");
            return;
        }

        int doctorId = facade.addDoctor(name, specialization);
        currentDoctorId = doctorId;
        doctorStatusLabel.setText("Active doctor: " + name + " (#" + doctorId + ")");
        doctorLoginIdField.setText(String.valueOf(doctorId));
        doctorNameField.setText("");
        refreshDoctorSelections();
        refreshDoctorAppointments();
        showMessage("Doctor registered with ID " + doctorId + ".");
    }

    private void refreshDoctorSelections() {
        List<String> specializations = facade.getAllSpecializations();
        specializationComboBox.setModel(new DefaultComboBoxModel<>(specializations.toArray(new String[0])));
        if (specializations.isEmpty()) {
            doctorComboBox.setModel(new DefaultComboBoxModel<>());
            slotComboBox.setModel(new DefaultComboBoxModel<>());
            return;
        }

        if (specializationComboBox.getSelectedItem() == null) {
            specializationComboBox.setSelectedIndex(0);
        }

        String selectedSpecialization = (String) specializationComboBox.getSelectedItem();
        List<Doctor> doctors = facade.getDoctorsBySpecialization(selectedSpecialization);
        doctorComboBox.setModel(new DefaultComboBoxModel<>(doctors.toArray(new Doctor[0])));
        refreshAvailableSlots();
    }

    private void refreshAvailableSlots() {
        Doctor doctor = (Doctor) doctorComboBox.getSelectedItem();
        if (doctor == null) {
            slotComboBox.setModel(new DefaultComboBoxModel<>());
            return;
        }

        LocalDate date = spinnerToLocalDate();
        List<LocalTime> slots = facade.getAvailableSlots(doctor.getId(), date);
        slotComboBox.setModel(new DefaultComboBoxModel<>(slots.toArray(new LocalTime[0])));
    }

    private void bookSelectedSlot() {
        if (currentPatientId == null) {
            showMessage("Log in or register a patient first.");
            return;
        }

        Doctor doctor = (Doctor) doctorComboBox.getSelectedItem();
        LocalTime time = (LocalTime) slotComboBox.getSelectedItem();
        if (doctor == null || time == null) {
            showMessage("Choose a doctor and an available slot first.");
            return;
        }

        boolean booked = facade.bookAppointmentInSlot(currentPatientId, doctor.getId(), spinnerToLocalDate(), time);
        if (!booked) {
            showMessage("Booking failed. The slot may be unavailable or the date may be invalid.");
            refreshAvailableSlots();
            return;
        }

        showMessage("Appointment booked for " + doctor.getName() + " at " + time + ".");
        refreshAvailableSlots();
        refreshPatientAppointments();
    }

    private void refreshPatientAppointments() {
        if (currentPatientId == null) {
            patientAppointmentsList.setListData(new Appointment[0]);
            return;
        }

        List<Appointment> appointments = facade.getAppointmentsForPatient(currentPatientId);
        patientAppointmentsList.setListData(appointments.toArray(new Appointment[0]));
    }

    private void cancelSelectedAppointment() {
        if (currentPatientId == null) {
            showMessage("Log in as a patient first.");
            return;
        }

        Appointment appointment = patientAppointmentsList.getSelectedValue();
        if (appointment == null) {
            showMessage("Select an appointment to cancel.");
            return;
        }

        boolean cancelled = facade.cancelAppointmentForPatient(appointment.getId(), currentPatientId);
        if (!cancelled) {
            showMessage("Unable to cancel that appointment.");
            return;
        }

        refreshPatientAppointments();
        refreshAvailableSlots();
        showMessage("Appointment updated.");
    }

    private void refreshDoctorAppointments() {
        if (currentDoctorId == null) {
            doctorAppointmentsList.setListData(new Appointment[0]);
            return;
        }

        List<Appointment> appointments = facade.getAppointmentsForDoctor(currentDoctorId);
        doctorAppointmentsList.setListData(appointments.toArray(new Appointment[0]));
    }

    private void completeSelectedAppointment() {
        if (currentDoctorId == null) {
            showMessage("Log in as a doctor first.");
            return;
        }

        Appointment appointment = doctorAppointmentsList.getSelectedValue();
        if (appointment == null) {
            showMessage("Select an appointment to complete.");
            return;
        }

        boolean completed = facade.completeAppointmentForDoctor(appointment.getId(), currentDoctorId);
        if (!completed) {
            showMessage("Unable to complete that appointment.");
            return;
        }

        refreshDoctorAppointments();
        showMessage("Appointment updated.");
    }

    private Integer parseId(String value, String label) {
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            showMessage("Enter a valid " + label + ".");
            return null;
        }
    }

    private LocalDate spinnerToLocalDate() {
        Date selectedDate = (Date) appointmentDateSpinner.getValue();
        return Instant.ofEpochMilli(selectedDate.getTime())
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
    }

    private void showMessage(String message) {
        JOptionPane.showMessageDialog(frame, message, "Clinic", JOptionPane.INFORMATION_MESSAGE);
    }
}
