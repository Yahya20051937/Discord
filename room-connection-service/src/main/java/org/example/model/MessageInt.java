package org.example.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

public interface MessageInt {
    @JsonIgnore
    String toJson();

    String getRoomId();
    @JsonIgnore
    String getSender();
}
