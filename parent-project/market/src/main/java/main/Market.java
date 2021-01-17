package main;

import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.*;

import database.Database;
import model.Instrument;
import dto.DataToObject;
import utils.Fix;

public class Market {
    private static Socket s;
    private static PrintStream ps;
    private static int Id;
    private static List<Instrument> all_instruments;
    private static Map<Integer, Integer> my_instruments = new HashMap<>();
    private static int wallet = 5000;

    private static void setMyInstruments(Database db) throws Exception{
        Random rand = new Random();
        //Get all instruments
        all_instruments = DataToObject.getAllInstruments(db.getAllInstruments());
        //set my_instruments
        for (int i = 0; i < 10; ++i){
            int pos = rand.nextInt(all_instruments.size());
            my_instruments.put(all_instruments.get(pos).getId(), rand.nextInt(3) + 1);
        }

    }

    //Check if the market can Sell his instruments
    private static boolean canSell(String msg){
        int instrumentId = Integer.parseInt(Fix.getValueByTag(58, msg));
        int qty = Integer.parseInt(Fix.getValueByTag(53, msg));
        int price = Integer.parseInt(Fix.getValueByTag(44, msg));
        Integer instrument = my_instruments.get(instrumentId);

        if (instrument == null)
            return (false);
        if (instrument < qty)
            return (false);
        //update stock
        if (instrument - qty == 0)
            my_instruments.remove(instrumentId);
        else
            my_instruments.put(instrumentId, instrument - qty);
        wallet += qty * price;
        return (true);
    }

    //Check if the market can Buy some instruments
    private static boolean canBuy(String msg){
        int instrumentId = Integer.parseInt(Fix.getValueByTag(58, msg));
        int qty = Integer.parseInt(Fix.getValueByTag(53, msg));
        int price = Integer.parseInt(Fix.getValueByTag(44, msg));

        if((price * qty) <= wallet){
            //update stock
            Integer tmp = my_instruments.get(instrumentId);
            if (tmp != null)
                qty += tmp;
            my_instruments.put(instrumentId, qty);
            wallet -= (price * qty);
            return (true);
        }
        return (false);
    }

    //Check for the trade
    private static boolean isTradePossible(String msg){
        String messageType = Fix.getValueByTag(35, msg);
        if (messageType.compareTo("SELL") == 0)
            return canBuy(msg);
        else
            return canSell(msg);
    }

    private static void sendResponse(String msgType, String msg)throws Exception{
        int targetId = Integer.parseInt(Fix.getValueByTag(49, msg));
        int instrumentId = Integer.parseInt(Fix.getValueByTag(58, msg));
        int qty = Integer.parseInt(Fix.getValueByTag(53, msg));
        int price = Integer.parseInt(Fix.getValueByTag(44, msg));
        String fix_message;

        fix_message = Fix.stringToFix(msgType, Id, targetId, instrumentId, qty, price);
        ps.println(fix_message);
    }

    private static void startFixing()throws Exception{
        Scanner scan = new Scanner(System.in);
        BufferedReader bf = new BufferedReader(new InputStreamReader(s.getInputStream()));

        //get your id
        Id = Integer.parseInt(bf.readLine());
        //Show the infos
        System.out.println("Id : " + Id);
        System.out.println("Market stock : " + my_instruments);
        System.out.println("Wallet : " + wallet);
        while(true){
            String msgType;
            //listen for msg
            String msg = bf.readLine();
            //if server is done exit
            if (msg == null){
                System.out.println("Server closed");
                s.close();
                System.exit(1);
            }
            Thread.sleep(2000);
            //check if possible
            if (msg != null){
                if(isTradePossible(msg) == true)
                    msgType = "Exeuted";
                else
                    msgType = "Rejected";
                //send response
                sendResponse(msgType, msg);
                //Show the update
                System.out.println("Trade : " + msgType);
                System.out.println("New stock : " + my_instruments);
                System.out.println("Wallet : " + wallet);
            }
        }
    }

    public static void main(String[] args){
        System.out.println("Market");
        try{
            //Get the connexion with the database
            Database db = new Database();
            //set my starting stuff
            setMyInstruments(db);
            //Connect to the router
            s = new Socket("localhost", 5001);
            ps = new PrintStream(s.getOutputStream());
            //InetSocketAddress addr = new InetSocketAddress(InetAddress.getByName("localhost"), 5001);
            startFixing();
        }catch(Exception e){
            System.out.println("Exception : " + e.getMessage());
        }
    }
}