package org.example.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.sql.Date;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DateTime implements Serializable {
    private int month;
    private int year;
    private int day;
    private int hour;
    private int minute;
    private int second;

    public DateTime(LocalDate date, LocalTime time){
        this.month = date.getMonthValue();
        this.year = date.getYear();
        this.day = date.getDayOfMonth();
        this.hour = time.getHour();
        this.minute = time.getMinute();
        this.second = time.getSecond();
    }
}
