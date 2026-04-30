package org.example.api;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.example.ClinicFacade;
import org.example.Doctor;
import org.example.dto.AppointmentDetails;
import org.example.service.OperationResult;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

public class ClinicHttpServer {
    private final ClinicFacade facade;
    private final HttpServer server;

    public ClinicHttpServer(ClinicFacade facade, int port) {
        this.facade = facade;
        try {
            this.server = HttpServer.create(new InetSocketAddress(port), 0);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create HTTP server", e);
        }
        server.setExecutor(Executors.newCachedThreadPool());
        registerRoutes();
    }

    public void start() {
        server.start();
    }

    public void stop(int delaySeconds) {
        server.stop(delaySeconds);
    }

    public int getPort() {
        return server.getAddress().getPort();
    }

    private void registerRoutes() {
        server.createContext("/api/health", exchange ->
                sendJson(exchange, 200, "{\"status\":\"ok\"}"));

        server.createContext("/api/doctors", new DoctorsHandler());
        server.createContext("/api/patients", new PatientsHandler());
        server.createContext("/api/appointments", new AppointmentsHandler());
    }

    private class DoctorsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            String method = exchange.getRequestMethod();

            if ("/api/doctors".equals(path)) {
                if ("GET".equals(method)) {
                    sendJson(exchange, 200, buildDoctorsJson(facade.getAllDoctors()));
                    return;
                }
                if ("POST".equals(method)) {
                    Map<String, String> query = parseQuery(exchange.getRequestURI());
                    OperationResult<Integer> result = facade.addDoctorResult(
                            query.get("name"),
                            query.get("specialization")
                    );
                    sendOperationResult(exchange, result, 201, "{\"doctorId\":" + safeInt(result.getPayload()) + "}");
                    return;
                }
            }

            if (path.matches("/api/doctors/\\d+/appointments") && "GET".equals(method)) {
                Integer doctorId = extractId(path, "/api/doctors/", "/appointments");
                if (doctorId == null) {
                    sendJson(exchange, 400, errorJson("Invalid doctor id."));
                    return;
                }
                sendJson(exchange, 200, buildAppointmentsJson(facade.getAppointmentDetailsForDoctor(doctorId)));
                return;
            }

            if (path.matches("/api/doctors/\\d+/slots") && "GET".equals(method)) {
                Integer doctorId = extractId(path, "/api/doctors/", "/slots");
                if (doctorId == null) {
                    sendJson(exchange, 400, errorJson("Invalid doctor id."));
                    return;
                }
                Map<String, String> query = parseQuery(exchange.getRequestURI());
                String dateValue = query.get("date");
                if (dateValue == null) {
                    sendJson(exchange, 400, errorJson("Missing required query parameter: date."));
                    return;
                }
                try {
                    LocalDate date = LocalDate.parse(dateValue);
                    sendJson(exchange, 200, buildSlotsJson(facade.getAvailableSlots(doctorId, date)));
                } catch (DateTimeParseException e) {
                    sendJson(exchange, 400, errorJson("Invalid date format. Use YYYY-MM-DD."));
                }
                return;
            }

