package server;

import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.*;
import java.io.*;

import utils.Fix;
import database.Database;

public class Server implements Runnable{
    private int id;
    private Socket s;
    private int port;
    private Map<Integer, Socket> routing_table;
    private Map<Integer, Integer> client_type;
    private static List<String> msg_pending;
    private static List<Integer> diconnected_broker;
    private Database db;
    private BufferedReader bf;
    private PrintStream ps;

    public Server(Socket s, Map<Integer, Socket> rt, Map<Integer, Integer> ct, List<String> mp, List<Integer> dis, int id, Database db){
        this.s = s;
        this.port = s.getLocalPort();
        this.routing_table = rt;
        this.client_type = ct;
        this.msg_pending = mp;
        this.diconnected_broker = dis;
        this.id = id;
        this.db = db;
        try{
            bf = new BufferedReader(new InputStreamReader(s.getInputStream()));
            ps = new PrintStream(s.getOutputStream());
        }catch(Exception e){
            System.out.println(e.getMessage());
        }
    }

    //***********************************************************************
    //***********************************************************************
    //RUN
    //***********************************************************************
    //***********************************************************************

    private void setGoodClient() throws Exception{
        int type = client_type.get(id); //5000 if broker

        if (type == 5000){
            int desired_id = Integer.parseInt(bf.readLine()); // desired id
            if (diconnected_broker.contains(desired_id)){
                routing_table.remove(id);
                client_type.remove(id);
                routing_table.put(desired_id, s);
                client_type.put(desired_id, type);
                id = desired_id;
            }
        }
        advert();
        ps.println(id);
    }

    //disconnect
    private void disconectClient() throws Exception{
        int type = client_type.get(id);

        routing_table.remove(id);
        client_type.remove(id);
        if(type == 5000){
            System.out.println("Added into disconnected broker");
            diconnected_broker.add(id);
        }
        s.close();
        System.out.println("Client " + id + " disconnected");
    }

    //LISTENING
    public void run(){
        try {
            //SetClient
            setGoodClient();
            //start chatting
            while(true){
                //getMessage
                String msg = bf.readLine();
                System.out.println(msg);
                //if the client is done return
                if (msg == null){
                    disconectClient();
                    return ;
                }
                //verify on checkSum;
                if (Fix.validateCheckSum(msg) == true){
                    //get the TargetSocket;
                    Socket target = getTargetSocket(msg);
                    //insert into db
                    db.insertTransaction(msg);
                    if (target != null && valideTarget(target)){
                        //sendMessage;
                        PrintStream tmp_ps = new PrintStream(target.getOutputStream());
                        tmp_ps.println(msg);
                    }else{
                        System.out.println("Target not found");
                        ps.println("NF");
                    }
                }else{
                    System.out.println("Unvalide checksum");
                    ps.println("NF");
                }
            }
        }catch(Exception e){
            try{
                disconectClient();
            }catch(Exception ee){}
            System.out.println("Exception occured : " + e.getMessage());
        }
    }

    //***********************************************************************
    //***********************************************************************
    //UTILS
    //***********************************************************************
    //***********************************************************************

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

    private boolean valideTarget(Socket target){
        return (s.getPort() != target.getPort());
    }

    private void advert(){
        System.out.println("Client " + id + " connected");
    }
}