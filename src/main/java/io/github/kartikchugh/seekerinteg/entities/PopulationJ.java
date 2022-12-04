package io.github.kartikchugh.seekerinteg.entities;

import java.awt.*;
import java.util.List;
import java.util.Arrays;
import java.util.Comparator;
import java.util.function.Supplier;

import io.jenetics.DoubleGene;
import io.jenetics.Phenotype;

import static io.github.kartikchugh.seekerinteg.main.SeekerPanel.rng;

public class PopulationJ implements Drawable {

    private DotJ[] dots;

    public PopulationJ(List<Phenotype<DoubleGene, Double>> population, double posX, double posY) {
        dots = new DotJ[population.size()];
        for (int i = 0; i < population.size(); i++) {
            var individual = population.get(i);
            var directions = individual.genotype().chromosome().stream().mapToDouble(DoubleGene::doubleValue).toArray();
            dots[i] = new DotJ(posX, posY, new GenomeJ(directions));
        }
    }

    /**
     * Spawns a population of dots.
     * @param size number of dots in the population
     * @param posX starting x position
     * @param posY starting y position
     */
    public PopulationJ(int size, int genes, double posX, double posY) {
        dots = new DotJ[size];
        for (int i = 0; i < dots.length; i++) {
            dots[i] = new DotJ(genes, posX, posY);
        }
    }

    /**
     * Returns whether any of the dots is moving.
     * @return true if any of the dots is moving, false otherwise.
     * @see DotJ#isMoving()
     */
    public boolean isMoving() {
        for (DotJ dot : dots) {
            if (dot.isMoving()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Updates all moving dots.
     * @see DotJ#update()
     */
    public void update() {
        for (DotJ dot : dots) {
            if (dot.isMoving()) {
                dot.update();
            }
        }
    }

    @Override
    public void draw(Graphics2D g2d) {
        for (DotJ dot : dots) {
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
        for (DotJ dot : dots) {
            dot.evaluateFitness();
        }
    }

    /**
     * Produces the next generation of dots from the existing one.
     * @param mutationChance chance of a dot's gene mutating
     * @return array containing next generation
     */
    private DotJ[] reproduce(double mutationChance) {
        final double cumulativeFitness = Arrays.stream(dots).mapToDouble(DotJ::getFitness).sum();
        //System.out.println(cumulativeFitness);
        final int populationSize = dots.length;
        final DotJ[] descendants = new DotJ[populationSize];

        includeElite(descendants); // prev gen's "champion"
        for (int i = 0; i < populationSize-1; i++) {
            final DotJ parent = selectParent(cumulativeFitness);
            final DotJ child = parent.cloned(mutationChance);
            descendants[i] = child;
        }

        return descendants;
    }

    /**
     * Includes most fit dot of existing generation into the next.
     * @param descendants next generation population
     */
    private void includeElite(DotJ[] descendants) {
        // Thrown if the dots array is empty, causing the max reduction below to fail
        final Supplier<RuntimeException> emptyPopulationJ = () -> new IllegalStateException("PopulationJ cannot be empty");

        final DotJ elite = Arrays.stream(dots).max(Comparator.comparingDouble(DotJ::getFitness)).orElseThrow(emptyPopulationJ);
        final DotJ eliteClone = elite.cloned(0); // (perfect) clone to use a new dot instance
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
    private DotJ selectParent(double cumulativeFitness) {
        final double threshold = rng.nextDouble() * cumulativeFitness;

        double runningSum = 0;
        // FIXME biased towards beginning of array
        for (DotJ dot : dots) {
            runningSum += dot.getFitness();
            if (runningSum >= threshold) {
                return dot;
            }
        }

        throw new AssertionError("Cumulative fitness fails to meet threshold");
    }
}
