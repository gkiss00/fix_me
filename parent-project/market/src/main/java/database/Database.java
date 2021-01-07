package database;

import java.sql.*;

public class Database{
    private Connection connection;

    public Database(){
        try{

            String url = "jdbc:mysql://localhost:3306/fix_me";
            String user = "root";
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(url, user, user);
            System.out.println("Connection with database etablished");
        }catch(Exception e){
            System.out.println(e + "\n" + e.getMessage());
        }
    }

    //*****************************************************************************************
    //*****************************************************************************************
    //INSTUMENT
    //*****************************************************************************************
    //*****************************************************************************************

    public ResultSet getAllInstruments() throws Exception{
        ResultSet res;
        String request = "SELECT * FROM instruments";

        Statement statement = connection.createStatement();
        res = statement.executeQuery(request);
        return (res);
    }

    //*****************************************************************************************
    //*****************************************************************************************
    //TRANSACTIONS
    //*****************************************************************************************
    //*****************************************************************************************

    public void insertTransaction(int brokerId, int marketId, int instrumentId, int qty, int price, String date) throws Exception{
        String request = getRequest(brokerId, marketId, instrumentId, qty, price, date);

        Statement statement = connection.createStatement();
        statement.executeUpdate(request);
    }

    private String getRequest(int brokerId, int marketId, int instrumentId, int qty, int price, String date){
        String request = "INSERT INTO transactions VALUES (0, ";
        request += ITOS(brokerId);
        request += ", ";
        request += ITOS(marketId);
        request += ", ";
        request += ITOS(instrumentId);
        request += ", ";
        request += ITOS(qty);
        request += ", ";
        request += ITOS(price);
        request += ", '";
        request += date;
        request += "')";
        return request;
    }

    private String ITOS(int nb){
        return Integer.toString(nb);
    }
}