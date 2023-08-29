package view;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class BorderLayoutDemo {

    private static final String resourceFolder = (System.getProperty("user.dir").toString() + "\\src\\main\\resources\\");

    public static void main (String [] a) {
        SwingUtilities.invokeLater (new Runnable () {
            @Override public void run () {
                try {
                    createAndShowGUI ();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }
    private static void createAndShowGUI () throws IOException {
        JFrame frame = new JFrame ("Test");
        frame.setDefaultCloseOperation (JFrame.EXIT_ON_CLOSE);
        frame.setContentPane (new MainPanel());
        frame.pack ();
        frame.setLocationRelativeTo (null);
        frame.setVisible (true);
    }
    static class MainPanel extends JPanel
    {
        private static final String resourceFolder = (System.getProperty("user.dir").toString() + "\\src\\main\\resources\\");
        public MainPanel () throws IOException {
            super (new BorderLayout ());

            BufferedImage myPicture = ImageIO.read(new File(resourceFolder + File.separator + "logojpg.jpg"));
            JLabel picLabel = new JLabel(new ImageIcon(myPicture));

            JLabel nameAndReg = new JLabel ("My details", SwingConstants.CENTER);
            JLabel errorMsg = new JLabel ("The error message", SwingConstants.CENTER);
            nameAndReg.setForeground(Color.blue);

            JPanel labsPanel = new JPanel (new GridBagLayout ());

            labsPanel.add (picLabel);

            GridBagConstraints c = new GridBagConstraints ();
            c.gridy = 1;
            c.insets = new Insets (5, 0, 0, 0);
            labsPanel.add (errorMsg, c);
            c.gridy = 2;
            c.gridx = 2;
            labsPanel.add (nameAndReg, c);

            JButton butSubmit = new JButton("Submit");
            JButton butReset = new JButton("Reset");

            JTextField redVal = new JTextField(20);

            JPanel butPanelSouth = new JPanel ();
            JPanel butPanelNorth = new JPanel ();

            butPanelSouth.add (redVal);
            butPanelSouth.add (butSubmit);

            butPanelNorth.add (butReset);

            add (labsPanel, BorderLayout.CENTER);
            add (butPanelNorth, BorderLayout.NORTH);
            add (butPanelSouth, BorderLayout.SOUTH);
        }
    }
}