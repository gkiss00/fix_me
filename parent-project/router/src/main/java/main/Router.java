package main;

import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.*;
import java.util.concurrent.*;

import server.Server;
import database.Database;

public class Router{
    private static int id = 100000;
    private static Database db = new Database();
    private static Map<Integer, Socket> routing_table = new HashMap<Integer, Socket>();
    private static Map<Integer, Integer> client_type = new HashMap<Integer, Integer>();
    private static ExecutorService executor_service = Executors.newFixedThreadPool(10);
    private static List<Server> server_list = new ArrayList<Server>();

    //***********************************************************************
    //***********************************************************************
    //UTILS
    //***********************************************************************
    //***********************************************************************

    private static int getNextId(){
        ++id;
        return id;
    }

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

    private static void advert(){
        System.out.println("Client " + id + " connected");
    }

    //***********************************************************************
    //***********************************************************************
    //START LISTENING FOR CONNECTION
    //***********************************************************************
    //***********************************************************************

    private static void register(Socket socket, int port, int nextId){
        int type = (port == 5000) ? (5000) : (5001);

        client_type.put(nextId, type);
        routing_table.put(nextId, socket);
        advert();
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
                //remove the key from the list
                SelectionKey sk = it.next();
                it.remove();
                //get the serverSocket
                ServerSocketChannel ssc = (ServerSocketChannel)sk.channel();
                int port = ssc.socket().getLocalPort();
                //Accept the entring connexion
                Socket sock = ssc.socket().accept();
                //Register the sockect into the routing table
                int nextId = getNextId();
                register(sock, port, nextId);
                //START A SERVER WITH THE SOCKET...
                Server server = new Server(sock, routing_table, client_type, nextId, db);
                server_list.add(server);
                executor_service.submit(server);
            }
        }
    }

    //***********************************************************************
    //***********************************************************************
    //MAIN
    //***********************************************************************
    //***********************************************************************

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