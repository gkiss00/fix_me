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
    private int type;
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
        this.type = client_type.get(id);
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

    private void sendPendingMessage(){
        for (int i = 0; i < msg_pending.size(); ++i){
            String tmp = msg_pending.get(i);
            int targetId = Integer.parseInt(Fix.getValueByTag(56, tmp));
            if (targetId == id){
                ps.println(tmp);
                break;
            }
        }
    }

    //set good id
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
            //check if pending
            sendPendingMessage();
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
                    //insert into db
                    db.insertTransaction(msg);
                    //if I am broker
                    if(type == 5000){
                        //if target exist and is market
                        Socket target = getTargetSocket(msg);
                        if (target != null && isTargetMarket(msg) == true){
                            PrintStream tmp_ps = new PrintStream(target.getOutputStream());
                            tmp_ps.println(msg);
                        }else{
                            System.out.println("Target not found");
                            ps.println("NF");
                        }
                    //if I am market
                    }else{
                        Socket target = getTargetSocket(msg);
                        if (target != null){
                            PrintStream tmp_ps = new PrintStream(target.getOutputStream());
                            tmp_ps.println(msg);
                        }else{
                            msg_pending.add(msg);
                        }
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

    private boolean isTargetMarket(String msg){
        int targetId = Integer.parseInt(Fix.getValueByTag(56, msg));
        return (client_type.get(targetId) == 5001);
    }

    private void advert(){
        System.out.println("Client " + id + " connected");
    }
}