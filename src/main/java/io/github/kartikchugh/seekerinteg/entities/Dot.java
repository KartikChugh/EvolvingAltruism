package io.github.kartikchugh.seekerinteg.entities;

import java.awt.*;

import static io.github.kartikchugh.seekerinteg.main.SeekerPanel.*;

public class Dot extends Entity {

    private static final double MAX_VELOCITY = 7.0;

    private final double posX_start;
    private final double posY_start;
    private double velX;
    private double velY;
    private double accX;
    private double accY;

    private double fitness;
    private int steps;
    private boolean moving;
    private boolean reachedGoal;
    private boolean elite;
    private final Genome genome;
    private final Color color;

    /**
     * Spawns a dot.
     * @param posX starting x position
     * @param posY starting y position
     * @param genome genetic instructions
     */
    private Dot(double posX, double posY, Genome genome) {
        super(posX, posY);
        posX_start = posX;
        posY_start = posY;
        this.genome = genome;
        color = genome.getColor();
        initialize();
    }

    /**
     * Spawns a dot with a randomized genome.
     * @param posX starting x position
     * @param posY starting y position
     */
    Dot(int genes, double posX, double posY) {
        this(posX, posY, new Genome(genes));
    }

    /**
     * Clones this dot with a mutated genome
     * @param mutationChance chance of gene mutation
     * @return cloned dot
     */
    Dot cloned(double mutationChance) {
        return new Dot(this.posX_start, this.posY_start, genome.cloned(mutationChance));
    }

    /**
     * Sets default field values upon construction
     */
    private void initialize() {
        setPosX(posX_start);
        setPosY(posY_start);
        velX = 0;
        velY = 0;
        accX = 0;
        accY = 0;
        steps = 0;
        moving = true;
        reachedGoal = false;
        fitness = 0;
    }

    /**
     * Checks if dot can no longer move, otherwise moves it
     */
    public void update() {
        boolean touchingGoal = isTouchingGoal();
        if (touchingGoal) reachedGoal = true;

        if (!genome.hasNextAcc() || isTouchingWall() || touchingGoal) {
            moving = false;
        } else {
            move();
            steps++;
        }
    }

    private boolean isTouchingGoal() {
        return isTouching(goal);
    }

    private boolean isTouchingWall() {
        return (getPosX() < 0 || getPosX() > WIDTH-getDiameter()) || (getPosY() < 0 || getPosY() > HEIGHT-getDiameter());
    }

    /**
     * Retrieves acceleration to update velocity and position
     */
    private void move() {
        final double[] acc = genome.nextAcc();
        accX = acc[0];
        accY = acc[1];

        velX += accX;
        velY += accY;

        velX = clamp(velX);
        velY = clamp(velY);

        changePosX(velX*DT*65);
        changePosY(velY*DT*65);

    }

    /**
     * Evaluates fitness based on distance
     */
    public void evaluateFitness() {
        double minDist = (getDiameter() + goal.getDiameter())/2.0;
        double minDistCost = minDist*minDist;

        final double distCost = Math.max(squareDistanceFrom(goal), minDistCost);
        final double stepCost = steps*steps;

        fitness = 1/distCost;
        if (reachedGoal) {
            fitness += 10000/stepCost;
        }
    }

    boolean isMoving() {
        return moving;
    }

    private double clamp(double num) {
        return Math.max(-MAX_VELOCITY, Math.min(MAX_VELOCITY, num));
    }

    @Override
    protected Color getColor() {
        return elite ? Color.GREEN : color;
    }

    @Override
    protected int getDiameter() {
        return 4;
    }

    @Override
    protected int getRenderDiameter() {
        return elite ? 8 : 4;
    }

    public double getFitness() {
        return fitness;
    }

    public void setElite(boolean elite) {
        this.elite = elite;
    }
}
