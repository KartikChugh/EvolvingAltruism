package io.github.kartikchugh.seekerinteg.entities;

import java.awt.*;

import static io.github.kartikchugh.seekerinteg.main.SeekerPanel.rng;

class GenomeJ {

    private final double[] directions;
    private int geneIndex;
    private Color color;

    // JENETICS edit
    GenomeJ(double[] directions) {
        this.directions = directions;
    }

    /**
     * Forms genome with random genes.
     * @param size number of genes
     */
    GenomeJ(int size) {
        directions = new double[size];
        randomizeDirections();
    }

    // TODO directions constructor?
    /**
     * Copy constructor with mutation chance
     * @param genome genome to copy
     * @param mutationChance chance of mutating a gene
     */
    private GenomeJ(GenomeJ genome, double mutationChance) {
        directions = genome.directions.clone();
        mutateDirections(mutationChance);
    }

    /**
     * Clones this genome with mutated genes
     * @param mutationChance chance of mutating a gene
     * @return mutated genome
     */
    GenomeJ cloned(double mutationChance) {
        return new GenomeJ(this, mutationChance);
    }

    /**
     * Completely randomizes all genes
     */
    private void randomizeDirections() {
        mutateDirections(1.00);
    }

    /**
     * Randomizes genes by chance
     * @param mutationChance chance of randomizing a gene
     */
    private void mutateDirections(double mutationChance) {
        for (int i = 0; i < directions.length; i++) {
            final double mutationRoll = rng.nextDouble();
            if (mutationRoll < mutationChance) {
                directions[i] = rng.nextDouble() * 360.0;
            }
        }
        updateColor();
    }

    /**
     * Provides a color phenotype based on the genes
     */
    private void updateColor() {
        double r = 120, g = 0, b = 120;
        final int del = 10;
        int rights = 0;
        for (double dir : directions) {
            if (dir > 270 || dir < 90) rights++;
            else rights--;
        }
        r += rights*del;
        b -= rights*del;
        r = Math.max(0, Math.min(255, r));
        g = Math.max(0, Math.min(255, g));
        b = Math.max(0, Math.min(255, b));
        color = new Color((int)r, (int)g, (int)b);
    }

    Color getColor() {
        return color;
    }

    /**
     * Indicates whether genome has a new instruction available
     * @return true if available, false otherwise
     */
    boolean hasNextAcc() {
        return geneIndex < directions.length;
    }

    /**
     * Returns an acceleration vector from the current genetic instruction
     * @return array of x and y accelerations
     */
    double[] nextAcc() {
        final double[] acc = new double[2];
        acc[0] = Math.cos(Math.toRadians(directions[geneIndex]));
        acc[1] = Math.sin(Math.toRadians(directions[geneIndex]));
        geneIndex++;
        return acc;
    }

}
