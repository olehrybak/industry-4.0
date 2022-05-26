package Agents;

import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;

public class MachineAgent extends Agent {
    @Override
    protected void setup(){
        System.out.println("Machine Agent " + getAID().getName() + " is up\n");

        Behaviour messaging = new CyclicBehaviour() {
            @Override
            public void action() {
                ACLMessage msg = receive();
                if (msg != null){
                    if (msg.getPerformative() == ACLMessage.INFORM){
                        try {
                            Order order = (Order)msg.getContentObject();
                            System.out.println(getAID().getLocalName() + ": I was informed about Order#" + order.orderID + ". Taking my position in the production line");
                        } catch (UnreadableException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
        };

        addBehaviour(messaging);
    }

    @Override
    protected void takeDown(){
        System.out.println("Machine Agent " + getAID().getName() + " is down\n");
    }
}
