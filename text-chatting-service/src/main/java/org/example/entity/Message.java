package org.example.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.MediaType;
import org.example.dto.SendMessageRequest;

import java.sql.Date;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "message", indexes ={
        @Index(name = "counter_idx", columnList = "counter"),
        @Index(name = "roomId_idx", columnList = "roomId")
})
public class Message {
    @Id
    private String id;
    private String writer;
    private String content;
    private MediaType mediaType;
    private Date date;
    private Time time;
    private String roomId;
    private long counter;
    public Message(SendMessageRequest request, String username, long counter){
        this.id = UUID.randomUUID().toString();
        this.writer = username;
        this.content = request.getContent();
        this.mediaType = request.getMediaType();
        this.date = Date.valueOf(LocalDate.now());
        this.time = Time.valueOf(LocalTime.now());
        this.roomId = request.getRoomId();
        this.counter = counter;
    }


}
