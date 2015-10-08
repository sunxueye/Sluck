package org.sluckframework.demo.member.event;

/**
 * 用户被创建事件
 *
 * Author: sunxy
 * Created: 2015-10-08 23:08
 * Since: 1.0
 */
public class MemberCreatedEvent {

    private String name;
    private String idCard;

    private MemberCreatedEvent() {
    }

    public MemberCreatedEvent(String name, String idCard) {
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
