package org.sluckframework.cqrs.eventhanding.scheduling.java;

import org.sluckframework.cqrs.eventhanding.scheduling.ScheduleToken;

/**
 * token简单实现
 *
 * Author: sunxy
 * Created: 2015-09-17 22:50
 * Since: 1.0
 */
public class SimpleScheduleToken implements ScheduleToken {

    private static final long serialVersionUID = -8118223354702247016L;
    private final String tokenId;

    public SimpleScheduleToken(String tokenId) {
        this.tokenId = tokenId;
    }

    public String getTokenId() {
        return tokenId;
    }
}
