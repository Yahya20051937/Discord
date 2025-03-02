package org.example.dto.response.binary.tree;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class RoomsTree extends Tree {
    {
        super.setMainAttribute("id");
    }
}
