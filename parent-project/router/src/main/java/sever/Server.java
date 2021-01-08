package server;

import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.*;
import java.io.*;

public class Server implements Runnable{
    private int id;
    private Socket s;
    private int port;
    private Map<Integer, Socket> routing_table;

    public Server(Socket s, Map<Integer, Socket> rt, int id){
        this.s = s;
        this.port = s.getLocalPort();
        this.routing_table = rt;
        this.id = id;
    }

    public void run(){
        try {
            BufferedReader bf = new BufferedReader(new InputStreamReader(s.getInputStream()));
            PrintStream ps = new PrintStream(s.getOutputStream());

            //Communicate the Id
            ps.println(id);
        
            while(true){
            //getMessage
            String msg = bf.readLine();
            System.out.println(msg);
            //verify on checkSum;
            //get the TargetSocket;
            //sendMessage;
            }
        }catch(Exception e){
            System.out.println(e.getMessage());
        }
    }

    private Socket getTargetSocket(int target){
        Socket tmp = null;

        tmp = routing_table.get(target);
        return (tmp);
    }
}