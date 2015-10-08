package org.sluckframework.demo.member;

import org.sluckframework.domain.identifier.DefaultIdentifier;

/**
 * 用户id
 *
 * Author: sunxy
 * Created: 2015-10-08 22:57
 * Since: 1.0
 */
public class MemberID extends DefaultIdentifier {
    @Override
    public String toString() {
        return "member identifier, id :" + super.toString();
    }
}
