package io.github.kartikchugh.altruism;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.Insets;
import java.awt.GraphicsEnvironment;

public class Altruism {
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(Altruism::init);
    }

    private static void init() {
        new AltruismPanel(computeSize());
        
/*         final JFrame frame = new JFrame("Genetic Altruism");
        frame.getContentPane().add(new AltruismPanel(computeSize()));
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setVisible(true); */

    }

    /**
     * Computes dimensions for the panel.
     * @return length of one side of the square
     */
    public static int computeSize() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Insets bounds = Toolkit.getDefaultToolkit().getScreenInsets(GraphicsEnvironment
                .getLocalGraphicsEnvironment().getDefaultScreenDevice()
                .getDefaultConfiguration());
        // Heuristic - height minus twice the taskbar size
        final double size = screenSize.getHeight() - 2 * (bounds.top + bounds.bottom);
        return (int) size;
    }

}
