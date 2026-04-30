package org.example;

import org.example.api.ClinicHttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClinicHttpServerTest {

    @TempDir
    Path tempDir;

    private ClinicHttpServer server;

    @AfterEach
    void tearDown() {
        if (server != null) {
            server.stop(0);
        }
        System.clearProperty("clinic.db.path");
        System.clearProperty("clinic.db.url");
    }

    @Test
    void healthEndpointReturnsOk() throws Exception {
        startServer("health.db");

        HttpResponse<String> response = request("GET", "/api/health");

        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("\"status\":\"ok\""));
    }

    @Test
    void apiSupportsRegistrationBookingAndLookup() throws Exception {
        startServer("api-flow.db");

        HttpResponse<String> doctorResponse = request(
                "POST",
                "/api/doctors?name=" + encode("Dr. Miles") + "&specialization=therapist"
        );
        assertEquals(201, doctorResponse.statusCode());
        assertTrue(doctorResponse.body().contains("\"success\":true"));

        HttpResponse<String> patientResponse = request(
                "POST",
                "/api/patients?name=" + encode("Alice") + "&phone=" + encode("+7-777-100-2000")
        );
        assertEquals(201, patientResponse.statusCode());
        assertTrue(patientResponse.body().contains("\"patientId\":1"));

        LocalDate nextDate = nextWeekday();
        HttpResponse<String> slotsResponse = request(
                "GET",
                "/api/doctors/1/slots?date=" + nextDate
        );
        assertEquals(200, slotsResponse.statusCode());
        assertTrue(slotsResponse.body().contains("\"09:00\""));

        HttpResponse<String> bookingResponse = request(
                "POST",
                "/api/appointments/book?patientId=1&doctorId=1&date=" + nextDate + "&time=09:00"
        );
        assertEquals(200, bookingResponse.statusCode());
        assertTrue(bookingResponse.body().contains("Appointment booked successfully"));

        HttpResponse<String> patientAppointments = request("GET", "/api/patients/1/appointments");
        assertEquals(200, patientAppointments.statusCode());
        assertTrue(patientAppointments.body().contains("Dr. Miles"));
        assertTrue(patientAppointments.body().contains("Alice"));
        assertTrue(patientAppointments.body().contains("CONFIRMED"));
    }

    private void startServer(String dbName) {
        System.setProperty("clinic.db.path", tempDir.resolve(dbName).toString());
        DatabaseInitializer.init();
        server = new ClinicHttpServer(new ClinicFacade(), 0);
        server.start();
    }

    private HttpResponse<String> request(String method, String path) throws IOException, InterruptedException {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + server.getPort() + path));

        if ("POST".equals(method)) {
            builder.POST(HttpRequest.BodyPublishers.noBody());
        } else {
            builder.GET();
        }

        return HttpClient.newHttpClient().send(builder.build(), HttpResponse.BodyHandlers.ofString());
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private LocalDate nextWeekday() {
        LocalDate date = LocalDate.now().plusDays(1);
        while (date.getDayOfWeek().getValue() >= 6) {
            date = date.plusDays(1);
        }
        return date;
    }
}
