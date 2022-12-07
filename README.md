# Evolving Altruism

There are three subpackages in src/main/java/io/github/kartikchugh, the first of which is "seeker". This package is the Heat Seeker genetic algorithm app I mentioned in class. `SeekerPanel.java` creates thousands of agents with a genome composed simply of a few hundred acceleration vectors that are utilized to move. The agents are evaluated on their resulting distance from the goal at the top of the screen. Also, the agents are colored based on the average direction of the vectors to visualize descendance. 

Run `Seeker.java` to begin the program.

The second package, "seekerinteg" swaps the logic I wrote under-the-hood for an evolution engine powered by Jenetics. Due to Jenetics using the Streams API, the most straightforward way to do this was for `SeekerPanel.java` to precompute a set number of generations beforehand, and then visualize those, instead of doing it all in tandem. The convergenccfe happens very slowly due to using default parameters, but would outperform my manual simulation with some tweaking (mutation rate, recombination mechanism, selection criteria). 

Run `Seeker.java` to begin the program.

The third package is where I used my practice using Jenetics to code my core project idea. `AltruismPanel.java` is well-documented throughout, and core evolution parameters have been refactored to the top of the file for easy tweaking. The program models how altruistic vs. egotistic organisms might react when a few pick up on an imminent threat - do they sacrifice themselves for others near them, and when do they do so? The first question depends on the altruism gene, the second question on the conditionality gene (which has no effect for non-altruists aka egotists).

Read details of the simulation, including results, [here](https://docs.google.com/presentation/d/1PmAkJHpttKFHP-Z-TV0nmraQja-rtjrhAd6wmDtteE8/edit?usp=sharing).

Run `Altruism.java` to begin the program - note that GUI/rendering code has been commented out due to not being able to complete visualization on time.
