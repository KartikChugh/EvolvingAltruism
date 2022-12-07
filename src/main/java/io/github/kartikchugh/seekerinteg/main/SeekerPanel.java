package io.github.kartikchugh.seekerinteg.main;

import io.github.kartikchugh.seekerinteg.entities.Entity;
import io.github.kartikchugh.seekerinteg.entities.Goal;
import io.github.kartikchugh.seekerinteg.entities.PopulationJ;
import io.jenetics.DoubleChromosome;
import io.jenetics.DoubleGene;
import io.jenetics.Genotype;
import io.jenetics.Optimize;
import io.jenetics.Phenotype;
import io.jenetics.RouletteWheelSelector;
import io.jenetics.engine.Engine;
import io.jenetics.engine.EvolutionResult;
import io.jenetics.stat.MinMax;
import io.jenetics.util.Factory;

import javax.swing.*;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.Random;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class SeekerPanel extends JPanel {

    // Evolution Parameters
    private static final double MUTATION_CHANCE = 0.005;
    private static final int POPULATION_SIZE = 1000; // JENETICS edit
    private static final long SEED = -1;
    private static final int RELOCATE_INTERVAL = 15;
    private static int GENOME_LENGTH;

    // Visual Parameters
    public static int WIDTH;
    public static int HEIGHT;
    public static final double DT = 0.01;
    private static final int TPS_DESIRED = 100;
    private static final int TPS_INTERVAL = TPS_DESIRED;

    // Timing
    public static double deltaTime;
    public static double dt_accumulator;
    private int ticks;
    private long t;
    private double dt_interval;
    private int tps;

    public static Random rng;
    public static Goal goal;
    private PopulationJ population;
    private int gen = 0;

    // JENETICS
    private static final double MAX_VELOCITY = 7.0;
    private List<EvolutionResult<DoubleGene, Double>> RESULTS = null;

    private static double eval(Genotype<DoubleGene> gt) {
        List<Double> directions = gt.chromosome()
        .as(DoubleChromosome.class)
        .doubleStream()
        .boxed()
        .collect(Collectors.toList());

        double velX = 0;
        double velY = 0;

        double posX = 0;
        double posY = 0;

        for (int i = 0; i < directions.size(); i++) {
            double accX = Math.cos(Math.toRadians(directions.get(i)));
            double accY = Math.sin(Math.toRadians(directions.get(i)));

            velX += accX;
            velY += accY;

            velX = clamp(velX);
            velY = clamp(velY);

            posX += velX;
            posY += velY;
        }

        double minDist = (getDiameter() + goal.getDiameter())/2.0;
        double minDistCost = minDist*minDist;

        final double distCost = Math.max(squareDistanceFrom(goal, posX, posY), minDistCost);
        //final double stepCost = steps*steps;

        //double fitness = 1/distCost;
        // if (reachedGoal) {
        //     fitness += 10000/stepCost;
        // }

        return distCost; // JENETICS EDIT: minimize the cost
    }

    private static double clamp(double num) {
        return Math.max(-MAX_VELOCITY, Math.min(MAX_VELOCITY, num));
    }

    private static int getDiameter() {
        return 4;
    }

    final static double squareDistanceFrom(Entity o, double posX, double posY) {
        final double dx = getCenterX(posX) - o.getCenterX();
        final double dy = getCenterY(posY) - o.getCenterY();
        return dx*dx + dy*dy;
    }

    private static double getCenterX(double posX) {
        return posX + getDiameter()/2.0;
    }

    private static double getCenterY(double posY) {
        return posY + getDiameter()/2.0;
    }

    SeekerPanel(int size) {
        initGUI(size);
        GENOME_LENGTH = (int)(0.3 * HEIGHT);
        //GENOME_LENGTH = 10; // JENETICS

        goal = new Goal(WIDTH/2.0, 100);
        rng = null;

        // JENETICS
        Factory<Genotype<DoubleGene>> gtf = Genotype.of(DoubleChromosome.of(0, 360, GENOME_LENGTH));
        
        Engine<DoubleGene, Double> engine = Engine
            .builder(SeekerPanel::eval, gtf)
            .selector(new RouletteWheelSelector<>())
            .optimize(Optimize.MINIMUM)
            .populationSize(POPULATION_SIZE)
            .build();

        var results = engine.stream().limit(20).toList();
        RESULTS = results;

        var pop = results.get(0).population().stream().toList();
        population = new PopulationJ(pop, WIDTH/2.0, HEIGHT-100);
        

/*         Phenotype<DoubleGene, Double> result = engine.stream()
            .limit(10)
            //.peek(SeekerPanel::update)
            .collect(EvolutionResult.toBestPhenotype()); */

        

/*         rng = new Random();
        if (SEED != -1) rng.setSeed(SEED); */

        //population = new PopulationJ(POPULATION_SIZE, GENOME_LENGTH, WIDTH/2.0, HEIGHT-100);
        

        t = System.currentTimeMillis();
        final Timer timer = new Timer(1000/TPS_DESIRED, this::tick);
        timer.start();
    }

    private void initGUI(int size) {
        HEIGHT = size;
        WIDTH = size;
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(Color.WHITE);
    }

    private void tick(ActionEvent e) {
        updateTime();
        if (population.isMoving()) {
            while (dt_accumulator >= DT) {
                population.update();
                dt_accumulator -= DT;
            }
            repaint();
        } else {
            gen++;
            var pop = RESULTS.get(gen).population().stream().toList();
            population = new PopulationJ(pop, WIDTH/2.0, HEIGHT-100);
            
/*             population.doNaturalSelection(MUTATION_CHANCE);
            gen++;
            if (gen % RELOCATE_INTERVAL == 0 && gen > 0) {
                goal.relocate(WIDTH * 0.9);
            } */
        }
        ticks++;
    }

    private void updateTime() {
        long t_ = System.currentTimeMillis();
        deltaTime = (t_ - t)/1000.0;
        t = t_;
        dt_accumulator += deltaTime;

        // Re-calculate tps every TPS_INTERVAL milliseconds
        if (ticks % TPS_INTERVAL == 0) {
            tps = (int) (TPS_INTERVAL/dt_interval);
            dt_interval = 0;
        } else {
            dt_interval += deltaTime;
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        final Graphics2D g2d = (Graphics2D) g;

        goal.draw(g2d);
        population.draw(g2d);

        g2d.setColor(Color.BLACK);
        g2d.drawString("Gen: " + gen, 15, 15);
        g2d.drawString("Frames: " + tps, WIDTH-80, 15);
    }
}
