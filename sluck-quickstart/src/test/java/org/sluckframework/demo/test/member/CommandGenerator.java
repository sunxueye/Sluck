package org.sluckframework.demo.test.member;


import org.sluckframework.cqrs.commandhandling.gateway.CommandGateway;
import org.sluckframework.demo.member.MemberID;
import org.sluckframework.demo.member.command.UpdateMemberInfoCommand;


public class CommandGenerator {

    public static void sendCommands(CommandGateway commandGateway) {
//        final DefaultIdentifier itemId1 = new DefaultIdentifier();
//        final DefaultIdentifier itemId2 = new DefaultIdentifier();
        final MemberID memberID = new MemberID("993ffc20-6fd6-11e5-b87f-0a3f44bb4c19");
        System.out.println(memberID.getIdentifier());
        commandGateway.sendAndWait(new UpdateMemberInfoCommand(memberID, "sunxy2", "12345"));
//        commandGateway.sendAndWait(new CreateToDoItemCommand(itemId2, "Think about the next steps!"));
//        commandGateway.sendAndWait(new MarkCompletedCommand(itemId1));
    }
}
