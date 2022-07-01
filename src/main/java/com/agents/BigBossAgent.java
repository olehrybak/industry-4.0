package com.agents;

import com.gui.GUIController;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;
import javafx.application.Platform;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class BigBossAgent extends Agent {
    List<AID> managersList = new ArrayList<>();
    List<Order> ordersList = new ArrayList<>();
    Order currentOrder;
    int currentManager;

    @Override
    protected void setup(){
        System.out.println("\u001B[31m" + "Big Boss Agent " + getLocalName() + " is up\n");

        int managerNum = 3;
        createManagers(managerNum);

        Behaviour messaging = new CyclicBehaviour() {
            @Override
            public void action() {
                //startOrderProduction();
                ACLMessage msg = receive();
                if (msg != null) {
                    if (msg.getPerformative() == ACLMessage.REQUEST){
                        try {
                            Order order = (Order)msg.getContentObject();
                            System.out.println("\u001B[31m" + getLocalName() + ": received an order from " + msg.getSender().getLocalName());
                            ACLMessage reply = msg.createReply();
                            reply.setPerformative(ACLMessage.AGREE);
                            send(reply);

                            sendToManager(order);
                        } catch (UnreadableException e) {
                            e.printStackTrace();
                        }
                    }
                    if (msg.getPerformative() == ACLMessage.AGREE) {
                        try {
                            Order msgOrder = (Order)msg.getContentObject();
                            System.out.println("\u001B[31m" + getLocalName() + ": " + msg.getSender().getLocalName() + " accepted Order#" + msgOrder.orderID);
                        } catch (UnreadableException e) {
                            e.printStackTrace();
                        }
                    }
                    if (msg.getPerformative() == ACLMessage.REFUSE) {
                        try {
                            Order msgOrder = (Order) msg.getContentObject();
                            System.out.println("\u001B[31m" + getLocalName() + ": " + msg.getSender().getLocalName() + " refused to take Order#" + msgOrder.orderID);
                            sendToManager(msgOrder);
                        } catch (UnreadableException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        };
        addBehaviour(messaging);
    }

    private void createManagers(int managersNum){
        for (int i = 0; i < managersNum; i++){
            try {
                String managerName = "Manager#" + (i+1);
                AgentController manager = getContainerController().createNewAgent(
                        managerName,
                        "com.agents.ManagerAgent",
                        new Object[]{getAID()}
                );
                manager.start();
                managersList.add(getAID(managerName));
            } catch (StaleProxyException e) {
                e.printStackTrace();
            }
        }
    }

    private void sendToManager(Order order) {
        try {
            ACLMessage message = new ACLMessage(ACLMessage.REQUEST);
            message.setContentObject(order);
            message.addReceiver(managersList.get(currentManager));
            System.out.println("\u001B[31m" + getLocalName() + ": sending a request to a manager");
            send(message);
            Agent currentAgent = this;
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    GUIController.bigBossMsg(currentAgent,"manager",managersList.get(currentManager));
                }
            });
            currentManager++;
            if (currentManager >= managersList.size())
                currentManager = 0;
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    @Override
    protected void takeDown(){
        System.out.println("\u001B[31m" + "Big Boss Agent " + getAID().getName() + " is down\n");
    }
}
