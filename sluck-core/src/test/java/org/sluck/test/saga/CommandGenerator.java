package org.sluck.test.saga;


import org.sluckframework.cqrs.commandhandling.gateway.CommandGateway;
import org.sluckframework.domain.identifier.DefaultIdentifier;


public class CommandGenerator {

    public static void sendCommands(CommandGateway commandGateway) {
        final DefaultIdentifier itemId1 = new DefaultIdentifier();
        final DefaultIdentifier itemId2 = new DefaultIdentifier();
        commandGateway.sendAndWait(new CreateToDoItemCommand(itemId1, "Check if it really works!"));
        commandGateway.sendAndWait(new CreateToDoItemCommand(itemId2, "Think about the next steps!"));
        commandGateway.sendAndWait(new MarkCompletedCommand(itemId1));
    }
}
