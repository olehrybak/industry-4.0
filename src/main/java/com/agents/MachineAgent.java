package com.agents;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;

import java.util.HashMap;
import java.util.Map;

public class MachineAgent extends Agent {
    AID nextMachine;
    Map<String, Integer> stagesList = new HashMap<String, Integer>();

    @Override
    protected void setup(){
        System.out.println("\u001B[34m" + "Machine Agent " + getAID().getName() + " is up\n");

        Behaviour messaging = new CyclicBehaviour() {
            @Override
            public void action() {
                ACLMessage msg = receive();
                if (msg != null){
                    if (msg.getPerformative() == ACLMessage.INFORM){
                        try {
                            Order order = (Order)msg.getContentObject();
                            System.out.println("\u001B[34m" + getAID().getLocalName() + ": I was informed about Order#" + order.orderID + ". Taking my position in the production line");
                        } catch (UnreadableException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        };

        addBehaviour(messaging);
    }

    @Override
    protected void takeDown(){
        System.out.println("\u001B[34m" + "Machine Agent " + getAID().getName() + " is down\n");
    }
}
