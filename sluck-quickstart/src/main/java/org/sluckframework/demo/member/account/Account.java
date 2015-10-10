package org.sluckframework.demo.member.account;

import org.sluckframework.cqrs.commandhandling.annotation.CommandHandler;
import org.sluckframework.cqrs.eventsourcing.annotation.AbstractAnnotatedAggregateRoot;
import org.sluckframework.cqrs.eventsourcing.annotation.AggregateIdentifier;
import org.sluckframework.cqrs.eventsourcing.annotation.EventSourcingHandler;
import org.sluckframework.demo.member.MemberID;
import org.sluckframework.demo.member.account.command.CreateAccountCommand;
import org.sluckframework.demo.member.account.event.AccountBalanceChangeEvent;
import org.sluckframework.demo.member.account.event.CreateAccountEvent;
import org.sluckframework.demo.member.account.exception.BalanceNotEnoughException;

/**
 * 账户
 *
 * Author: sunxy
 * Created: 2015-10-11 00:32
 * Since: 1.0
 */
public class Account extends AbstractAnnotatedAggregateRoot<AccountID> {
    @AggregateIdentifier
    private AccountID accountID;

    private MemberID memberID;

    private Double balance;

    private Account(){}

    @CommandHandler
    public Account(CreateAccountCommand createAccountCommand) {
        apply(new CreateAccountEvent(createAccountCommand.getAccountID(), createAccountCommand.getMemberID()));
    }

    @EventSourcingHandler
    public void hanldeCreateAccount(CreateAccountEvent createAccountEvent) {
        this.accountID = createAccountEvent.getAccountID();
        this.memberID = createAccountEvent.getOfMember();
        this.balance = 0D;
    }

    @EventSourcingHandler
    public void accountChange(AccountBalanceChangeEvent accountBalanceChangeEvent) {
        if (ChangeType.Add.equals(accountBalanceChangeEvent.getChangeType())) {
            balance = balance + accountBalanceChangeEvent.getChangeAmount();
        } else {
            if (balance.compareTo(accountBalanceChangeEvent.getChangeAmount()) < 0)
                throw new BalanceNotEnoughException("账户余额不足,余额:" + balance);
            balance = balance - accountBalanceChangeEvent.getChangeAmount();
        }
    }
}
