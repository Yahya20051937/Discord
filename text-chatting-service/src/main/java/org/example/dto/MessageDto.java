package org.example.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.MediaType;
import org.example.entity.Message;
import org.example.model.DateTime;

import java.sql.Date;
import java.sql.Time;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MessageDto {
    private String id;
    private String writer;
    private String content;
    private MediaType mediaType;
    private DateTime dateTime;
    private String roomId;

    private long counter;

    public MessageDto(Message message){
        this.id = message.getId();
        this.writer  = message.getWriter();
        this.content = message.getContent();
        this.mediaType = message.getMediaType();
        this.dateTime = new DateTime(message.getDate().toLocalDate(), message.getTime().toLocalTime());
        this.roomId = message.getRoomId();
        this.counter = message.getCounter();
    }
}
