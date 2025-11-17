package com.login.calendar.service;

import com.login.calendar.dto.CalendarEventDTO;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ICalendarService {

    List<CalendarEventDTO> listCalendarEvents(String googleAccessToken) throws IOException;
    List<CalendarEventDTO> listCalendarEventsByMonth(String googleAccessToken, LocalDate date) throws IOException;
    List<CalendarEventDTO> listCalendarEventsByWeek(String googleAccessToken, LocalDate date) throws IOException;
    List<CalendarEventDTO> listCalendarEventsByDay(String googleAccessToken, LocalDate date) throws IOException;
    List<CalendarEventDTO> searchCalendarEvents(String googleAccessToken, LocalDate startDate, Optional<LocalDate> endDateOptional) throws IOException;

}
