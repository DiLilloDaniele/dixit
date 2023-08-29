package view;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class MainGui {

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

    /*
    idee: liste di oggetti carte
     */
    static class MainPanel extends JPanel
    {
        private static final String resourceFolder = (System.getProperty("user.dir").toString() + "\\src\\main\\resources\\");
        private JPanel listPane;

        public MainPanel () throws IOException {
            super (new BorderLayout ());

            listPane = new JPanel();
            listPane.setLayout(new BoxLayout(listPane, BoxLayout.X_AXIS));
            BufferedImage myPicture = ImageIO.read(new File(resourceFolder + File.separator + "carte" + File.separator + "1.PNG"));
            JLabel picLabel = new JLabel(new ImageIcon(myPicture));
            picLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            picLabel.addMouseListener(getListener(1));
            listPane.add(picLabel);
            JLabel picLabel1 = new JLabel(new ImageIcon(myPicture));
            listPane.add(picLabel1);
            JLabel picLabel2 = new JLabel(new ImageIcon(myPicture));
            listPane.add(picLabel2);
            JLabel picLabel3 = new JLabel(new ImageIcon(myPicture));
            listPane.add(picLabel3);

            JLabel nameAndReg = new JLabel ("My details", SwingConstants.CENTER);
            JLabel errorMsg = new JLabel ("The error message", SwingConstants.CENTER);
            nameAndReg.setForeground(Color.blue);

            JPanel labsPanel = new JPanel (new GridBagLayout ());

            labsPanel.add (listPane);

            GridBagConstraints c = new GridBagConstraints ();
            c.gridy = 1;
            c.insets = new Insets (5, 0, 0, 0);
            labsPanel.add (errorMsg, c);

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
            //listPane.add(picLabel);
        }

        public void addCard() {

        }

        public void removeCard() {

        }

        public void swapCard(int index) {

        }

        public void swapCards() {

        }
    }

    public static MouseListener getListener(int index) {
        return new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                System.out.println("IMAGE CLICKED: " + index);
            }

            @Override
            public void mousePressed(MouseEvent e) {

            }

            @Override
            public void mouseReleased(MouseEvent e) {

            }

            @Override
            public void mouseEntered(MouseEvent e) {

            }

            @Override
            public void mouseExited(MouseEvent e) {

            }
        };
    }

}
