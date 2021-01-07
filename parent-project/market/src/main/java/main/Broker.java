package main;

import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.*;

import database.Database;
import model.Instrument;
import dto.DataToObject;

public class Broker {
    private static List<Instrument> all_instruments;
    private static Map<Integer, Integer> my_instruments = new HashMap<>();
    private static int wallet = 500;

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

    public static void main(String[] args){
        System.out.println("Broker");
        try{
            //Get the connexion with the database
            Database db = new Database();
            //set my starting stuff
            setMyInstruments(db);
            System.out.println(my_instruments);
            //Connect to the router
            InetSocketAddress addr = new InetSocketAddress(InetAddress.getByName("localhost"), 5000);
        }catch(Exception e){

        }
    }
}