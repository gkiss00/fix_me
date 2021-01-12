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
    //INSTRUMENT
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

    public void insertTransaction(String fix_msg) throws Exception{
        String request = getRequest(fix_msg);

        Statement statement = connection.createStatement();
        statement.executeUpdate(request);
    }

    private String getRequest(String fix_msg){
        String request = "INSERT INTO transactions VALUES (0, \"";
        request += fix_msg;
        request += "\")";
        return request;
    }

    private String ITOS(int nb){
        return Integer.toString(nb);
    }
}