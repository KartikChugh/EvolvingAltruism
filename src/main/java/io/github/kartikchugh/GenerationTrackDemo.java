package io.github.kartikchugh;

import io.jenetics.BitChromosome;
import io.jenetics.BitGene;
import io.jenetics.Genotype;
import io.jenetics.Phenotype;
import io.jenetics.engine.Engine;
import io.jenetics.engine.EvolutionResult;
import io.jenetics.engine.EvolutionStatistics;
import io.jenetics.util.Factory;

public class GenerationTrackDemo {
    
    private static void update(final EvolutionResult<BitGene, Integer> result) {
        final EvolutionStatistics<Integer,?> statistics = EvolutionStatistics.ofNumber();
        
        System.out.println("Generation: " + result.generation());
        System.out.println(result.population());
        System.out.println("===================");
    }

    private static int eval(Genotype<BitGene> gt) {
        return gt.chromosome()
            .as(BitChromosome.class)
            .bitCount();
    }

    public static void main(String[] args) {

        Factory<Genotype<BitGene>> gtf =
            Genotype.of(BitChromosome.of(8, 0.25));

        Engine<BitGene, Integer> engine = Engine
            .builder(GenerationTrackDemo::eval, gtf)
            .build();

        Phenotype<BitGene, Integer> result = engine.stream()
            .limit(3)
            .peek (GenerationTrackDemo::update)
            .collect(EvolutionResult.toBestPhenotype());
 
        System.out.println(result);

    }

}
