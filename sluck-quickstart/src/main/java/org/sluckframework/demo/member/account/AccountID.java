package org.sluckframework.demo.member.account;

import org.sluckframework.domain.identifier.DefaultIdentifier;

/**
 * Author: sunxy
 * Created: 2015-10-11 00:33
 * Since: 1.0
 */
public class AccountID extends DefaultIdentifier{
    public AccountID() {
    }

    public AccountID(String identifier) {
        super(identifier);
    }
}
