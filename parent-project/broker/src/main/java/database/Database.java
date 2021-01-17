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
    //RESET
    //*****************************************************************************************
    //*****************************************************************************************

    public void reset() throws Exception{
        String request1 = "DELETE FROM clientsinstruments";
        String request2 = "DELETE FROM transactions";
        String request3 = "DELETE FROM clients";

        Statement statement = connection.createStatement();
        statement.executeUpdate(request1);
        statement.executeUpdate(request2);
        statement.executeUpdate(request3);
    }

    //*****************************************************************************************
    //*****************************************************************************************
    //CLIENT
    //*****************************************************************************************
    //*****************************************************************************************

    public void insertClient(int client_id, int type) throws Exception{
        String request = "INSERT INTO clients VALUES (" + ITOS(client_id) + ", " + ITOS(type) + ", 0, 2000)";

        Statement statement = connection.createStatement();
        statement.executeUpdate(request);
    }

    public int getClientPending(int client_id)throws Exception{
        ResultSet res;
        int pending = 0;
        String request = "SELECT pending FROM clients WHERE id=" + ITOS(client_id);
        Statement statement = connection.createStatement();
        res = statement.executeQuery(request);
        if (res.next())
            pending = res.getInt(1);
        return (pending);
    }

    public void updateClientPending(int client_id, int pending) throws Exception{
        String request = "UPDATE clients SET pending=" + ITOS(pending) + " WHERE id=" + ITOS(client_id);

        Statement statement = connection.createStatement();
        statement.executeUpdate(request);
    }

    public int getClientWallet(int client_id) throws Exception{
        ResultSet res;
        int wallet = 0;
        String request = "SELECT wallet FROM clients WHERE id=" + ITOS(client_id);
        Statement statement = connection.createStatement();
        res = statement.executeQuery(request);
        if (res.next())
            wallet = res.getInt(1);
        return (wallet);
    }

    public void updateClientWallet(int client_id, int wallet) throws Exception{
        String request = "UPDATE clients SET wallet=" + ITOS(wallet) + " WHERE id=" + ITOS(client_id);

        Statement statement = connection.createStatement();
        statement.executeUpdate(request);
    }

    public int getMaxId() throws Exception{
        int maxId = 0;
        ResultSet res;
        String request = "SELECT MAX(id) FROM clients";

        Statement statement = connection.createStatement();
        res = statement.executeQuery(request);
        if (res.next())
            maxId = res.getInt(1);
        return (maxId);
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

    public ResultSet getClientInstruments(int client_id) throws Exception{
        ResultSet res;
        String request = "SELECT * FROM clientsinstruments where client_id=" + ITOS(client_id);

        Statement statement = connection.createStatement();
        res = statement.executeQuery(request);
        return (res);
    }

    public void insertClientIntrument(int client_id, int instrument_id, int qty) throws Exception{
        String request = "INSERT INTO clientsinstruments VALUES (" + ITOS(client_id) + ", " + ITOS(instrument_id) + ", " + ITOS(qty) + ")";

        Statement statement = connection.createStatement();
        statement.executeUpdate(request);
    }

    public void updateClientIntrument(int client_id, int instrument_id, int qty) throws Exception{
        String request = "UPDATE clientsinstruments SET quantity=" + ITOS(qty) + " WHERE client_id=" + ITOS(client_id) + " and instrument_id=" + ITOS(instrument_id);
        
        Statement statement = connection.createStatement();
        statement.executeUpdate(request);
    }

    public void removeClientIntrument(int client_id, int instrument_id) throws Exception{
        String request = "DELETE FROM clientsinstruments WHERE client_id=" + ITOS(client_id) + " and instrument_id=" + ITOS(instrument_id);

        Statement statement = connection.createStatement();
        statement.executeUpdate(request);
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