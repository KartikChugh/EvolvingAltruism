package io.github.kartikchugh.seeker.entities;

import java.awt.*;
import java.util.Arrays;
import java.util.Comparator;
import java.util.function.Supplier;

import static io.github.kartikchugh.seeker.main.SeekerPanel.rng;

public class Population implements Drawable {

    private Dot[] dots;

    /**
     * Spawns a population of dots.
     * @param size number of dots in the population
     * @param posX starting x position
     * @param posY starting y position
     */
    public Population(int size, int genes, double posX, double posY) {
        dots = new Dot[size];
        for (int i = 0; i < dots.length; i++) {
            dots[i] = new Dot(genes, posX, posY);
        }
    }

    /**
     * Returns whether any of the dots is moving.
     * @return true if any of the dots is moving, false otherwise.
     * @see Dot#isMoving()
     */
    public boolean isMoving() {
        for (Dot dot : dots) {
            if (dot.isMoving()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Updates all moving dots.
     * @see Dot#update()
     */
    public void update() {
        for (Dot dot : dots) {
            if (dot.isMoving()) {
                dot.update();
            }
        }
    }

    @Override
    public void draw(Graphics2D g2d) {
        for (Dot dot : dots) {
            dot.draw(g2d);
        }
    }

    /**
     * Evaluates fitness of each dot and performs reproduction accordingly.
     * @param mutationChance chance of a dot's gene mutating
     */
    public void doNaturalSelection(double mutationChance) {
        evaluateFitness();
        dots = reproduce(mutationChance);
    }

    private void evaluateFitness() {
        for (Dot dot : dots) {
            dot.evaluateFitness();
        }
    }

    /**
     * Produces the next generation of dots from the existing one.
     * @param mutationChance chance of a dot's gene mutating
     * @return array containing next generation
     */
    private Dot[] reproduce(double mutationChance) {
        final double cumulativeFitness = Arrays.stream(dots).mapToDouble(Dot::getFitness).sum();
        //System.out.println(cumulativeFitness);
        final int populationSize = dots.length;
        final Dot[] descendants = new Dot[populationSize];

        includeElite(descendants); // prev gen's "champion"
        for (int i = 0; i < populationSize-1; i++) {
            final Dot parent = selectParent(cumulativeFitness);
            final Dot child = parent.cloned(mutationChance);
            descendants[i] = child;
        }

        return descendants;
    }

    /**
     * Includes most fit dot of existing generation into the next.
     * @param descendants next generation population
     */
    private void includeElite(Dot[] descendants) {
        // Thrown if the dots array is empty, causing the max reduction below to fail
        final Supplier<RuntimeException> emptyPopulation = () -> new IllegalStateException("Population cannot be empty");

        final Dot elite = Arrays.stream(dots).max(Comparator.comparingDouble(Dot::getFitness)).orElseThrow(emptyPopulation);
        final Dot eliteClone = elite.cloned(0); // (perfect) clone to use a new dot instance
        eliteClone.setElite(true);
        descendants[descendants.length-1] = eliteClone; // drawn last to overlay other dots
    }

    /**
     * Selects a dot based on its fitness
     *
     * Implementation: FPS (fitness-proportionate selection)
     * @param cumulativeFitness sum of all fitnesses
     * @return selected dot
     */
    private Dot selectParent(double cumulativeFitness) {
        final double threshold = rng.nextDouble() * cumulativeFitness;

        double runningSum = 0;
        // FIXME biased towards beginning of array
        for (Dot dot : dots) {
            runningSum += dot.getFitness();
            if (runningSum >= threshold) {
                return dot;
            }
        }

        throw new AssertionError("Cumulative fitness fails to meet threshold");
    }
}
