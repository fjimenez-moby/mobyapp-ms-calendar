package com.login.calendar.controller;

import com.login.calendar.dto.CalendarEventDto;
import com.login.calendar.service.CalendarService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@RestController
@RequestMapping("/api/calendar")
public class CalendarController {

    private static final Logger logger = Logger.getLogger(CalendarController.class.getName());

    @Autowired
    private CalendarService service;

    @GetMapping("/events")
    public ResponseEntity<Object> getCalendarEvents(@RequestHeader("Authorization") String authHeader)
            throws IOException
    {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Object() {
                public final boolean success = false;
                public final String message = "Autorización requerida. Encabezado 'Authorization' no encontrado o inválido.";
            });
        }
        String googleAccessToken = authHeader.substring(7); // Remueve "Bearer "

        // Llama al servicio para obtener los eventos usando el accessToken de Google
        List<CalendarEventDto> events = service.listCalendarEvents(googleAccessToken);

        Object response = createResponse(events);
        return ResponseEntity.ok().body(response);

    }

    @GetMapping("/events/month")
    public ResponseEntity<Object> getCalendarEventsByMonth(@RequestHeader("Authorization") String authHeader,
                                                           @RequestParam LocalDate date)
            throws IOException
    {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Object() {
                public final boolean success = false;
                public final String message = "Autorización requerida. Encabezado 'Authorization' no encontrado o inválido.";
            });
        }

        String googleAccessToken = authHeader.substring(7); // Remueve "Bearer "

        // Llama al servicio para obtener los eventos usando el accessToken de Google
        List<CalendarEventDto> events = service.listCalendarEventsByMonth(googleAccessToken,date);

        Object response = createResponse(events);
        return ResponseEntity.ok().body(response);

    }

    @GetMapping("/events/week")
    public ResponseEntity<Object> getCalendarEventsByWeek(@RequestHeader("Authorization") String authHeader,
                                                          @RequestParam LocalDate date)
            throws IOException
    {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Object() {
                public final boolean success = false;
                public final String message = "Autorización requerida. Encabezado 'Authorization' no encontrado o inválido.";
            });
        }

        String googleAccessToken = authHeader.substring(7); // Remueve "Bearer "

        // Llama al servicio para obtener los eventos usando el accessToken de Google
        List<CalendarEventDto> events = service.listCalendarEventsByWeek(googleAccessToken,date);

        Object response = createResponse(events);
        return ResponseEntity.ok().body(response);

    }

    @GetMapping("/events/day")
    public ResponseEntity<Object> getCalendarEventsByDate(@RequestHeader("Authorization") String authHeader,
                                                          @RequestParam LocalDate date)
            throws IOException
    {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Object() {
                public final boolean success = false;
                public final String message = "Autorización requerida. Encabezado 'Authorization' no encontrado o inválido.";
            });
        }

        String googleAccessToken = authHeader.substring(7); // Remueve "Bearer "

        // Llama al servicio para obtener los eventos usando el accessToken de Google
        List<CalendarEventDto> events = service.listCalendarEventsByDay(googleAccessToken,date);

        Object response = createResponse(events);
        return ResponseEntity.ok().body(response);

    }


    public Object createResponse (List<CalendarEventDto> events) {
        return new Object() {
            public final boolean success = true;
            public final String message = "Eventos del calendario obtenidos exitosamente";
            public final int eventCount = events.size();
            public final List<CalendarEventDto> eventsList = events;
            public final long timestamp = System.currentTimeMillis();
            public final String note = "Mostrando próximos " + events.size() + " eventos";
        };
    }

}
