package Agents;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;

import java.io.IOException;
import java.util.*;

public class BigBossAgent extends Agent {
    List<AID> managersList = new ArrayList<>();
    List<Order> ordersList = new ArrayList<>();
    Order currentOrder;

    @Override
    protected void setup(){
        System.out.println("Big Boss Agent " + getLocalName() + " is up\n");

        int managerNum = 3;
        createManagers(managerNum);

        Behaviour messaging = new CyclicBehaviour() {
            @Override
            public void action() {
                startOrderProduction();
                ACLMessage msg = receive();
                if (msg != null) {
                    if (msg.getPerformative() == ACLMessage.REQUEST){
                        try {
                            Order order = (Order)msg.getContentObject();
                            System.out.println(getLocalName() + ": received an order from " + msg.getSender().getLocalName());
                            ACLMessage reply = msg.createReply();
                            reply.setPerformative(ACLMessage.AGREE);
                            send(reply);
                            ordersList.add(order);
                            Collections.sort(ordersList);

                        } catch (UnreadableException e) {
                            e.printStackTrace();
                        }
                    }
                    if (msg.getPerformative() == ACLMessage.PROPOSE) {
                        try {
                            Order msgOrder = (Order)msg.getContentObject();
                            if (currentOrder != null && msgOrder.orderID == currentOrder.orderID) {
                                ACLMessage reply = msg.createReply();
                                reply.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
                                reply.setContentObject(currentOrder);
                                ordersList.remove(currentOrder);
                                currentOrder = null;
                                System.out.println(getLocalName() + ": " + msg.getSender().getLocalName() + " is free, so I'm assigning the order to him");
                                send(reply);
                            }
                        } catch (IOException | UnreadableException e) {
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
                        "Agents.ManagerAgent",
                        new Object[]{getAID()}
                );
                manager.start();
                managersList.add(getAID(managerName));
            } catch (StaleProxyException e) {
                e.printStackTrace();
            }
        }
    }

    private void startOrderProduction() {
        if (currentOrder != null || ordersList.isEmpty())
            return;
        try {
            currentOrder = ordersList.get(0);
            ACLMessage message = new ACLMessage(ACLMessage.CFP);
            message.setContentObject(currentOrder);
            for (AID manager : managersList) {
                message.addReceiver(manager);
            }
            System.out.println(getLocalName() + ": sending a request to managers");
            send(message);
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    @Override
    protected void takeDown(){
        System.out.println("Big Boss Agent " + getAID().getName() + " is down\n");
    }
}
