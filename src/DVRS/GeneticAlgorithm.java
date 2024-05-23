package DVRS;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

//The GeneticAlgorithm class provides methods for solving the vehicle routing problem using a genetic algorithm.
public class GeneticAlgorithm {
	// Constants for genetic algorithm parameters
    private static final double MUTATION_RATE = 0.1; // The probability of mutation in the genetic algorithm.
    private static final int TOURNAMENT_SIZE = 5; // The size of the tournament selection pool.
    private static final int MAX_GENERATIONS = 1000; // The maximum number of generations for the genetic algorithm.
    
    // Initializes the population with random routes.
    public static List<List<Integer>> initializePopulation(int populationSize, int numParcels) {
        List<List<Integer>> population = new ArrayList<>();
        for (int i = 0; i < populationSize; i++) {
            population.add(randomRoute(numParcels));
        }
        return population;
    }
    
    // Generates a random route for a given number of parcels.
    private static List<Integer> randomRoute(int numParcels) {
        List<Integer> route = new ArrayList<>();
        Random rnd = new Random();
        for (int i = 1; i <= numParcels; i++) {
            route.add(i);
        }
        java.util.Collections.shuffle(route, rnd);
        route.add(0, 0);
        route.add(0);
        return route;
    }
    
    // Calculates distances between parcels to create a distance matrix.
    private static double[][] calculateDistances(List<Parcel> parcels) {
        int numParcels = parcels.size();
        double[][] distances = new double[numParcels + 1][numParcels + 1];
        for (int i = 0; i < numParcels; i++) {
            for (int j = 0; j < numParcels; j++) {
                double distance = Math.sqrt(Math.pow(parcels.get(i).x - parcels.get(j).x, 2) + Math.pow(parcels.get(i).y - parcels.get(j).y, 2));
                distances[i + 1][j + 1] = Math.round(distance * 10.0) / 10.0; // Rounded to one decimal place
            }
            double originDistance = Math.sqrt(Math.pow(parcels.get(i).x, 2) + Math.pow(parcels.get(i).y, 2));
            distances[0][i + 1] = Math.round(originDistance * 10.0) / 10.0; // Rounded to one decimal place
            distances[i + 1][0] = distances[0][i + 1];
        }
        return distances;
    }
    
    // Runs the genetic algorithm to find the best route.
    public static List<Integer> runGeneticAlgorithm(List<List<Integer>> population, double[][] distances) {
        int generation = 0;
        List<Integer> bestRoute = null;
        double bestFitness = Double.MAX_VALUE;

        // The loop terminates when reaches the MAX_GENERATIONS limit, and the best route found during the iterations is returned
        while (generation < MAX_GENERATIONS) { 
            for (int i = 0; i < population.size(); i++) {
                List<Integer> route = population.get(i);
                double fitness = evaluateRoute(route, distances);
                if (fitness < bestFitness) {
                    bestFitness = fitness;
                    bestRoute = new ArrayList<>(route);
                }
            }

            List<Integer> parent1 = tournamentSelection(population, distances);
            List<Integer> parent2 = tournamentSelection(population, distances);
            List<Integer> child = crossover(parent1, parent2);
            mutate(child);

            final List<Integer> finalBestRoute = bestRoute;
            population.removeIf(route -> route.equals(finalBestRoute));
            population.add(child);

            generation++;
        }
        return bestRoute;
    }
    
    // Selects parents for crossover using tournament selection.
    private static List<Integer> tournamentSelection(List<List<Integer>> population, double[][] distances) {
        Random rnd = new Random();
        List<Integer> bestRoute = null;
        double bestFitness = Double.MAX_VALUE;
        for (int i = 0; i < TOURNAMENT_SIZE; i++) {
            List<Integer> candidate = population.get(rnd.nextInt(population.size()));
            double fitness = evaluateRoute(candidate, distances);
            if (fitness < bestFitness) {
                bestFitness = fitness;
                bestRoute = candidate;
            }
        }
        return bestRoute;
    }
    
    // Performs crossover operation between parents to create a child route
    // Swap the segments of parent routes between the crossover points to create new routes for offspring. 
    private static List<Integer> crossover(List<Integer> parent1, List<Integer> parent2) {
        Random rnd = new Random();
        int size = parent1.size();
        int startPos = rnd.nextInt(size - 2) + 1;
        int endPos = rnd.nextInt(size - startPos) + startPos;

        List<Integer> child = new ArrayList<>();
        child.add(0);
        for (int i = startPos; i < endPos; i++) {
            child.add(parent1.get(i));
        }
        for (int gene : parent2) {
            if (!child.contains(gene)) {
                child.add(gene);
            }
        }
        child.add(0);

        return child;
    }
    
    // Mutates the child route with a given mutation rate.
    // Swap stops within a route, transforming the original route into the mutated route 
    private static void mutate(List<Integer> route) {
        Random rnd = new Random();
        for (int i = 1; i < route.size() - 1; i++) {
            if (Math.random() < MUTATION_RATE) {
                int index1 = 1 + rnd.nextInt(route.size() - 2);
                int index2 = 1 + rnd.nextInt(route.size() - 2);
                int temp = route.get(index1);
                route.set(index1, route.get(index2));
                route.set(index2, temp);
            }
        }
    }
    
    // Evaluates the fitness of a route based on total distance traveled.
    private static double evaluateRoute(List<Integer> route, double[][] distances) {
        double fitness = 0;
        for (int i = 0; i < route.size() - 1; i++) {
            fitness += distances[route.get(i)][route.get(i + 1)];
        }
        return fitness;
    }
    
    // Runs the genetic algorithm for a given parcel list.
    public static List<Integer> runGeneticAlgorithmForParcels(ParcelList parcelList) {
        List<Parcel> parcels = parcelList.parcels;
        int numParcels = parcels.size();
        double[][] distances = calculateDistances(parcels);
        List<List<Integer>> population = initializePopulation(10, numParcels);
        return runGeneticAlgorithm(population, distances);
    }
}