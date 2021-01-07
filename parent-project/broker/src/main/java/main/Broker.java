package main;

import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.*;

import database.Database;

public class Broker {
    public static void main(String[] args){
        System.out.println("Broker");
        try{
            Database db = new Database();
            InetSocketAddress addr = new InetSocketAddress(InetAddress.getByName("localhost"), 5000);
        }catch(Exception e){

        }
    }
}