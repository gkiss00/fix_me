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
    private static int Id = 1000000;
    private static List<Instrument> all_instruments;
    private static Map<Integer, Integer> my_instruments = new HashMap<>();
    private static int wallet = 500;

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

    //VALIDATE THE INPUT
    private static boolean validateInput(String args[]){
        if (args.length != 5)
            return false;
        try {
            int test = Integer.parseInt(args[2]);
            test = Integer.parseInt(args[3]);
            test = Integer.parseInt(args[4]);
            System.out.println("ok");
            return true;
        }catch(Exception e){
            return false;
        }
    }

    private static void startFixing()throws Exception{
        Scanner scan = new Scanner(System.in);
        BufferedReader bf = new BufferedReader(new InputStreamReader(s.getInputStream()));
        PrintStream ps = new PrintStream(s.getOutputStream());

        //get your id
        Id = Integer.parseInt(bf.readLine());
        while(true){
            String msg = scan.nextLine();
            String args[] = msg.split("\\s+");
            if(validateInput(args)){
                String fix_msg = Fix.stringToFix(args[0], Id, Integer.parseInt(args[1]), 
                                                Integer.parseInt(args[2]), Integer.parseInt(args[3]), Integer.parseInt(args[4]));    
                ps.println(fix_msg);
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
            //InetSocketAddress addr = new InetSocketAddress(InetAddress.getByName("localhost"), 5000);
            startFixing();
        }catch(Exception e){

        }
    }
}