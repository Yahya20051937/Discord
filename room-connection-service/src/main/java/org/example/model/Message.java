package org.example.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.discovery.provider.Serializer;
import lombok.Data;
import org.example.MediaType;

import java.io.Serializable;


@Data
public class Message implements MessageInt{
    private String id;
    private String writer;
    private String content;
    private DateTime dateTime;
    private String roomId;
    private MediaType mediaType;
    private long counter;


    public String toJson(){
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.writeValueAsString(this);
        }  catch (JsonProcessingException e){
            return null;
        }

    }

    @Override
    public String getSender() {
        return this.getWriter();
    }
}