            sendJson(exchange, 404, errorJson("Endpoint not found."));
        }
    }

    private class PatientsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            String method = exchange.getRequestMethod();

            if ("/api/patients".equals(path) && "POST".equals(method)) {
                Map<String, String> query = parseQuery(exchange.getRequestURI());
                OperationResult<Integer> result = facade.registerPatientResult(
                        query.get("name"),
                        query.get("phone")
                );
                sendOperationResult(exchange, result, 201, "{\"patientId\":" + safeInt(result.getPayload()) + "}");
                return;
            }

            if (path.matches("/api/patients/\\d+/appointments") && "GET".equals(method)) {
                Integer patientId = extractId(path, "/api/patients/", "/appointments");
                if (patientId == null) {
                    sendJson(exchange, 400, errorJson("Invalid patient id."));
                    return;
                }
                sendJson(exchange, 200, buildAppointmentsJson(facade.getAppointmentDetailsForPatient(patientId)));
                return;
            }

            sendJson(exchange, 404, errorJson("Endpoint not found."));
        }
    }

    private class AppointmentsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            String method = exchange.getRequestMethod();

            if ("/api/appointments/book".equals(path) && "POST".equals(method)) {
                Map<String, String> query = parseQuery(exchange.getRequestURI());
                try {
                    int patientId = Integer.parseInt(query.get("patientId"));
                    int doctorId = Integer.parseInt(query.get("doctorId"));
                    LocalDate date = LocalDate.parse(query.get("date"));
                    LocalTime time = LocalTime.parse(query.get("time"));
                    OperationResult<Void> result = facade.bookAppointmentInSlotResult(patientId, doctorId, date, time);
                    sendOperationResult(exchange, result, 200, "{}");
                } catch (Exception e) {
                    sendJson(exchange, 400, errorJson("Missing or invalid booking parameters."));
                }
                return;
            }

            if (path.matches("/api/appointments/\\d+/cancel") && "POST".equals(method)) {
                Integer appointmentId = extractId(path, "/api/appointments/", "/cancel");
                Map<String, String> query = parseQuery(exchange.getRequestURI());
                try {
                    int patientId = Integer.parseInt(query.get("patientId"));
                    OperationResult<Void> result = facade.cancelAppointmentForPatientResult(appointmentId, patientId);
                    sendOperationResult(exchange, result, 200, "{}");
                } catch (Exception e) {
                    sendJson(exchange, 400, errorJson("Missing or invalid cancel parameters."));
                }
                return;
            }

            if (path.matches("/api/appointments/\\d+/complete") && "POST".equals(method)) {
                Integer appointmentId = extractId(path, "/api/appointments/", "/complete");
                Map<String, String> query = parseQuery(exchange.getRequestURI());
                try {
                    int doctorId = Integer.parseInt(query.get("doctorId"));
                    OperationResult<Void> result = facade.completeAppointmentForDoctorResult(appointmentId, doctorId);
                    sendOperationResult(exchange, result, 200, "{}");
                } catch (Exception e) {
                    sendJson(exchange, 400, errorJson("Missing or invalid completion parameters."));
                }
                return;
            }

            sendJson(exchange, 404, errorJson("Endpoint not found."));
        }
    }

    private void sendOperationResult(HttpExchange exchange,
                                     OperationResult<?> result,
                                     int successStatus,
                                     String payloadJson) throws IOException {
        int statusCode = result.isSuccess() ? successStatus : 400;
        String body = "{\"success\":" + result.isSuccess()
                + ",\"message\":\"" + escapeJson(result.getMessage()) + "\""
                + ",\"data\":" + payloadJson + "}";
        sendJson(exchange, statusCode, body);
    }

    private void sendJson(HttpExchange exchange, int statusCode, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private Map<String, String> parseQuery(URI uri) {
        Map<String, String> params = new LinkedHashMap<>();
        String query = uri.getRawQuery();
        if (query == null || query.isEmpty()) {
            return params;
        }

        String[] pairs = query.split("&");
        for (String pair : pairs) {
            String[] parts = pair.split("=", 2);
            String key = decode(parts[0]);
            String value = parts.length > 1 ? decode(parts[1]) : "";
            params.put(key, value);
        }
        return params;
    }

    private Integer extractId(String path, String prefix, String suffix) {
        String value = path.substring(prefix.length(), path.length() - suffix.length());
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String buildDoctorsJson(List<Doctor> doctors) {
        List<String> items = new ArrayList<>();
        for (Doctor doctor : doctors) {
            items.add("{\"id\":" + doctor.getId()
                    + ",\"name\":\"" + escapeJson(doctor.getName()) + "\""
                    + ",\"specialization\":\"" + escapeJson(doctor.getSpecialization()) + "\""
                    + ",\"workStart\":\"" + doctor.getWorkStart() + "\""
                    + ",\"workEnd\":\"" + doctor.getWorkEnd() + "\""
                    + ",\"slotMinutes\":" + doctor.getSlotMinutes()
                    + "}");
        }
        return "{\"count\":" + doctors.size() + ",\"items\":[" + String.join(",", items) + "]}";
    }

    private String buildAppointmentsJson(List<AppointmentDetails> appointments) {
        List<String> items = new ArrayList<>();
        for (AppointmentDetails details : appointments) {
            items.add("{\"appointmentId\":" + details.getAppointmentId()
                    + ",\"doctorId\":" + details.getDoctorId()
                    + ",\"doctorName\":\"" + escapeJson(details.getDoctorName()) + "\""
                    + ",\"doctorSpecialization\":\"" + escapeJson(details.getDoctorSpecialization()) + "\""
                    + ",\"patientId\":" + details.getPatientId()
                    + ",\"patientName\":\"" + escapeJson(details.getPatientName()) + "\""
                    + ",\"dateTime\":\"" + details.getDateTime() + "\""
                    + ",\"status\":\"" + escapeJson(details.getStatus()) + "\""
                    + "}");
        }
        return "{\"count\":" + appointments.size() + ",\"items\":[" + String.join(",", items) + "]}";
    }

    private String buildSlotsJson(List<LocalTime> slots) {
        List<String> items = new ArrayList<>();
        for (LocalTime slot : slots) {
            items.add("\"" + slot + "\"");
        }
        return "{\"count\":" + slots.size() + ",\"items\":[" + String.join(",", items) + "]}";
    }

    private String errorJson(String message) {
        return "{\"success\":false,\"message\":\"" + escapeJson(message) + "\"}";
    }

    private String decode(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }

    private String escapeJson(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private int safeInt(Integer value) {
        return value == null ? -1 : value;
    }
}
