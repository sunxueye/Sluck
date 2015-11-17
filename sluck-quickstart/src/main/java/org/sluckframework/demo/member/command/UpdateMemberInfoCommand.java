package org.sluckframework.demo.member.command;

import org.sluckframework.cqrs.commandhandling.annotation.TargetAggregateIdentifier;
import org.sluckframework.demo.member.MemberID;

/**
 * Author: sunxy
 * Created: 2015-10-11 12:47
 * Since: 1.0
 */
public class UpdateMemberInfoCommand {

    @TargetAggregateIdentifier
    private MemberID memberID;

    private String newName;
    private String newIdCard;

    public UpdateMemberInfoCommand(MemberID memberID, String newName, String newIdCard) {
        this.memberID = memberID;
        this.newName = newName;
        this.newIdCard = newIdCard;
    }

    public MemberID getMemberID() {
        return memberID;
    }

    public String getNewName() {
        return newName;
    }

    public String getNewIdCard() {
        return newIdCard;
    }
}
