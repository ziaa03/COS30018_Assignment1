package allAgents;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class OptimizationTechniques {

    private static final int POPULATION_SIZE = 100;
    private static final int MAX_GENERATIONS = 1000;
    private static final double MUTATION_RATE = 0.05;
    private static final double CROSSOVER_RATE = 0.8;
    private static final int TOURNAMENT_SIZE = 5;

    // Assuming you have a way to calculate the distance between points
    private double[][] distanceMatrix;

    public OptimizationTechniques(double[][] distanceMatrix) {
        this.distanceMatrix = distanceMatrix;
    }

    public List<Integer> solveVRP() {
        List<List<Integer>> population = initializePopulation();
        for (int generation = 0; generation < MAX_GENERATIONS; generation++) {
            List<List<Integer>> newPopulation = new ArrayList<>();
            while (newPopulation.size() < POPULATION_SIZE) {
                List<Integer> parent1 = tournamentSelection(population);
                List<Integer> parent2 = tournamentSelection(population);
                List<Integer> offspring;
                if (Math.random() < CROSSOVER_RATE) {
                    offspring = crossover(parent1, parent2);
                } else {
                    offspring = new ArrayList<>(parent1);
                }
                if (Math.random() < MUTATION_RATE) {
                    mutate(offspring);
                }
                newPopulation.add(offspring);
            }
            population = newPopulation;
        }
        return Collections.min(population, this::compareRoutes);
    }

    private List<List<Integer>> initializePopulation() {
        List<List<Integer>> population = new ArrayList<>();
        List<Integer> initialRoute = new ArrayList<>();
        for (int i = 0; i < distanceMatrix.length; i++) {
            initialRoute.add(i);
        }
        for (int i = 0; i < POPULATION_SIZE; i++) {
            Collections.shuffle(initialRoute);
            population.add(new ArrayList<>(initialRoute));
        }
        return population;
    }

    private List<Integer> tournamentSelection(List<List<Integer>> population) {
        List<List<Integer>> tournament = new ArrayList<>();
        for (int i = 0; i < TOURNAMENT_SIZE; i++) {
            int randomIndex = (int) (Math.random() * population.size());
            tournament.add(population.get(randomIndex));
        }
        return Collections.min(tournament, this::compareRoutes);
    }

    private List<Integer> crossover(List<Integer> parent1, List<Integer> parent2) {
        // Implement ordered crossover or another suitable crossover mechanism
        return parent1; // Placeholder
    }

    private void mutate(List<Integer> route) {
        int index1 = (int) (Math.random() * route.size());
        int index2 = (int) (Math.random() * route.size());
        Collections.swap(route, index1, index2);
    }

    private int compareRoutes(List<Integer> route1, List<Integer> route2) {
        double distance1 = calculateTotalDistance(route1);
        double distance2 = calculateTotalDistance(route2);
        return Double.compare(distance1, distance2);
    }

    private double calculateTotalDistance(List<Integer> route) {
        double totalDistance = 0;
        for (int i = 0; i < route.size() - 1; i++) {
            totalDistance += distanceMatrix[route.get(i)][route.get(i + 1)];
        }
        totalDistance += distanceMatrix[route.get(route.size() - 1)][route.get(0)]; // Assuming return to start
        return totalDistance;
    }

    public static void main(String[] args) {
        double[][] distanceMatrix = {{0, 10, 15, 20}, {10, 0, 35, 25}, {15, 35, 0, 30}, {20, 25, 30, 0}};
        OptimizationTechniques optimizer = new OptimizationTechniques(distanceMatrix);
        List<Integer> bestRoute = optimizer.solveVRP();
        System.out.println("Best route: " + bestRoute);
    }
}
