package org.example.dto.response;

import lombok.Data;
import org.example.entitiy.MemberShip;

@Data
public class MemberShipDto {
    private String id;
    private String member;
    private String serverId;

    public MemberShipDto(MemberShip memberShip){
        this.id = memberShip.getId();
        this.member = memberShip.getMember();
        this.serverId = memberShip.getServer().getId();
    }
}
