package com.login.calendar.service;

import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.ConferenceData;
import com.google.api.services.calendar.model.EntryPoint;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;
import com.login.calendar.dto.CalendarEventDTO;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.time.*;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;


@Service
public class CalendarService implements ICalendarService {

    private static final Logger logger = Logger.getLogger(CalendarService.class.getName());
    private static final String STARTING_CALENDAR_EVENTS_SEARCH = "Starting calendar events search...";
    private static final String BRANDING_ORGANIZER_EMAIL = "branding@mobydigital.com";

    @Override
    public List<CalendarEventDTO> listCalendarEvents(String googleAccessToken) throws IOException {
        logger.info(STARTING_CALENDAR_EVENTS_SEARCH);

        AccessToken accessToken = new AccessToken(googleAccessToken, null);
        GoogleCredentials credentials = GoogleCredentials.create(accessToken);

        com.google.api.services.calendar.Calendar service =
                new com.google.api.services.calendar.Calendar.Builder(
                        new NetHttpTransport(),
                        GsonFactory.getDefaultInstance(),
                        new HttpCredentialsAdapter(credentials))
                        .setApplicationName("Google Auth API")
                        .build();

        com.google.api.services.calendar.model.Events events = service.events()
                .list("primary")
                .setMaxResults(5)
                .setTimeMin(new com.google.api.client.util.DateTime(System.currentTimeMillis()))
                .setOrderBy("startTime")
                .setSingleEvents(true)
                .execute();

        return getCalendarEventDtos(events);
    }

    @Override
    public List<CalendarEventDTO> listCalendarEventsByMonth(String googleAccessToken, LocalDate date) throws IOException {
        logger.info(STARTING_CALENDAR_EVENTS_SEARCH);

        LocalDate startDate = YearMonth.from(date).atDay(1);
        LocalDate endDate   = YearMonth.from(date).atEndOfMonth();

        return searchCalendarEvents(googleAccessToken,startDate,Optional.of(endDate));
    }

    @Override
    public List<CalendarEventDTO> listCalendarEventsByWeek(String googleAccessToken, LocalDate date) throws IOException {
        logger.info(STARTING_CALENDAR_EVENTS_SEARCH);

        LocalDate startDate = date.with(java.time.temporal.TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate endDate = date.with(java.time.temporal.TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

        return searchCalendarEvents(googleAccessToken,startDate,Optional.of(endDate));
    }

    @Override
    public List<CalendarEventDTO> listCalendarEventsByDay(String googleAccessToken, LocalDate date) throws IOException {
        logger.info(STARTING_CALENDAR_EVENTS_SEARCH);
        return searchCalendarEvents(googleAccessToken,date,Optional.empty());
    }

    @Override
    public List<CalendarEventDTO> searchCalendarEvents(String googleAccessToken, LocalDate startDate, Optional<LocalDate> endDateOptional ) throws IOException {

        AccessToken accessToken = new AccessToken(googleAccessToken, null);
        GoogleCredentials credentials = GoogleCredentials.create(accessToken);
        ZonedDateTime startZdt;
        ZonedDateTime endZdt;

        startZdt = startDate.atStartOfDay(ZoneId.systemDefault());

        endZdt = endDateOptional
                .orElse(startDate)
                .atTime(LocalTime.MAX)
                .atZone(ZoneId.systemDefault());

        DateTime timeMin = new DateTime(Date.from(startZdt.plusSeconds(1).toInstant()));
        DateTime timeMax = new DateTime(Date.from(endZdt.plusSeconds(1).toInstant()));

        com.google.api.services.calendar.Calendar service =
                new com.google.api.services.calendar.Calendar.Builder(
                        new NetHttpTransport(),
                        GsonFactory.getDefaultInstance(),
                        new HttpCredentialsAdapter(credentials))
                        .setApplicationName("Google Auth API")
                        .build();

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

    private List<CalendarEventDTO> getCalendarEventDtos(Events events) {

        if (events.getItems() == null || events.getItems().isEmpty()) {
            return Collections.emptyList();
        }

        return events.getItems().stream()
                .map(this::mapEventToDTO)
                .collect(Collectors.toList());
    }

    private CalendarEventDTO mapEventToDTO(Event event) {

        String title = Optional.ofNullable(event.getSummary()).orElse("No title");
        String description = Optional.ofNullable(event.getDescription()).orElse("");
        String dateTime = resolveDateTime(event);
        String urlMeet = resolveMeetUrl(event);

        CalendarEventDTO dto = new CalendarEventDTO();
        dto.setTitle(title);
        dto.setDescription(description);
        dto.setDateTime(dateTime);
        dto.setMeetUrl(urlMeet);
        dto.setKindEvent(resolveKindEvent(event));

        return dto;
    }

    private String resolveKindEvent(Event event) {
        Event.Organizer organizer = event.getOrganizer();

        if (organizer != null && BRANDING_ORGANIZER_EMAIL.equals(organizer.getEmail())) {
            return "Branding";
        }
        return "Personal";
    }

    private String resolveDateTime(Event event) {
        if (event.getStart() == null) {
            return "";
        }

        if (event.getStart().getDateTime() != null) {
            return event.getStart().getDateTime().toString();
        }

        if (event.getStart().getDate() != null) {
            return event.getStart().getDate().toString();
        }
        return "";

    }

    private String resolveMeetUrl(Event event) {

        if (event.getHangoutLink() != null && !event.getHangoutLink().isEmpty()) {
            return event.getHangoutLink();
        }

        return Optional.ofNullable(event.getConferenceData())
                .map(ConferenceData::getEntryPoints)
                .orElse(Collections.emptyList())
                .stream()
                .filter(Objects::nonNull)
                .filter(ep -> "video".equalsIgnoreCase(ep.getEntryPointType()))
                .filter(ep -> ep.getUri() != null && !ep.getUri().isEmpty())
                .findFirst()
                .map(EntryPoint::getUri)
                .orElse(null);
    }
}
