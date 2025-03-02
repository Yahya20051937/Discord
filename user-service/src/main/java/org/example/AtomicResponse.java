package org.example;

import lombok.Data;
import org.springframework.http.ResponseEntity;

@Data
public class AtomicResponse {
    private int status;
    private Object body;

    public ResponseEntity<?> toResponse(){
        return ResponseEntity.status(status)
                .body(body)
                ;
    }
}
