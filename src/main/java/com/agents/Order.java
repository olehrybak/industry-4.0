package com.agents;

import jade.core.AID;

import java.io.Serializable;

public class Order implements Serializable, Comparable<Order> {
    public AID customerId;
    public int deadline;
    public String productName;
    public int quantity;
    private static int ID;
    public int orderID;
    public Boolean isSplit;

    public Order(AID customerId, int deadline, String productName, int quantity){
        this.customerId = customerId;
        this.deadline = deadline;
        this.productName = productName;
        this.quantity = quantity;
        orderID = ID;
        ID++;
    }

    @Override
    public int compareTo(Order o) {
        int compareQuantity = o.quantity;

        //ascending order
        return this.quantity - compareQuantity;
    }
}
