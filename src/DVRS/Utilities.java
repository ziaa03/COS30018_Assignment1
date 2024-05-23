package DVRS;

import jade.core.Agent;

//The Utilities class provides utility methods for printing messages to the console and GUI in a synchronized manner.
//It ensures that message output is thread-safe and consistent.
public class Utilities
{
    private static boolean titlePrinted = false; // Static field to check if the title has been printed

    // Synchronized method to print a title message once.
    // Ensures the title is printed only once by checking the titlePrinted flag.
    public static synchronized void printTitle() {
        if (!titlePrinted) {
            System.out.println("\u001B[30m" + "----- Vehicle's Max Capacity and Depot Location received by MRA: -----" + "\u001B[0m");
            titlePrinted = true;
        }
    }
    
    // Synchronized method to print a message associated with a specific agent.
    // The agent's local name is printed in a specific color for clarity.
    public static synchronized void printMessage(Agent agent, String message)
    {
        System.out.println("\u001B[36m" + agent.getAID().getLocalName() + "\u001B[0m");
        System.out.println(message);
    }
    
    // Synchronized method to print a message without associating it with any agent.
    // This method is useful for general messages that are not specific to any agent.
    public static synchronized void printMessageWithoutAgent(String message)
    {
        System.out.println(message);
    }
    
    // Method to print a message to the GUI.
    // This method appends the message to the result area of the VRPGui.
    public static void printToGui(String message) {
        VRPGui.resultArea.append(message + "\n");
    }
}