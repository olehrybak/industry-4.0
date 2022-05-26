package Agents;

import jade.core.Agent;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;

import static java.lang.Thread.sleep;

public class SimulationAgent extends Agent {
    String bigBossName = "BigBoss";
    @Override
    protected void setup(){
        AgentController bigboss;
        System.out.println("Simulation Agent " + getAID().getName() + " is up\n");
        try {
            bigboss = getContainerController().createNewAgent(
                    bigBossName,
                    "Agents.BigBossAgent",
                    new Object[]{}
            );
            bigboss.start();
        } catch (StaleProxyException e) {
            throw new RuntimeException(e);
        }
        /*try {
            sleep(20000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }*/
        try {
            AgentController customer = getContainerController().createNewAgent(
                    "Customer#1",
                    "Agents.CustomerAgent",
                    new Object[]{getAID(bigBossName)}
            );
            customer.start();
        } catch (StaleProxyException e) {
            throw new RuntimeException(e);
        }
    }
}
