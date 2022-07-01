package com.agents;

import jade.core.Agent;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;
import static java.lang.Thread.sleep;

public class SimulationAgent extends Agent {
    String bigBossName = "BigBoss";
    @Override
    protected void setup(){
        System.out.println("Simulation Agent " + getAID().getName() + " is up\n");
        try {
            AgentController bigBoss = getContainerController().createNewAgent(
                    bigBossName,
                    "com.agents.BigBossAgent",
                    new Object[]{}
            );
            bigBoss.start();
        } catch (StaleProxyException e) {
            throw new RuntimeException(e);
        }

        try {
            for (int i = 0; i < 4; i++) {
                AgentController customer = getContainerController().createNewAgent(
                        "Customer#" + (i+1),
                        "com.agents.CustomerAgent",
                        new Object[]{getAID(bigBossName)}
                );
                customer.start();
                sleep(3000);
                System.out.println();
            }
        } catch (StaleProxyException|InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}