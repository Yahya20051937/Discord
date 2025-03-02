package org.example.model.Value;

import lombok.Data;


public interface Value {
    public String getAttribute(String key);
    public void performAction(String action, Object...args);
}
