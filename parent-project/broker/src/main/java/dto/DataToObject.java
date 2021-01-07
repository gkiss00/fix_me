package dto;

import java.sql.*;
import java.util.List;
import java.util.ArrayList;

import model.Instrument;

public class DataToObject {
    public DataToObject(){}

    public static List<Instrument> getAllInstrument(ResultSet res) throws Exception{
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
}