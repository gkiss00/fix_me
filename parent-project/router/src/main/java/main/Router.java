package main;

import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.*;
import java.util.concurrent.*;

import server.Server;

public class Router{
    private static int id = 100000;
    private static Map<Integer, Socket> routing_table = new HashMap<Integer, Socket>();
    private static ExecutorService executor_service = Executors.newFixedThreadPool(10);
    private static List<Server> server_list = new ArrayList<Server>();

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

    private static void advert(Socket socket){
        if (socket.getPort() == 5000){
            System.out.println("Broker " + id + " connected");
        }else{
            System.out.println("Market " + id + " connected");
        }
    }

    //START LISTENING FOR ANY CHANGE
    private static void startServing(Selector selector) throws Exception{
        System.out.println("Waiting for connexion");
        while(true){
            selector.select();
            //if someone try to connect
            Iterator<SelectionKey> it = selector.selectedKeys().iterator();
            //foreach connexion
            while(it.hasNext()){
                SelectionKey sk = it.next();
                it.remove();

                ServerSocketChannel ssc = (ServerSocketChannel)sk.channel();
                //Accept the entring connexion
                Socket sock = ssc.socket().accept();
                advert(sock);
                //Register the sockect into the routing table
                int tmp = getNextId();
                routing_table.put(tmp, sock);
                //START A SERVER WITH THE SOCKET...
                Server server = new Server(sock, routing_table, tmp);
                server_list.add(server);
                executor_service.submit(server);
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

    private static int getNextId(){
        ++id;
        return id;
    }
}