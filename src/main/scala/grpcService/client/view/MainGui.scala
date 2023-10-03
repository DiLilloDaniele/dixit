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
    val gui: MainGui = MainGui(null, null, null)
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE)
    frame.setContentPane (gui)
    frame.pack ()
    frame.setLocationRelativeTo (null)
    frame.setVisible (true)
    gui.getImageToChoose(List("1","1","1","1","1"))

class MainGui(val sendImage: (cardId: Int, title: String) => Unit,
              val guessCard: (id: Int) => Unit,
              val guessCardFromMine: (id: Int) => Unit) extends JPanel(BorderLayout()){

  val resourceFolder = (System.getProperty("user.dir").toString() + "\\src\\main\\resources\\")
  val imagesFolder = "carte"
  val listPane: JPanel = JPanel()

  listPane.setLayout(BoxLayout(listPane, BoxLayout.X_AXIS))

  val images = LazyList.iterate(1){i => i + 1}.map { i =>
    (
      i,
      ImageIO.read(File(resourceFolder + File.separator + imagesFolder + File.separator + s"$i.PNG"))
    )}

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

  def resetListPane() =
    listPane.removeAll()

  def getImageForGuessing(cards: List[String], title: String) =
    resetListPane()
    changeWarnText(s"Titolo della carta: $title \n Qual è quella giusta?")
    cards foreach { i =>
      val image = images(i.toInt - 1)
      val picLabel = new JLabel(new ImageIcon(image._2))
      picLabel.setAlignmentX(Component.CENTER_ALIGNMENT)
      picLabel.addMouseListener(getListenerForGuessing(image._1))
      listPane.add(picLabel)
    }

  def getImageForGuessingFromMine(cards: List[String], title: String) =
    resetListPane()
    changeWarnText(s"Titolo della carta: $title \n Scegli quella che reputi idonea")
    cards foreach { i =>
      val image = images(i.toInt - 1)
      val picLabel = new JLabel(new ImageIcon(image._2))
      picLabel.setAlignmentX(Component.CENTER_ALIGNMENT)
      picLabel.addMouseListener(getListenerForGuessingFromMine(image._1))
      listPane.add(picLabel)
    }

  def getImageToChoose(cards: List[String]) =
    resetListPane()
    changeWarnText("Scegli la carta e il titolo per il tuo turno")
    cards foreach { i =>
      val image = images(i.toInt - 1)
      val picLabel = new JLabel(new ImageIcon(image._2))
      picLabel.setAlignmentX(Component.CENTER_ALIGNMENT)
      picLabel.addMouseListener(getListenerForChoose(image._1))
      listPane.add(picLabel)
    }

  def changeWarnText(text: String) =
    SwingUtilities.invokeLater(() => {
      errorMsg.setText(text)
    })

  def getListenerForChoose(index: Int): MouseListener =
    return new MouseListener {
      override def mouseClicked(e: MouseEvent): Unit =
        println("IMAGE CLICKED " + index)
        changeWarnText(s"Attendi...")
        val title = redVal.getText
        sendImage(index, title)

      override def mousePressed(e: MouseEvent): Unit = ()

      override def mouseReleased(e: MouseEvent): Unit = ()

      override def mouseEntered(e: MouseEvent): Unit = ()

      override def mouseExited(e: MouseEvent): Unit = ()
    }

  def getListenerForGuessing(index: Int): MouseListener =
    return new MouseListener {
      override def mouseClicked(e: MouseEvent): Unit =
        println("IMAGE CLICKED " + index)
        changeWarnText(s"Attendi...")
        guessCard(index)

      override def mousePressed(e: MouseEvent): Unit = ()

      override def mouseReleased(e: MouseEvent): Unit = ()

      override def mouseEntered(e: MouseEvent): Unit = ()

      override def mouseExited(e: MouseEvent): Unit = ()
    }

  def getListenerForGuessingFromMine(index: Int): MouseListener =
    return new MouseListener {
      override def mouseClicked(e: MouseEvent): Unit =
        println("IMAGE CLICKED " + index)
        changeWarnText(s"Attendi...")
        guessCardFromMine(index)

      override def mousePressed(e: MouseEvent): Unit = ()

      override def mouseReleased(e: MouseEvent): Unit = ()

      override def mouseEntered(e: MouseEvent): Unit = ()

      override def mouseExited(e: MouseEvent): Unit = ()
    }

  def createAndShowGui() =
    val frame = JFrame("Test")
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE)
    frame.setContentPane (this)
    frame.pack ()
    frame.setLocationRelativeTo (null)
    frame.setVisible (true)

}