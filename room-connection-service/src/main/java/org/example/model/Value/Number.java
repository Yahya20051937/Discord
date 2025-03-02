package org.example.model.Value;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Number implements Value{
    private int value;
    @Override
    public String getAttribute(String key) {
        return String.valueOf(value);
    }

    @Override
    public void performAction(String action, Object... args) {

    }
}
