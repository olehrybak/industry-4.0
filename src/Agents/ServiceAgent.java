package Agents;

import jade.core.Agent;

public class ServiceAgent extends Agent {

    @Override
    protected void setup(){
        System.out.println("Service Agent " + getAID().getName() + " is up\n");


    }

    @Override
    protected void takeDown(){
        System.out.println("Service Agent " + getAID().getName() + " is down\n");
    }
}
