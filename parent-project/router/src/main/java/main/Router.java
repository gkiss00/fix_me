package main;

import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.*;

public class Router{

    //RETURN A SERVER SOCKET CHANNEL CONFIGURED NON BLOCKING
    private static ServerSocketChannel createServerSocketChannel(Selector s, int port){
        try{
            //NEW SERVER SOCKET
            ServerSocketChannel ssc = ServerSocketChannel.open();
            ServerSocket ss = ssc.socket();
            
            //CONFIGURE NON BLOCKING
            ssc.configureBlocking(false);
            //OPEN ON PORT <SELECTED>
            ss.bind(new InetSocketAddress(port));
            //REGISTER TO THE SELECTOR
            ssc.register(s, ssc.validOps()); //SelectionKey.OP_ACCEPT
            return (ssc);
        }catch(Exception e){
            e.printStackTrace();
            System.exit(1);
        }
        return (null);
    }

    //START LISTENING FOR ANY CHANGE
    private static void startServing(Selector selector) throws Exception{
        while(true){
            System.out.println("Waiting for connexion");
            selector.select();
            Iterator<SelectionKey> it = selector.selectedKeys().iterator();
            while(it.hasNext()){
                SelectionKey sk = it.next();
                it.remove();

                ServerSocketChannel ssc = (ServerSocketChannel)sk.channel();

                Socket sock = ssc.socket().accept();
            }
        }
    }

    public static void main(String[] args){
        System.out.println("Router");
        try{
            //CREATE A SELECTOR
            Selector selector = Selector.open();
            //CREATE A SERVER SOCKET FOR THE BROKER
            ServerSocketChannel sscBroker = createServerSocketChannel(selector, 5000);
            //CREATE A SERVER SOCKET FOR THE MARKET
            ServerSocketChannel sscMarket = createServerSocketChannel(selector, 5001);
            startServing(selector);
        }catch(Exception e){
            System.out.println(e.getMessage());
        }

    }
}