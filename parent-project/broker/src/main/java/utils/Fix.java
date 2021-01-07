package utils;

import java.util.*;

public class Fix{
    public Fix(){}

    public static String stringToFix(String msgType, int senderCompId, int targetCompId, int instrumentId, int qty, int price){
        String BeginString = "8=FIX.4.4";
        String BodyLenght = "9=";
        String MsgType = "35=" + msgType;
        String SenderCompId = "49=" + Integer.toString(senderCompId);
        String TargetCompId = "56=" + Integer.toString(targetCompId);
        String Instrument = "58=" + Integer.toString(instrumentId);
        String Qty = "53=" + Integer.toString(qty);
        String Price = "44=" + Integer.toString(price);
        String CheckSum = "10=";

        BodyLenght += getBodyLength(6, MsgType, SenderCompId, TargetCompId, Instrument, Qty, Price);
        CheckSum += getCheckSum(8, BeginString, BodyLenght, MsgType, SenderCompId, TargetCompId, Instrument, Qty, Price);

        String FixMessage = joinWith(9, '|', BeginString, BodyLenght, MsgType, SenderCompId, TargetCompId, Instrument, Qty, Price, CheckSum);

        return (FixMessage);
    }

    private static String getBodyLength(int nb_values, String ...args){
        int body_lenght = 0;

        //add each lenght of string
        for (int i = 0; i < nb_values; ++i){
            body_lenght += args[i].length();
        }
        //add the number of pipes
        body_lenght += nb_values -1;
        return Integer.toString(nb_values);
    }

    private static String getCheckSum(int nb_values, String... args){
        int sum = 0;
        for (int i = 0; i < nb_values; ++i){
            String tmp = args[i];
            for (int j = 0; j < tmp.length(); ++j){
                sum += tmp.charAt(j);
            }
        }
        sum += nb_values;
        sum = sum % 256;
        return (fillFront(Integer.toString(sum), 3, '0'));
    }

    private static String joinWith(int nb_values, char c, String ...args){
        String str = "";

        for (int i = 0; i < nb_values; ++i){
            str += args[i];
            if (i != nb_values -1)
                str += c;
        }
        return (str);
    }

    

    private static String fillFront(String str, int len, char c){
        String ret = "";

        while(len > str.length()){
            ret += c;
            --len;
        }
        ret += str;
        return (ret);
    }
}