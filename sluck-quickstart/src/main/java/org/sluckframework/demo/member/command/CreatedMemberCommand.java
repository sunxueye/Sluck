package org.sluckframework.demo.member.command;

import org.sluckframework.demo.member.MemberID;
import org.sluckframework.demo.member.MerchantID;

/**
 * Author: sunxy
 * Created: 2015-10-09 23:45
 * Since: 1.0
 */
public class CreatedMemberCommand {

    private MerchantID merchantID;

    private MemberID memberID;
    private String name;
    private String idCard;

    public CreatedMemberCommand(MemberID memberID, String name, String idCard) {
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

    public void setMerchantID(MerchantID merchantID) {
        this.merchantID = merchantID;
    }

    public MerchantID getMerchantID() {
        return merchantID;
    }
}
