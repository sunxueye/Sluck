package org.sluckframework.demo.member;

import org.sluckframework.cqrs.commandhandling.annotation.CommandHandler;
import org.sluckframework.cqrs.eventsourcing.annotation.AbstractAnnotatedAggregateRoot;
import org.sluckframework.cqrs.eventsourcing.annotation.AggregateIdentifier;
import org.sluckframework.cqrs.eventsourcing.annotation.EventSourcingHandler;
import org.sluckframework.demo.member.command.CreatedMemberCommand;
import org.sluckframework.demo.member.command.UpdateMemberInfoCommand;
import org.sluckframework.demo.member.event.MemberCreatedEvent;
import org.sluckframework.demo.member.event.MemberInfoUpdateEvent;

/**
 * 用户
 *
 * Author: sunxy
 * Created: 2015-10-08 22:54
 * Since: 1.0
 */
public class Member extends AbstractAnnotatedAggregateRoot<MemberID> {

    @AggregateIdentifier
    private MemberID memberID;

    private String name;
    private String idCard;

    private Member(){}

    @CommandHandler
    public Member(CreatedMemberCommand createdMemberCommand) {
        apply(new MemberCreatedEvent(createdMemberCommand.getMemberID(), createdMemberCommand.getName(),
                createdMemberCommand.getIdCard()));
    }

    @CommandHandler
    public void updateInfo(UpdateMemberInfoCommand updateMemberInfoCommand) {
        apply(new MemberInfoUpdateEvent(updateMemberInfoCommand.getNewName(),
                updateMemberInfoCommand.getNewIdCard()));
    }

    @EventSourcingHandler
    public void updateMemberInfo(MemberInfoUpdateEvent updateInfoEvent) {
        this.name = updateInfoEvent.getNewName();
        System.out.println("new name" + updateInfoEvent.getNewName());
        this.idCard = updateInfoEvent.getNewIdCard();
        System.out.println("new idCard" + updateInfoEvent.getNewIdCard());
    }

    @EventSourcingHandler
    public void createdMember(MemberCreatedEvent createdEvent) {
        memberID = createdEvent.getMemberID();
        this.name = createdEvent.getName();
        this.idCard = createdEvent.getIdCard();
    }
}
