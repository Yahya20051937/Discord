package org.example.model;

import jakarta.annotation.security.DenyAll;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DateTime{
    private int month;
    private int year;
    private int day;
    private int hour;
    private int minute;
    private int second;
}
