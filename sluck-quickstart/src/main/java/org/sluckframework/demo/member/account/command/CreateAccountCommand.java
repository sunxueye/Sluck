package org.sluckframework.demo.member.account.command;

import org.sluckframework.demo.member.MemberID;
import org.sluckframework.demo.member.account.AccountID;

/**
 * Author: sunxy
 * Created: 2015-10-11 00:41
 * Since: 1.0
 */
public class CreateAccountCommand {
    private AccountID accountID;
    private MemberID memberID;

    public CreateAccountCommand(AccountID accountID, MemberID memberID) {
        this.accountID = accountID;
        this.memberID = memberID;
    }

    public AccountID getAccountID() {
        return accountID;
    }

    public MemberID getMemberID() {
        return memberID;
    }
}
