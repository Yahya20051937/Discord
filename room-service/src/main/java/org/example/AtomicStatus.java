package org.example;

import lombok.Data;
import org.springframework.http.HttpStatusCode;

@Data
public class AtomicStatus {
    private HttpStatusCode httpStatusCode;

    public void set(HttpStatusCode status){
        this.httpStatusCode = status;
    }

    public HttpStatusCode get(){
        return this.httpStatusCode;
    }
}
