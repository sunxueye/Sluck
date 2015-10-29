package org.sluckframework.demo.test.member;


import org.sluckframework.cqrs.commandhandling.gateway.CommandGateway;
import org.sluckframework.demo.member.MemberID;
import org.sluckframework.demo.member.MerchantID;
import org.sluckframework.demo.member.command.CreateMerchantCommand;


public class CommandGenerator {

    public static void sendCommands(CommandGateway commandGateway) {
//        final DefaultIdentifier itemId1 = new DefaultIdentifier();
//        final DefaultIdentifier itemId2 = new DefaultIdentifier();
        final MemberID memberID = new MemberID();
        final MerchantID merhantID = new MerchantID();
        System.out.println(memberID.getIdentifier());
//        commandGateway.sendAndWait(new CreatedMemberCommand(memberID, "sunxy2", "12345"));
//        commandGateway.sendAndWait(new CreateToDoItemCommand(itemId2, "Think about the next steps!"));
        commandGateway.sendAndWait(new CreateMerchantCommand(merhantID, memberID, "s1", "234", "s2"));
//        commandGateway.sendAndWait(new MarkCompletedCommand(itemId1));
    }
}
