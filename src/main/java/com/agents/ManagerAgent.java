package com.agents;

import com.gui.GUIController;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;
import javafx.application.Platform;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ManagerAgent extends Agent {
    Boolean acceptingOrders = true;
    List<AID> machinesList = new ArrayList<>();
    List<Order> ordersList = new ArrayList<>();
    List<AID> managersList = new ArrayList<>();
    List<Integer> proposeList = new ArrayList<>();
    boolean machinesAreBusy = false;

    @Override
    protected void setup(){
        System.out.println("\u001B[32m" + "Manager Agent " + getAID().getName() + " is up\n");

        int machinesNum = 3;
        createMachines(machinesNum);

        //Registering ManagerAgent in the DF agent
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("manager");
        sd.setName(getAID().getName());
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);

        }catch (FIPAException fe) {
            fe.printStackTrace();
        }

        //Behaviour for messages handling
        Behaviour messaging = new CyclicBehaviour() {
            @Override
            public void action() {

                if (!machinesAreBusy && !ordersList.isEmpty()){
                    informMachines(ordersList.get(0));
                    ordersList.remove(0);
                    Collections.sort(ordersList);
                    machinesAreBusy = true;
                }

                ACLMessage msg = receive();
                if (msg != null) {
                    try {
                        if (msg.getPerformative() == ACLMessage.CFP){
                            System.out.println("\u001B[32m" + getLocalName() + ": Received cfp from " +
                                    msg.getSender().getLocalName() + ", sending back a propose");
                            ACLMessage proposal = new ACLMessage(ACLMessage.PROPOSE);
                            proposal.addReceiver(msg.getSender());
                            proposal.setContentObject(msg.getContentObject());
                            myAgent.send(proposal);
                        }
                        else if (msg.getPerformative() == ACLMessage.PROPOSE){
                            Order order = (Order)msg.getContentObject();

                            if (!proposeList.contains(order.orderID)) {
                                ACLMessage proposal = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
                                proposal.setContentObject(order);
                                proposal.addReceiver(msg.getSender());
                                System.out.println("\u001B[32m" + getLocalName() + ": Received proposal from " +
                                        msg.getSender().getLocalName() + ", accepting it");
                                myAgent.send(proposal);
                                proposeList.add(order.orderID);
                            }
                        }
                        else if (msg.getPerformative() == ACLMessage.ACCEPT_PROPOSAL){
                            Order order = (Order)msg.getContentObject();
                            System.out.println("\u001B[32m" + getLocalName() + ": " + msg.getSender().getLocalName() +
                                            " accepted my proposal, adding half of Order#" + order.orderID +
                                            " to the queue");
                            ordersList.add(order);
                            Collections.sort(ordersList);
                        }
                        else if (msg.getPerformative() == ACLMessage.REQUEST) {
                            ACLMessage reply = msg.createReply();
                            Order order = (Order)msg.getContentObject();
                            if (acceptingOrders) {
                                System.out.println("\u001B[32m" + getLocalName() + ": received an order request from Big Boss. " +
                                        "I am on duty, so I'm accepting it");
                                reply.setContentObject(order);
                                reply.setPerformative(ACLMessage.AGREE);
                                send(reply);
                                Agent currentAgent = myAgent;
                                Platform.runLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        GUIController.managerMsg(currentAgent,"bigBoss", msg.getSender());
                                    }
                                });

                                /*
                                If it is impossible to prepare order before the deadline, we split it into 2 parts
                                and send one part to another manager
                                */
                                if (orderProdTime(order) > order.deadline){
                                    System.out.println("\u001B[32m" + getLocalName() + ": The order is too big. I can't complete it " +
                                            "before the deadline alone!");
                                    Order splitOrder1 = new Order(order.customerId,
                                            order.deadline,
                                            order.productName,
                                            (int)Math.ceil((double)order.quantity/2));
                                    splitOrder1.isSplit = true;

                                    Order splitOrder2 = new Order(order.customerId,
                                            order.deadline,
                                            order.productName,
                                            (int)Math.floor((double)order.quantity/2));
                                    splitOrder2.isSplit = true;

                                    System.out.println("\u001B[32m" + getLocalName() + ": splitting Order#" + order.orderID +
                                            " into Order#" + splitOrder1.orderID + " and Order#" + splitOrder2.orderID);

                                    ordersList.add(splitOrder1);
                                    Collections.sort(ordersList);

                                    getManagersDF();
                                    ACLMessage message = new ACLMessage(ACLMessage.CFP);
                                    for (AID manager : managersList){
                                        if(manager != getAID())
                                            message.addReceiver(manager);
                                    }
                                    message.setContentObject(splitOrder2);
                                    System.out.println("\u001B[32m" + getLocalName() + ": Sending cfp to all managers");
                                    myAgent.send(message);

                                } else{
                                    ordersList.add(order);
                                    Collections.sort(ordersList);
                                }
                            }
                            else {
                                System.out.println("\u001B[32m" + getLocalName() + ": received an order request from Big Boss, but " +
                                        "I cannot accept it");
                                reply.setContentObject(order);
                                reply.setPerformative(ACLMessage.REFUSE);
                                send(reply);
                            }
                        }
                    }catch (UnreadableException|IOException e) {
                            e.printStackTrace();
                    }
                }
            }
        };
        addBehaviour(messaging);
    }

    private void createMachines(int machinesNum){
        for (int i = 0; i < machinesNum; i++){
            try {
                String machineName = getLocalName() + "_Machine#" + (i+1);
                AgentController machine = getContainerController().createNewAgent(
                        machineName,
                        "com.agents.MachineAgent",
                        new Object[]{getAID()}
                );
                machine.start();
                machinesList.add(getAID(machineName));
            } catch (StaleProxyException e) {
                e.printStackTrace();
            }
        }
    }

    private void informMachines(Order order){
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        try {
            msg.setContentObject(order);
            System.out.println("\u001B[32m" + getLocalName() + ": Informing the machines about the Order#" + order.orderID);
            for (AID machine:machinesList) {
                msg.addReceiver(machine);
                send(msg);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private int orderProdTime(Order order){
        return switch (order.productName) {
            case "A" -> 4 * order.quantity;
            case "B" -> 2 * order.quantity;
            case "C" -> order.quantity;
            default -> 0;
        };
    }

    private int totalOrdersProdTime(){
        int totalProdTime = 0;
        for (Order order : ordersList) {
            totalProdTime += orderProdTime(order);
        }
        return totalProdTime;
    }

    private void getManagersDF(){
        DFAgentDescription dfd = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType("manager");
        dfd.addServices(sd);
        try{
            managersList.clear();
            DFAgentDescription[] result = DFService.search(this, dfd);
            if (result.length > 0) {
                //Getting the list of Gateways from DF agent
                for (int i = 0; i < result.length; i++) {
                    managersList.add(result[i].getName());
                }
            }
            managersList.remove(getAID());
        }
        catch (FIPAException e){
            e.printStackTrace();
        }
    }

    @Override
    protected void takeDown(){
        System.out.println("\u001B[32m" + "Manager Agent " + getAID().getName() + " is down\n");
    }
}
