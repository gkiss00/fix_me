package main;

import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.*;

public class Router{

    private static void listen(Selector selector, ServerSocketChannel serverSocketChannel) throws Exception{
        SelectionKey key = null;
        while(true){
            if(selector.select() <= 0)
                continue;
            Set<SelectionKey> selectedKeys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = selectedKeys.iterator();
            while (iterator.hasNext()) {
                key = (SelectionKey) iterator.next();
                iterator.remove();
                if (key.isAcceptable()) {
                    SocketChannel sc = serverSocketChannel.accept();
                    sc.configureBlocking(false);
                    sc.register(selector, SelectionKey.OP_READ);
                    System.out.println("Connection Accepted: " + sc.getLocalAddress() + "\n");
                }
                if (key.isReadable()) {
                    SocketChannel sc = (SocketChannel) key.channel();
                    ByteBuffer bb = ByteBuffer.allocate(1024);
                    sc.read(bb);
                    String result = new String(bb.array()).trim();
                    System.out.println("Message received: " + result + " Message length= " + result.length());
                    if (result.length() <= 0) {
                        sc.close();
                        System.out.println("Connection closed...");
                        System.out.println("Server will keep running. " + "Try running another client to " + "re-establish connection");
                    }
                }
            }
        }
    }

    public static void main(String[] args){
        System.out.println("Router");
        try{
            //CRETAE A NON BLOCKING SOCKET FOR THE SERVER
            Selector selector = Selector.open();
            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.configureBlocking(false);
            //CONFIGURE IP AND PORT FOR BROKERS
            InetAddress ip = InetAddress.getByName("localhost");
            serverSocketChannel.bind(new InetSocketAddress(ip, 5000));
            //REGISTER THE SELECTOR IN ACCEPT CONNEXION MODE
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
            listen(selector, serverSocketChannel);
        }catch(Exception e){
            System.out.println(e.getMessage());
        }

    }
}