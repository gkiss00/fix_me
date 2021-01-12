package main;

import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.*;
import java.io.*;

import database.Database;
import model.Instrument;
import dto.DataToObject;
import utils.Fix;

public class Broker {
    private static Socket s;
    private static BufferedReader bf;
    private static PrintStream ps;
    private static int Id;
    private static List<Instrument> all_instruments;
    private static Map<Integer, Integer> my_instruments = new HashMap<>();
    private static int wallet = 2000;

    //SET STARTER INSTUMENTS
    private static void setMyInstruments(Database db) throws Exception{
        Random rand = new Random();
        //Get all instruments
        all_instruments = DataToObject.getAllInstruments(db.getAllInstruments());
        //set my_instruments
        for (int i = 0; i < 5; ++i){
            int pos = rand.nextInt(all_instruments.size());
            my_instruments.put(all_instruments.get(pos).getId(), rand.nextInt(3) + 1);
        }
    }

    private static int getPrice(int id){
        for (int i = 0; i < all_instruments.size(); ++i){
            if (all_instruments.get(i).getId() == id){
                return (all_instruments.get(i).getPrice());
            }
        }
        return (-1);
    }

    //VALIDATE MSGTYPE
    private static boolean validateMsgType(String msgtype){
        String tmp = msgtype.toUpperCase();
        return (tmp.compareTo("BUY") == 0 || tmp.compareTo("SELL") == 0);
    }

    //VALIDATE THE INPUT
    private static boolean validateInput(String args[]){
        if (args.length != 4)
            return false;
        if (validateMsgType(args[0]) == false)
            return false;
        try {
            int test = Integer.parseInt(args[1]);
            test = Integer.parseInt(args[2]);
            if (getPrice(test) < 0)
                return false;
            test = Integer.parseInt(args[3]);
            return true;
        }catch(Exception e){
            return false;
        }
    }

    //CAN HE DO IT ??
    private static boolean canHeTrade(String args[]){
        String tmp = args[0].toUpperCase();
        int instrumentId = Integer.parseInt(args[2]);
        int qty = Integer.parseInt(args[2]);
        int price = getPrice(instrumentId);

        Integer instrument = my_instruments.get(instrumentId);

        //check if instrument exists
        if (price < 0)
            return false;
        if (tmp.compareTo("BUY") == 0){
            if (price * qty > wallet)
                return false;
        }else{
            if (instrument == null)
                return false;
            if (qty > instrument)
                return false;
        }
        return (true);
    }

    private static void getResponse(String action, String args[]) throws Exception{
        String ans;

        //if server is done exit
        ans = bf.readLine();
        if (ans == null){
            System.out.println("Server closed");
            s.close();
            System.exit(1);
        }
        if(ans.compareTo("NF") == 0)
            return ;
        String msgType = Fix.getValueByTag(35, ans);
        int instruId = Integer.parseInt(args[2]);
        int qty = Integer.parseInt(args[3]);

        Integer instru = my_instruments.get(instruId);

        if (msgType.compareTo("Rejected") == 0)
            return ;
        //update stock
        if (action.compareTo("SELL") == 0){
            if (instru == qty)
                my_instruments.remove(instruId);
            else
                my_instruments.put(instruId, instru - qty);
            wallet += (qty * getPrice(instruId));
        }else{
            if (instru != null)
                my_instruments.put(instruId, instru + qty);
            else
                my_instruments.put(instruId, qty);
            wallet -= (qty * getPrice(instruId));
        }
    }

    private static void startFixing()throws Exception{
        Scanner scan = new Scanner(System.in);
        
        //get your id
        Id = Integer.parseInt(bf.readLine());
        //get some info
        System.out.println("Id : " + Id);
        System.out.println("Your stock : " + my_instruments);
        System.out.println("Wallet : " + wallet);
        //start fixing
        while(true){
            //get input
            String msg = scan.nextLine();
            String args[] = msg.split("\\s+");
            if(validateInput(args)){
                //to fix
                String fix_msg = Fix.stringToFix(args[0].toUpperCase(), Id, Integer.parseInt(args[1]), 
                                Integer.parseInt(args[2]), Integer.parseInt(args[3]), getPrice(Integer.parseInt(args[2])));
                ps.println(fix_msg);
                //recieve response
                getResponse(args[0].toUpperCase(), args);
                //show new stock
                System.out.println("New stock : " + my_instruments);
                System.out.println("Wallet : " + wallet);
            }else{
                System.out.println("Unvalide message");
            }
        }
    }

    public static void main(String[] args){
        System.out.println("Broker");
        try{
            //Get the connexion with the database
            Database db = new Database();
            //set my starting stuff
            setMyInstruments(db);
            //Connect to the router
            s = new Socket("localhost", 5000);
            bf = new BufferedReader(new InputStreamReader(s.getInputStream()));
            ps = new PrintStream(s.getOutputStream());
            //InetSocketAddress addr = new InetSocketAddress(InetAddress.getByName("localhost"), 5000);
            startFixing();
        }catch(Exception e){
            System.out.println("Exception : " + e.getMessage());
        }
    }
}