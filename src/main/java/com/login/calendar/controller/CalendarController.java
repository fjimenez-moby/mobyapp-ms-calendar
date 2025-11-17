package com.login.calendar.controller;

import com.login.calendar.dto.CalendarEventDTO;
import com.login.calendar.service.CalendarService;
import com.login.calendar.exception.InvalidAuthHeaderException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.logging.Logger;

@RestController
@RequestMapping("/api/calendar")
public class CalendarController {

    private static final boolean SUCCESS_STATUS = true;
    private static final String SUCCESS_MESSAGE = "Eventos del calendario obtenidos exitosamente";
    private static final Logger logger = Logger.getLogger(CalendarController.class.getName());
    private final CalendarService service;

    @Autowired
    public CalendarController(CalendarService service) {
        this.service = service;
    }

    @GetMapping("/events")
    public ResponseEntity<Object> getCalendarEvents(@RequestHeader("Authorization") String authHeader) throws IOException {
        logger.info("GET /events");

        String googleAccessToken = getGoogleAccessToken(authHeader);

        List<CalendarEventDTO> events = service.listCalendarEvents(googleAccessToken);

        return ResponseEntity.ok().body(createResponse(events));
    }

    @GetMapping("/events/month")
    public ResponseEntity<Object> getCalendarEventsByMonth(@RequestHeader("Authorization") String authHeader,
                                                           @RequestParam LocalDate date) throws IOException {
        logger.info("GET /events/month");

        String googleAccessToken = getGoogleAccessToken(authHeader);

        List<CalendarEventDTO> events = service.listCalendarEventsByMonth(googleAccessToken,date);

        return ResponseEntity.ok().body(createResponse(events));
    }

    @GetMapping("/events/week")
    public ResponseEntity<Object> getCalendarEventsByWeek(@RequestHeader("Authorization") String authHeader,
                                                          @RequestParam LocalDate date) throws IOException {
        logger.info("GET /events/week");

        String googleAccessToken = getGoogleAccessToken(authHeader);

        List<CalendarEventDTO> events = service.listCalendarEventsByWeek(googleAccessToken,date);

        return ResponseEntity.ok().body(createResponse(events));
    }

    @GetMapping("/events/day")
    public ResponseEntity<Object> getCalendarEventsByDate(@RequestHeader("Authorization") String authHeader,
                                                          @RequestParam LocalDate date) throws IOException {
        logger.info("GET /events/day");

        String googleAccessToken = getGoogleAccessToken(authHeader);

        List<CalendarEventDTO> events = service.listCalendarEventsByDay(googleAccessToken,date);

        return ResponseEntity.ok().body(createResponse(events));
    }

    private String getGoogleAccessToken(String authHeader){
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            logger.warning("UNAUTHORIZED: Invalid or missing 'Authorization' header.");
            throw new InvalidAuthHeaderException("Autorización requerida. Encabezado 'Authorization' no encontrado o inválido.");
        }
        logger.info("Access token successfully extracted.");
        return authHeader.substring(7);
    }

    private Object createResponse (List<CalendarEventDTO> events) {
        logger.info("Create Response");
        return new Object() {
            public final boolean success = SUCCESS_STATUS;
            public final String message = SUCCESS_MESSAGE;
            public final int eventCount = events.size();
            public final List<CalendarEventDTO> eventsList = events;
            public final long timestamp = System.currentTimeMillis();
            public final String note = "Mostrando próximos " + events.size() + " eventos";
        };
    }
}
