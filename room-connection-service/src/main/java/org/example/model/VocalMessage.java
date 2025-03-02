package org.example.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class VocalMessage implements MessageInt {
    private String roomId;
    private String audioBytes64;
    private String speaker;

    @Override
    public String toJson() {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.writeValueAsString(this);
        }  catch (JsonProcessingException e){
            return null;
        }
    }

    @Override
    public String getSender() {
        return this.getSpeaker();
    }
}
