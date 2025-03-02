package org.example;

import org.springframework.core.io.ResourceLoader;
import org.springframework.http.ResponseEntity;

public class AtomicResponse {
    private ResponseEntity<?> response;

    public ResponseEntity<?> get(){
        return this.response;
    }

    public void set(ResponseEntity<?> response){
        this.response = response;
    }
}
