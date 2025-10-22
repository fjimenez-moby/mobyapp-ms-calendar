package com.login.calendar.service;

import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.EntryPoint;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;
import com.login.calendar.dto.CalendarEventDto;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.*;
import java.util.*;
import java.util.logging.Logger;


@Service
public class CalendarService {

    private static final Logger logger = Logger.getLogger(CalendarService.class.getName());


    public List<CalendarEventDto> listCalendarEvents(String googleAccessToken) throws IOException {
        List<CalendarEventDto> eventList = new ArrayList<>();
        logger.info("Starting calendar events search...");

        // Construir las credenciales para la API de Google con el accessToken recibido
        AccessToken accessToken = new AccessToken(googleAccessToken, null);
        GoogleCredentials credentials = GoogleCredentials.create(accessToken);

        // Construir el servicio de Google Calendar
        com.google.api.services.calendar.Calendar service =
                new com.google.api.services.calendar.Calendar.Builder(
                        new NetHttpTransport(),
                        GsonFactory.getDefaultInstance(),
                        new HttpCredentialsAdapter(credentials))
                        .setApplicationName("Google Auth API")
                        .build();

        // Obtener los eventos
        com.google.api.services.calendar.model.Events events = service.events()
                .list("primary")
                .setMaxResults(5)
                .setTimeMin(new com.google.api.client.util.DateTime(System.currentTimeMillis()))
                .setOrderBy("startTime")
                .setSingleEvents(true)
                .execute();

        return getCalendarEventDtos(events);
    }

    public List<CalendarEventDto> listCalendarEventsByMonth(String googleAccessToken,LocalDate date) throws IOException {

        logger.info("Starting calendar events search...");

        // Construir las credenciales para la API de Google con el accessToken recibido

        LocalDate startDate = YearMonth.from(date).atDay(1);
        LocalDate endDate   = YearMonth.from(date).atEndOfMonth();

        return searchCalendarEvents(googleAccessToken,startDate, Optional.of(endDate));

    }

    public List<CalendarEventDto> listCalendarEventsByWeek(String googleAccessToken,LocalDate date) throws IOException {
        logger.info("Starting calendar events search...");

        // Primer día de la semana (lunes)
        LocalDate startDate = date.with(java.time.temporal.TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        // Último día de la semana (domingo)
        LocalDate endDate = date.with(java.time.temporal.TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

        return searchCalendarEvents(googleAccessToken,startDate, Optional.of(endDate));
    }

    public List<CalendarEventDto> listCalendarEventsByDay(String googleAccessToken,LocalDate date) throws IOException {
        logger.info("Starting calendar events search...");
        return searchCalendarEvents(googleAccessToken,date, Optional.empty());
    }

    public List<CalendarEventDto> searchCalendarEvents(String googleAccessToken, LocalDate startDate, Optional<LocalDate> endDateOptional ) throws IOException {
        List<CalendarEventDto> eventList = new ArrayList<>();

        AccessToken accessToken = new AccessToken(googleAccessToken, null);
        GoogleCredentials credentials = GoogleCredentials.create(accessToken);
        ZonedDateTime startZdt;
        ZonedDateTime endZdt;

        if(endDateOptional.isPresent()){
            LocalDate endDate = endDateOptional.orElse(LocalDate.now());
            startZdt = startDate.atStartOfDay(ZoneId.systemDefault());
            endZdt   = endDate.atTime(LocalTime.MAX).atZone(ZoneId.systemDefault());
        }
        else{
            startZdt = startDate.atStartOfDay(ZoneId.systemDefault());
            endZdt   = startDate.atTime(LocalTime.MAX).atZone(ZoneId.systemDefault());
        }
        DateTime timeMin = new DateTime(Date.from(startZdt.plusSeconds(1).toInstant()));
        DateTime timeMax = new DateTime(Date.from(endZdt.plusSeconds(1).toInstant()));

        // Construir el servicio de Google Calendar
        com.google.api.services.calendar.Calendar service =
                new com.google.api.services.calendar.Calendar.Builder(
                        new NetHttpTransport(),
                        GsonFactory.getDefaultInstance(),
                        new HttpCredentialsAdapter(credentials))
                        .setApplicationName("Google Auth API")
                        .build();

        // Obtener los eventos
        com.google.api.services.calendar.model.Events events = service.events()
                .list("primary")
                .setMaxResults(100)
                .setTimeMin(timeMin)
                .setTimeMax(timeMax)
                .setOrderBy("startTime")
                .setSingleEvents(true)
                .execute();

        return getCalendarEventDtos(events);
    }

    private List<CalendarEventDto> getCalendarEventDtos(Events events) {
        List<CalendarEventDto> listEvents = new ArrayList<>();

        if (events.getItems() != null && !events.getItems().isEmpty()) {
            for (com.google.api.services.calendar.model.Event event : events.getItems()) {
                Event.Organizer organizer = event.getOrganizer();

                String title = event.getSummary() != null ? event.getSummary() : "No title";


                String description = event.getDescription() != null ? event.getDescription() : "";
                String dateTime = "";
                String urlMeet = resolveMeetUrl(event);


                if (event.getStart() != null) {
                    if (event.getStart().getDateTime() != null) {
                        dateTime = event.getStart().getDateTime().toString();
                    } else if (event.getStart().getDate() != null) {
                        dateTime = event.getStart().getDate().toString();
                    }
                }

                CalendarEventDto dto = new CalendarEventDto();
                dto.setTitle(title);
                dto.setDescription(description);
                dto.setDateTime(dateTime);
                dto.setMeetUrl(urlMeet);

                if (organizer != null && "branding@mobydigital.com".equals(organizer.getEmail())) {

                    dto.setKindEvent("Branding");
                } else {
                    dto.setKindEvent("Personal");
                }
                listEvents.add(dto);
            }
        }

        return listEvents;
    }


    private String resolveMeetUrl(Event event) {
        if (event.getHangoutLink() != null && !event.getHangoutLink().isEmpty()) {
            return event.getHangoutLink();
        }

        if (event.getConferenceData() != null &&
                event.getConferenceData().getEntryPoints() != null) {

            List<EntryPoint> eps = event.getConferenceData().getEntryPoints();
            for (EntryPoint ep : eps) {
                if (ep != null &&
                        ep.getEntryPointType() != null &&
                        ep.getEntryPointType().equalsIgnoreCase("video") &&
                        ep.getUri() != null &&
                        !ep.getUri().isEmpty()) {
                    return ep.getUri();
                }
            }
        }
        return null;
    }
}
