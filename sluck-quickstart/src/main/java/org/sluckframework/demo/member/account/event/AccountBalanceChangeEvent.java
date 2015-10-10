package org.sluckframework.demo.member.account.event;

import org.sluckframework.demo.member.account.ChangeType;

/**
 * Author: sunxy
 * Created: 2015-10-11 00:44
 * Since: 1.0
 */
public class AccountBalanceChangeEvent {
    private Double changeAmount;
    private ChangeType changeType;

    public AccountBalanceChangeEvent(Double changeAmount, ChangeType changeType) {
        this.changeAmount = changeAmount;
        this.changeType = changeType;
    }

    public Double getChangeAmount() {
        return changeAmount;
    }

    public ChangeType getChangeType() {
        return changeType;
    }
}
