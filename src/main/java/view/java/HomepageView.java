package view.java;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.io.IOException;

public class HomepageView {
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
        frame.setContentPane (new HomepageView.MainPanel());
        frame.pack ();
        frame.setLocationRelativeTo (null);
        frame.setVisible (true);
    }

    static class MainPanel extends JPanel {
        private static final String resourceFolder = (System.getProperty("user.dir").toString() + "\\src\\main\\resources\\");
        private JPanel listPane;
        private CardLayout cl = new CardLayout();

        public MainPanel() throws IOException {
            super();

            setLayout(cl);

            JPanel borderLayout = new JPanel(new BorderLayout());

            listPane = new JPanel();
            listPane.setLayout(new BoxLayout(listPane, BoxLayout.X_AXIS));

            JPanel labsPanel = new JPanel();
            labsPanel.setLayout(new BoxLayout(labsPanel, BoxLayout.X_AXIS));

            labsPanel.add(listPane);

            String[] strings = {"csacsa", "cassaf", "sgrereh","csacsa", "cassaf", "sgrereh","csacsa", "cassaf", "sgrereh"};
            JList list = new JList(strings); //data has type Object[]
            list.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
            list.addListSelectionListener(new ListSelectionListener() {
                @Override
                public void valueChanged(ListSelectionEvent e) {
                    System.out.println("ELEMENTO CLICCATO: " + list.getSelectedValue());
                }
            });
            list.setLayoutOrientation(JList.VERTICAL_WRAP);
            list.setVisibleRowCount(-1);

            JScrollPane listScroller = new JScrollPane(list);
            listScroller.setPreferredSize(new Dimension(500, 200));

            labsPanel.add(listScroller);

            JButton butCreate = new JButton("Create game");
            JButton butSubmit = new JButton("Submit");
            JButton butRefresh = new JButton("Refresh...");

            JTextField redVal = new JTextField(20);

            JPanel butPanelSouth = new JPanel();
            JPanel butPanelNorth = new JPanel();

            butPanelSouth.add(redVal);
            butPanelSouth.add(butCreate);

            butPanelNorth.add(butRefresh);
            butPanelNorth.add(butSubmit);

            JPanel loginPanel = new JPanel();
            loginPanel.setLayout(new BoxLayout(loginPanel, BoxLayout.Y_AXIS));

            loginPanel.add(new JTextField("Inserisci il nome"));
            loginPanel.add(new JTextField("Inserisci la pass"));
            JButton loginButton = new JButton("Accedi");
            loginPanel.add(loginButton);
            loginButton.addActionListener((ev) -> {
                changeView();
            });

            borderLayout.add(labsPanel, BorderLayout.CENTER);
            borderLayout.add(butPanelNorth, BorderLayout.NORTH);
            borderLayout.add(butPanelSouth, BorderLayout.SOUTH);

            add(loginPanel, "empty");
            add(borderLayout, "listpane");
        }

        private void changeView() {
            cl.show(this, "listpane");
        }
    }
}
