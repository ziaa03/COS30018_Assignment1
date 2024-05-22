package DVRS;

import jade.core.Agent;

public class Utilities
{
    private static boolean titlePrinted = false;

    public static synchronized void printTitle() {
        if (!titlePrinted) {
            System.out.println("\u001B[30m" + "----- Vehicle's Max Capacity and Depot Location received by MRA: -----" + "\u001B[0m");
            titlePrinted = true;
        }
    }

    public static synchronized void printMessage(Agent agent, String message)
    {
        System.out.println("\u001B[36m" + agent.getAID().getLocalName() + "\u001B[0m");
        System.out.println(message);
    }

    public static synchronized void printMessageWithoutAgent(String message)
    {
        System.out.println(message);
    }

    public static void printToGui(String message) {
        VRPGui.resultArea.append(message + "\n");
    }
}