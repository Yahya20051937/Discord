package org.example.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@AllArgsConstructor
public class MemberDto implements Value{
    private String name;
    private int mainRoleRanking;

    @Override
    public String getAttribute(String key) {
        return switch (key){
            case "name" -> this.name;
            case "mainRoleRanking" -> String.valueOf(this.mainRoleRanking);
            default -> null;
        } ;
    }

    @Override
    public void performAction(String action, Object... args) {

    }

    @Override
    public String getType() {
        return "member";
    }
}
