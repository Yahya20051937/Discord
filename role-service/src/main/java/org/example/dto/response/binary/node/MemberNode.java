package org.example.dto.response.binary.node;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.example.dto.response.MemberDto;

@Data
@EqualsAndHashCode(callSuper = true)
public class MemberNode extends Node{
    public MemberNode(MemberDto member){
        super.setValue(member);
    }
}
