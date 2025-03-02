package org.example.dto.response.binary.node;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.example.dto.response.RoleDto;
import org.example.entity.Role;

@Data
@EqualsAndHashCode(callSuper = true)
public class RoleNode extends Node{
    public RoleNode(RoleDto role){
        super.setValue(role);
    }
}
