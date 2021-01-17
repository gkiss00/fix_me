package dto;

import java.sql.*;
import java.util.List;
import java.util.ArrayList;
import java.util.*;

import model.Instrument;

public class DataToObject {
    public DataToObject(){}

    public static List<Instrument> getAllInstruments(ResultSet res) throws Exception{
        List<Instrument> instruments = new ArrayList<>();
        while(res.next()){
            int id = res.getInt("id");
            String name = res.getString("name");
            int price = res.getInt("price");
            Instrument instrument = new Instrument(id, name, price);
            instruments.add(instrument);
        }
        return (instruments);
    }

    public static Map<Integer, Integer> getClientInstruments(ResultSet res) throws Exception{
        Map<Integer, Integer> instruments = new HashMap<Integer, Integer>();
        while(res.next()){
            int id = res.getInt("instrument_id");
            int qty = res.getInt("quantity");
            instruments.put(id, qty);
        }
        return (instruments);
    }
}