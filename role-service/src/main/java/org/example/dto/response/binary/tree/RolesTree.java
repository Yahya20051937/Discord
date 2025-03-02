package org.example.dto.response.binary.tree;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class RolesTree extends Tree {
    {
        super.setMainAttribute("ranking");
    }
}
