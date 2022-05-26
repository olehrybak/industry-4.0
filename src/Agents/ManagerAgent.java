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
import java.util.ArrayList;
import java.util.List;

public class ManagerAgent extends Agent {
    Boolean isBusy = false;
    List<AID> machinesList = new ArrayList<>();

    @Override
    protected void setup(){
        System.out.println("Manager Agent " + getAID().getName() + " is up\n");

        int machinesNum = 3;
        createMachines(machinesNum);

        Behaviour messaging = new CyclicBehaviour() {
            @Override
            public void action() {
                ACLMessage msg = receive();
                if (msg != null) {
                    try {
                        if (msg.getPerformative() == ACLMessage.CFP) {
                            ACLMessage reply = new ACLMessage(ACLMessage.PROPOSE);
                            if (!isBusy) {
                                System.out.println(getLocalName() + ": received an order request from Big Boss. I am free, so I'm proposing myself for this order");
                                reply.setContentObject(msg.getContentObject());
                                reply.addReceiver(msg.getSender());
                                send(reply);
                            }
                            else {
                                System.out.println(getLocalName() + ": received an order request from Big Boss, but I'm busy");
                            }
                        } else if (msg.getPerformative() == ACLMessage.ACCEPT_PROPOSAL) {
                            isBusy = true;
                            Order order = (Order)msg.getContentObject();
                            System.out.println(getLocalName() + ": I was chosen for the Order#" + order.orderID);
                            informMachines(order);
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
                        "Agents.MachineAgent",
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
            System.out.println(getLocalName() + ": Informing the machines about the Order#" + order.orderID);
            for (AID machine:machinesList) {
                msg.addReceiver(machine);
                send(msg);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void takeDown(){
        System.out.println("Manager Agent " + getAID().getName() + " is down\n");
    }
}
