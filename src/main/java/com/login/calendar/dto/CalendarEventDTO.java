package com.login.calendar.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CalendarEventDTO {
    private  String title;
    private  String description;
    private  String dateTime;
    private  String meetUrl;
    private  String kindEvent;
}