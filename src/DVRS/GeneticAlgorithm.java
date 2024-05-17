package DVRS;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GeneticAlgorithm {
    private static final double MUTATION_RATE = 0.1;
    private static final int TOURNAMENT_SIZE = 5;
    private static final int MAX_GENERATIONS = 1000;

    public static List<List<Integer>> initializePopulation(int populationSize, int numParcels) {
        List<List<Integer>> population = new ArrayList<>();
        for (int i = 0; i < populationSize; i++) {
            population.add(randomRoute(numParcels));
        }
        return population;
    }

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

    private static double[][] calculateDistances(List<Parcel> parcels) {
        int numParcels = parcels.size();
        double[][] distances = new double[numParcels + 1][numParcels + 1];
        for (int i = 0; i < numParcels; i++) {
            for (int j = 0; j < numParcels; j++) {
                double distance = Math.sqrt(Math.pow(parcels.get(i).x - parcels.get(j).x, 2) + Math.pow(parcels.get(i).y - parcels.get(j).y, 2));
                distances[i + 1][j + 1] = Math.round(distance * 10.0) / 10.0;
            }
            double originDistance = Math.sqrt(Math.pow(parcels.get(i).x, 2) + Math.pow(parcels.get(i).y, 2));
            distances[0][i + 1] = Math.round(originDistance * 10.0) / 10.0;
            distances[i + 1][0] = distances[0][i + 1];
        }
        return distances;
    }

    public static List<Integer> runGeneticAlgorithm(List<List<Integer>> population, double[][] distances) {
        int generation = 0;
        List<Integer> bestRoute = null;
        double bestFitness = Double.MAX_VALUE;

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

    private static double evaluateRoute(List<Integer> route, double[][] distances) {
        double fitness = 0;
        for (int i = 0; i < route.size() - 1; i++) {
            fitness += distances[route.get(i)][route.get(i + 1)];
        }
        return fitness;
    }

    public static List<Integer> runGeneticAlgorithmForParcels(ParcelList parcelList) {
        List<Parcel> parcels = parcelList.parcels;
        int numParcels = parcels.size();
        double[][] distances = calculateDistances(parcels);
        List<List<Integer>> population = initializePopulation(10, numParcels);
        return runGeneticAlgorithm(population, distances);
    }
}