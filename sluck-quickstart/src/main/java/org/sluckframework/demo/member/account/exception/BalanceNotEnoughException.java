package org.sluckframework.demo.member.account.exception;

/**
 * Author: sunxy
 * Created: 2015-10-11 00:51
 * Since: 1.0
 */
public class BalanceNotEnoughException extends RuntimeException {
    public BalanceNotEnoughException(String s) {
        super(s);
    }
}
