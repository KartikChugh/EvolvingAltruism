package io.github.kartikchugh.altruism;

import javax.swing.JPanel;

import io.jenetics.engine.*;
import io.jenetics.util.*;
import io.jenetics.*;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.stream.Collectors;
import java.awt.Color;

public class AltruismPanel extends JPanel {

    // Evolution Parameters
    private static final int POPULATION_SIZE = 48;
    private static final int CLUSTER_SIZE = 2;                          // Individuals per cluster
    private static final boolean CONDITIONAL_ALTRUISM_ENABLED = true;   // When true, GENE_CONDITIONAL_WHEN_ALTRUIST interacts with GENE_ALTRUISM
    private static final double PROBABILITY_ACTIVE_ALLELE = 0.5;        // Odds of a gene having the active allele
    private static final int GENERATIONS = 40;

    // Gene Locations
    private static final int GENE_ALTRUISM = 0;
    private static final int GENE_CONDITIONAL_WHEN_ALTRUIST = 1;



    AltruismPanel(int size) {
        // initGUI(size);
        runSimulation();
    }

    private boolean isAltruist(Phenotype<BitGene, ?> phenotype) {
        return phenotype.genotype().chromosome().get(GENE_ALTRUISM).booleanValue();
    }

    private boolean isAltruist(List<Phenotype<BitGene, Integer>> phenotypes) {
        return phenotypes.stream().anyMatch(pt -> pt.genotype().chromosome().get(GENE_ALTRUISM).booleanValue());
    }

    private boolean isConditionalWhenAltruist(Phenotype<BitGene, ?> phenotype) {
        return phenotype.genotype().chromosome().get(GENE_CONDITIONAL_WHEN_ALTRUIST).booleanValue();
    }

    /**
     * The evaluator is technically just the fitness function. 
     * But, because it is the most straightforward way of operating on a population before selection, 
     * I also use it to conduct the simulation.
     */
    private Evaluator<BitGene, Integer> evaluator = population -> {
        
        var pop = new ArrayList<>(population.asList());
        var survival = new BitSet(pop.size());

        // Loop through all individuals, incrementing by the cluster size
        for (int i = 0; i+CLUSTER_SIZE-1 < pop.size(); i+=CLUSTER_SIZE) {

            // Form cluster of individuals. One is observant of the threat, the others are oblivious
            var observant = pop.get(i);
            var oblivious = pop.subList(i+1, i+CLUSTER_SIZE);

            // Check rules for performing altruistic behavior.
            boolean altruismSatisfied = isAltruist(observant);
            boolean conditionalitySatisfied = !CONDITIONAL_ALTRUISM_ENABLED || !isConditionalWhenAltruist(observant) || isAltruist(oblivious);

            if (altruismSatisfied && conditionalitySatisfied) { 
                // Altruism: Observant saves the oblivious
                survival.set(i+1, i+CLUSTER_SIZE);
            } else {
                // Egotism: Observant saves itself
                survival.set(i);
            }
        }

        // Set survival status of each individual
        for (int i = 0; i < pop.size(); i++) {
            pop.set(i, pop.get(i).withFitness(survival.get(i) ? 1 : 0));
        }
        return ISeq.of(pop);
    };

    /**
     * The interceptor is how we print each generation's population composition in the format: 
     * egotists, naive altruists, conditional altruists
     */
    private EvolutionInterceptor<BitGene, Integer> evolutionInterceptor = EvolutionInterceptor.ofBefore(result -> {
        int naiveAltruismCount = 0;
        int conditionalAltruismCount = 0;
        for (var pt : result.population()) {
            if (isAltruist(pt)) {
                if (CONDITIONAL_ALTRUISM_ENABLED && isConditionalWhenAltruist(pt)) {
                    conditionalAltruismCount++;
                } else {
                    naiveAltruismCount++;
                }
            }
        }
        int keeperCount = POPULATION_SIZE - naiveAltruismCount - conditionalAltruismCount;
        System.out.println(keeperCount + "," + naiveAltruismCount + "," + conditionalAltruismCount);
        return result;
    });

    /**
     * Here, we run the program. First, create a factory to produce new individuals. Second, create the parameterized
     * evolution engine. Third, run the engine for specified number of limits.
     */
    private void runSimulation() {

        if (POPULATION_SIZE % CLUSTER_SIZE != 0) {
            throw new IllegalArgumentException("Population size must be divisible by cluster size: " + CLUSTER_SIZE);
        }

        Factory<Genotype<BitGene>> gtf = Genotype.of(BitChromosome.of(2, PROBABILITY_ACTIVE_ALLELE));

        Engine<BitGene, Integer> engine = new Engine.Builder<>(evaluator, gtf)
            .selector(new TournamentSelector<>(3))
            .alterers(new Mutator<>(0))
            .populationSize(POPULATION_SIZE)
            .interceptor(evolutionInterceptor)
            .build();

        var res = engine.stream().limit(GENERATIONS).collect(Collectors.toList());
        //System.out.println(res.get(res.size()-1).population());
    }
    
}
