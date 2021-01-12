package server;

import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.*;
import java.io.*;

import utils.Fix;

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
            //start chatting
            while(true){
                //If the socket closed
                if (bf.ready() == false){
                    s.close();
                    routing_table.remove(id);
                    return ;
                }
                //getMessage
                String msg = bf.readLine();
                System.out.println(msg);
                System.out.println("S closed ; " + s.isClosed());
                System.out.println("Bf ready : " + bf.ready());
                //verify on checkSum;
                if (msg != null && Fix.validateCheckSum(msg) == true){
                    //get the TargetSocket;
                    Socket target = getTargetSocket(msg);
                    if (target != null){
                        //sendMessage;
                        PrintStream tmp_ps = new PrintStream(target.getOutputStream());
                        tmp_ps.println(msg);
                        //tmp_ps.close();
                    }else{
                        System.out.println("Target not found");
                        ps.println("NF");
                    }
                }else{
                    System.out.println("Unvalide checksum");
                }
            }
        }catch(Exception e){
            System.out.println(e.getMessage());
        }
    }

    private Socket getTargetSocket(String msg){
        Socket tmp = null;
        String value = Fix.getValueByTag(56, msg);
        int target;

        if (value == null)
            return null;
        target = Integer.parseInt(value);
        tmp = routing_table.get(target);
        return (tmp);
    }
}