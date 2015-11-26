package org.sluck.test.netty;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;

/**
 * Author: sunxy
 * Created: 2015-11-24 00:06
 * Since: 1.0
 */
public class NioSocketTest {

    public static void main(String[] args) throws IOException {

        //this si nio 1.0 api
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.bind(new InetSocketAddress(InetAddress.getLocalHost(), 8080));
        serverSocketChannel.configureBlocking(false);

        Selector selector = Selector.open();

        selector.select(1000);
        selector.selectedKeys();

        System.out.println("end");

        //nio 2.0 si based event driven and didn't need selector



    }
}
