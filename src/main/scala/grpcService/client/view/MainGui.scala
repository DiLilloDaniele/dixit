package grpcService.client.view

import java.awt.event.{MouseEvent, MouseListener}
import java.awt.{BorderLayout, Color, Component, GridBagConstraints, GridBagLayout, Insets}
import java.io.File
import javax.imageio.ImageIO
import javax.swing.{BoxLayout, ImageIcon, JButton, JFrame, JLabel, JPanel, JTextField, SwingConstants, SwingUtilities, WindowConstants}

object MainGui:

  @main def create() =
    SwingUtilities.invokeLater(() => {
      createAndShowGui()
    })

  def createAndShowGui() =
    val frame = JFrame("Test")
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE)
    frame.setContentPane (MainGui())
    frame.pack ()
    frame.setLocationRelativeTo (null)
    frame.setVisible (true)

class MainGui extends JPanel(BorderLayout()){

  val resourceFolder = (System.getProperty("user.dir").toString() + "\\src\\main\\resources\\")
  val listPane: JPanel = JPanel()
  val myPicture = ImageIO.read(File(resourceFolder + File.separator + "carte" + File.separator + "1.PNG"))

  listPane.setLayout(BoxLayout(listPane, BoxLayout.X_AXIS))

  val picLabel = new JLabel(new ImageIcon(myPicture))
  picLabel.setAlignmentX(Component.CENTER_ALIGNMENT)
  picLabel.addMouseListener(getListener(1))
  listPane.add(picLabel)
  val picLabel1 = new JLabel(new ImageIcon(myPicture))
  listPane.add(picLabel1)
  val picLabel2 = new JLabel(new ImageIcon(myPicture))
  listPane.add(picLabel2)
  val picLabel3 = new JLabel(new ImageIcon(myPicture))
  listPane.add(picLabel3)

  val nameAndReg = new JLabel("My details", SwingConstants.CENTER)
  val errorMsg = new JLabel("The error message", SwingConstants.CENTER)
  nameAndReg.setForeground(Color.blue)

  val labsPanel = new JPanel(new GridBagLayout)

  labsPanel.add(listPane)

  val c = new GridBagConstraints
  c.gridy = 1
  c.insets = new Insets(5, 0, 0, 0)
  labsPanel.add(errorMsg, c)

  val butSubmit = new JButton("Submit")
  val butReset = new JButton("Reset")

  val redVal = new JTextField(20)

  val butPanelSouth = new JPanel
  val butPanelNorth = new JPanel

  butPanelSouth.add(redVal)
  butPanelSouth.add(butSubmit)

  butPanelNorth.add(butReset)

  add(labsPanel, BorderLayout.CENTER)
  add(butPanelNorth, BorderLayout.NORTH)
  add(butPanelSouth, BorderLayout.SOUTH)

  def getListener(index: Int): MouseListener =
    return new MouseListener {
      override def mouseClicked(e: MouseEvent): Unit = println("IMAGE CLICKED " + index)

      override def mousePressed(e: MouseEvent): Unit = ???

      override def mouseReleased(e: MouseEvent): Unit = ???

      override def mouseEntered(e: MouseEvent): Unit = ???

      override def mouseExited(e: MouseEvent): Unit = ???
    }

}
