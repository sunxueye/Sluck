package org.sluckframework.demo.member.event;

/**
 * 用户信息修改
 *
 * Author: sunxy
 * Created: 2015-10-08 23:13
 * Since: 1.0
 */
public class MemberInfoUpdateEvent {

    private String newName;
    private String newIdCard;

    public MemberInfoUpdateEvent(String newName, String newIdCard) {
        this.newName = newName;
        this.newIdCard = newIdCard;
    }

    public String getNewName() {
        return newName;
    }

    public String getNewIdCard() {
        return newIdCard;
    }
}
