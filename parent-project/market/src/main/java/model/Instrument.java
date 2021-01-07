package model;

public class Instrument{
    private int id;
    private String name;
    private int price;
    public Instrument(int id, String name, int price){
        this.id = id;
        this.name = name;
        this.price = price;
    }

    public int getId(){
        return id;
    }

    public String getName(){
        return name;
    }

    public int getPrice(){
        return price;
    }

    @Override
    public String toString(){
        String str = "Id : ";
        str += Integer.toString(this.id);
        str += "\nName : ";
        str += this.name;
        str += "\nPrice : ";
        str += Integer.toString(this.price);
        return str;
    }
}