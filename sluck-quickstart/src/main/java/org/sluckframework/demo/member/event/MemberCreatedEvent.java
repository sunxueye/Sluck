package org.sluckframework.demo.member.event;

import org.sluckframework.demo.member.MemberID;

/**
 * 用户被创建事件
 *
 * Author: sunxy
 * Created: 2015-10-08 23:08
 * Since: 1.0
 */
public class MemberCreatedEvent {

    private MemberID memberID;

    private String name;
    private String idCard;

    private MemberCreatedEvent() {
    }

    public MemberCreatedEvent(MemberID memberID, String name, String idCard) {
        this.memberID = memberID;
        this.name = name;
        this.idCard = idCard;
    }

    public MemberID getMemberID() {
        return memberID;
    }

    public String getName() {
        return name;
    }

    public String getIdCard() {
        return idCard;
    }
}
