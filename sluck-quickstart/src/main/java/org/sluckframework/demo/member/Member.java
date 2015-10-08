package org.sluckframework.demo.member;

import org.sluckframework.cqrs.commandhandling.annotation.CommandHandler;
import org.sluckframework.cqrs.eventsourcing.annotation.AbstractAnnotatedAggregateRoot;
import org.sluckframework.cqrs.eventsourcing.annotation.AggregateIdentifier;
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

    @CommandHandler
    public Member(MemberCreatedEvent createdMemberEvent) {
        memberID = new MemberID();
        this.name = createdMemberEvent.getName();
        this.idCard = createdMemberEvent.getIdCard();
    }

    @CommandHandler
    public void updateMemberInfo(MemberInfoUpdateEvent updateInfoEvent) {
        this.name = updateInfoEvent.getNewName();
        this.idCard = updateInfoEvent.getNewIdCard();
    }
}
