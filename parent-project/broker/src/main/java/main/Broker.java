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

    //***********************************************************************
    //***********************************************************************
    //UTILS
    //***********************************************************************
    //***********************************************************************

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

    //get price of an intrument -1 if doesn't exist
    private static int getPrice(int id){
        for (int i = 0; i < all_instruments.size(); ++i){
            if (all_instruments.get(i).getId() == id){
                return (all_instruments.get(i).getPrice());
            }
        }
        return (-1);
    }

    private static boolean badRequest(String msg){
        System.out.println(msg);
        return (false);
    }

    
    //***********************************************************************
    //***********************************************************************
    //VALIDATION
    //***********************************************************************
    //***********************************************************************

    //VALIDATE THE INPUT
    private static boolean validateInput(String args[]){
        //must have 4 args
        if (args.length != 4)
            return badRequest("Your message must contain 4 arguments");
        //must be SELL or BUY
        if (validateMsgType(args[0]) == false)
            return badRequest("Your messageType must be sell or buy");
        //must be 3 numbers
        try {
            //target id
            int test = Integer.parseInt(args[1]);
            //intrument id
            test = Integer.parseInt(args[2]);
            //qty
            test = Integer.parseInt(args[3]);
            //check if instrument exist
            if (getPrice(Integer.parseInt(args[2])) < 0)
                return badRequest("The instrument you ar trying to sell/buy doesn't exist");
            //check if he can trade
            if (canHeTrade(args) == false)
                return false;
            return true;
        }catch(Exception e){
            return badRequest("Your message must contain 3 numbers");
        }
    }

    //VALIDATE MSGTYPE
    private static boolean validateMsgType(String msgtype){
        String tmp = msgtype.toUpperCase();
        return (tmp.compareTo("BUY") == 0 || tmp.compareTo("SELL") == 0);
    }

    //CAN HE DO IT ??
    private static boolean canHeTrade(String args[]){
        //get the action type
        String action = args[0].toUpperCase();
        //get the instument to trade
        int instrumentId = Integer.parseInt(args[2]);
        //get qty
        int qty = Integer.parseInt(args[3]);
        //get price
        int price = getPrice(instrumentId);

        //if u try to sell
        if(action.compareTo("SELL") == 0){
            //check if u get the instument in your inventory
            Integer instrument = my_instruments.get(instrumentId);
            if (instrument == null)
                return badRequest("You don't own this instrument");
            //check if u get enough to sell
            if (qty > instrument)
                return badRequest("You don't own enought of this instrument");
            return (true);
        //if u try to buy
        }else{
            if (price * qty > wallet)
                return badRequest("You don't get enought money to do this");
            return (true);
        }
    }

    //***********************************************************************
    //***********************************************************************
    //GET RESPONSE
    //***********************************************************************
    //***********************************************************************

    private static void getResponse(String action, String args[]) throws Exception{
        String ans;

        ans = bf.readLine();
        //if server is done exit
        if (ans == null){
            System.out.println("Server closed");
            s.close();
            System.exit(1);
        }
        //if target no found
        if(ans.compareTo("NF") == 0)
            return ;
        //get answer Exeuted or Rejected
        String msgType = Fix.getValueByTag(35, ans);
        //get instrument id
        int instruId = Integer.parseInt(args[2]);
        //get qty
        int qty = Integer.parseInt(args[3]);

        if (msgType.compareTo("Rejected") == 0)
            return ;
        //if sell Exeuted
        if (action.compareTo("SELL") == 0){
            //get the instrument
            Integer instru = my_instruments.get(instruId);
            //update qty in inventory
            if (instru == qty)
                my_instruments.remove(instruId);
            else
                my_instruments.put(instruId, instru - qty);
            //uodate wallet
            wallet += (qty * getPrice(instruId));
        //if buy Exeuted
        }else{
            //get the instrument
            Integer instru = my_instruments.get(instruId);
            //update qty in inventory
            if (instru != null)
                my_instruments.put(instruId, instru + qty);
            else
                my_instruments.put(instruId, qty);
            //update wallet
            wallet -= (qty * getPrice(instruId));
        }
    }

    //***********************************************************************
    //***********************************************************************
    //SEND MESSAGE
    //***********************************************************************
    //***********************************************************************

    private static void startFixing()throws Exception{
        Scanner scan = new Scanner(System.in);
        
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
                //send fix to server
                ps.println(fix_msg);
                //recieve response
                getResponse(args[0].toUpperCase(), args);
                //show new stock
                System.out.println("New stock : " + my_instruments);
                System.out.println("Wallet : " + wallet);
            }
        }
    }

    //***********************************************************************
    //***********************************************************************
    //CREATE BROKER
    //***********************************************************************
    //***********************************************************************

    private static void askForId(String[] args) throws Exception{
        int desired_id = -1;
        if (args.length != 0){
            try{
                desired_id = Integer.parseInt(args[0]);
            }catch(Exception e){
                System.out.println("You must pass a valid id as argument");
            }
        }
        ps.println(desired_id);
        //get your id
        Id = Integer.parseInt(bf.readLine());
    }

    //***********************************************************************
    //***********************************************************************
    //START
    //***********************************************************************
    //***********************************************************************

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
            //ask for his Id
            askForId(args);
            //start listening
            startFixing();
        }catch(Exception e){
            System.out.println("Exception : " + e.getMessage());
        }
    }
}