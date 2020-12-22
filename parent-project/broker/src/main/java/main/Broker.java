package main;

import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.*;

public class Broker {
    public static void main(String[] args){
        System.out.println("Broker");
        try{
            InetSocketAddress addr = new InetSocketAddress(InetAddress.getByName("localhost"), 5000);
        }catch(Exception e){

        }
    }
}