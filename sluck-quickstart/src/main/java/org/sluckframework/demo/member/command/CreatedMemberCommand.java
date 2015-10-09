package org.sluckframework.demo.member.command;

/**
 * Author: sunxy
 * Created: 2015-10-09 23:45
 * Since: 1.0
 */
public class CreatedMemberCommand {

    private String name;
    private String idCard;

    public CreatedMemberCommand(String name, String idCard) {
        this.name = name;
        this.idCard = idCard;
    }

    public String getName() {
        return name;
    }

    public String getIdCard() {
        return idCard;
    }
}
