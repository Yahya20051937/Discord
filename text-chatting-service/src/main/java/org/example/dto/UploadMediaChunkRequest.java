package org.example.dto;

import lombok.Data;

@Data
public class UploadMediaChunkRequest {
    private String messageId;
    private String mediaChunk64;
    private long chunkCounter;
}
