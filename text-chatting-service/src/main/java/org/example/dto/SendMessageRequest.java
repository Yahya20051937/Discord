package org.example.dto;

import lombok.Data;
import org.example.MediaType;

@Data
public class SendMessageRequest {
    private String content;
    private MediaType mediaType;
    private String roomId;
}
