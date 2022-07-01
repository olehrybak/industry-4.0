package com.agents;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;

import java.io.IOException;

public class CustomerAgent extends Agent {

    @Override
    protected void setup(){
        System.out.println("\u001B[33m" + "Customer Agent " + getLocalName() + " is up\n");

        Object[] args = getArguments();
        AID bigBossAgent = (AID)args[0];

        Behaviour sendOrder = new OneShotBehaviour() {
            @Override
            public void action() {
                try {
                    Order order = new Order(getAID(), 20, "A", 3);
                    ACLMessage orderMessage = new ACLMessage(ACLMessage.REQUEST);
                    System.out.println("\u001B[33m" + getLocalName() + ": sending an order to the factory");
                    orderMessage.setContentObject(order);
                    orderMessage.addReceiver(getAID("BigBoss"));
                    myAgent.send(orderMessage);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        };

        Behaviour receiveMessages = new CyclicBehaviour() {
            @Override
            public void action() {
                ACLMessage msg = receive();
                if (msg != null) {
                    if (msg.getPerformative() == ACLMessage.AGREE){
                        System.out.println("\u001B[33m" + getLocalName() + ": factory accepted my order");
                    }else if (msg.getPerformative() == ACLMessage.REFUSE){
                        System.out.println("\u001B[33m" + getLocalName() + ": factory refused my order");
                    }
                }else{
                    block();
                }

            }
        };

        addBehaviour(sendOrder);
        addBehaviour(receiveMessages);
    }

    @Override
    protected void takeDown(){
        System.out.println("\u001B[33m" + "Customer Agent " + getAID().getName() + " is down\n");
    }
}
