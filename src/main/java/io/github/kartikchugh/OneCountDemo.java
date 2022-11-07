package io.github.kartikchugh;

import io.jenetics.BitChromosome;
import io.jenetics.BitGene;
import io.jenetics.Genotype;
import io.jenetics.Phenotype;
import io.jenetics.engine.Engine;
import io.jenetics.engine.EvolutionInterceptor;
import io.jenetics.engine.EvolutionResult;
import io.jenetics.engine.EvolutionStatistics;
import io.jenetics.util.Factory;
 
public class OneCountDemo {

    // 2.) Definition of the fitness function.
    private static int eval(Genotype<BitGene> gt) {
        return gt.chromosome()
            .as(BitChromosome.class)
            .bitCount();
    }
 
    public static void main(String[] args) {
        // 1.) Define the genotype (factory) suitable for the problem.
        Factory<Genotype<BitGene>> gtf =
            Genotype.of(BitChromosome.of(8, 0.25));
 
        // 3.) Create the execution environment.
        Engine<BitGene, Integer> engine = Engine
            .builder(OneCountDemo::eval, gtf)
            .build();
 
        // 4.) Start the execution (evolution) and collect the result.
        final EvolutionStatistics<Integer,?> statistics = EvolutionStatistics.ofNumber();
        Phenotype<BitGene, Integer> result = engine.stream()
            .limit(10)
            .peek(statistics)
            .collect(EvolutionResult.toBestPhenotype());
 
        System.out.println(result);
        System.out.println(statistics);

    }
}
