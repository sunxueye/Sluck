package org.sluckframework.demo.member.account.event;

import org.sluckframework.demo.member.MemberID;
import org.sluckframework.demo.member.account.AccountID;

/**
 * Author: sunxy
 * Created: 2015-10-11 00:36
 * Since: 1.0
 */
public class CreateAccountEvent {
    private AccountID accountID;
    private MemberID ofMember;

    public CreateAccountEvent(AccountID accountID, MemberID ofMember) {
        this.accountID = accountID;
        this.ofMember = ofMember;
    }

    public MemberID getOfMember() {
        return ofMember;
    }

    public AccountID getAccountID() {
        return accountID;
    }
}
