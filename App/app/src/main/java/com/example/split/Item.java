package com.example.split;

public class Item {

    public String name;
    public String cost;
    public boolean paid;
    public int id;

    public Item(String cost, String name, int id, boolean paid) {
        this.cost = cost;
        this.name = name;
        this.id = id;
        this.paid = paid;
    }
}
