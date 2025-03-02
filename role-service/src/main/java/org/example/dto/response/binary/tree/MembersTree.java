package org.example.dto.response.binary.tree;


import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.example.dto.response.MemberDto;
import org.example.dto.response.binary.node.MemberNode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class MembersTree extends Tree{
    {
        super.setMainAttribute("name");
    }

    public MembersTree(List<MemberDto> members){
        for (MemberDto member : members)
            super.insert(new MemberNode(member));

    }
}
