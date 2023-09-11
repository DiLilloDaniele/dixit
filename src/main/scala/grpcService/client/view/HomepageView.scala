package grpcService.client.view

import grpcService.client.controller.{GameController, GameControllerObj}

import java.awt.*
import java.awt.event.{MouseEvent, MouseListener}
import java.awt.image.BufferedImage
import java.io.{File, IOException}
import java.lang.ModuleLayer.Controller
import java.lang.Runnable
import javax.imageio.ImageIO
import javax.swing.*
import javax.swing.event.{ListSelectionEvent, ListSelectionListener}
import scala.util.{Failure, Success}
import grpcService.client.ClientImpl

import java.util

object HomepageView:

  @main def executeView() =
    SwingUtilities.invokeLater(() => {
      createAndShowGui()
    })

  def createAndShowGui() =
    val clientImpl = ClientImpl()
    val controller = GameController(clientImpl)
    val frame = JFrame("Test")
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE)
    frame.setContentPane (HomepageView(controller))
    frame.pack ()
    frame.setLocationRelativeTo (null)
    frame.setVisible (true)

class HomepageView(val gameController : GameController) extends JPanel() {

  val resourceFolder = (System.getProperty("user.dir").toString() + "\\src\\main\\resources\\")
  var listPane = JPanel()
  var cl: CardLayout = CardLayout()

  setLayout(cl)

  val borderLayout = new JPanel(new BorderLayout())
  listPane.setLayout(new BoxLayout(listPane, BoxLayout.X_AXIS))

  val labsPanel = new JPanel()
  labsPanel.setLayout(new BoxLayout(labsPanel, BoxLayout.X_AXIS))

  labsPanel.add(listPane)

  val strings = Array("csacsa", "cassaf", "sgrereh", "csacsa", "cassaf", "sgrereh", "csacsa", "cassaf", "sgrereh")
  var myList: JList[String] = new JList(strings)
  myList.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION)
  myList.addListSelectionListener((e) => {
    println("ELEMENTO CLICCATO: " + myList.getSelectedValue)
  })
  myList.setLayoutOrientation(JList.VERTICAL_WRAP)
  myList.setVisibleRowCount(-1)

  val listScroller: JScrollPane = JScrollPane(myList)
  listScroller.setPreferredSize(Dimension(500, 200))

  labsPanel.add(listScroller)

  val butCreate = JButton("Create game")
  val butSubmit = JButton("Submit")
  val butRefresh = JButton("Refresh...")
  butRefresh.addActionListener((e) => {
    gameController.getGames((list) => {
      println("LISTA GAMES:")
      println(list)
      import javax.swing.DefaultListModel
      val model = new DefaultListModel[String]()
      model.addAll(getJavaList(list))
      myList.setModel(model)
    })
  })

  val redVal = JTextField(20)

  val butPanelSouth = JPanel()
  val butPanelNorth = JPanel()

  butPanelSouth.add(redVal)
  butPanelSouth.add(butCreate)

  butPanelNorth.add(butRefresh)
  butPanelNorth.add(butSubmit)

  val loginPanel = JPanel()
  loginPanel.setLayout(BoxLayout(loginPanel, BoxLayout.Y_AXIS))
  val usernameTextField = JTextField("Inserisci il nome")
  val passwordTextField = JTextField("Inserisci la password")
  loginPanel.add(usernameTextField)
  loginPanel.add(passwordTextField)
  val loginButton = JButton("Accedi")
  val registerButton = JButton("Registrati")
  loginPanel.add(loginButton)
  loginPanel.add(registerButton)
  loginButton.addActionListener((ev) => {
    val username = usernameTextField.getText
    val password = passwordTextField.getText
    gameController.login(username, password, (res) => {
      res match {
        case true =>
          gameController.username = username
          changeView()
        case _ => JOptionPane.showMessageDialog(null, "Utente non registrato")
      }
    })
  })
  registerButton.addActionListener((ev) => {
    val username = usernameTextField.getText
    val password = passwordTextField.getText
    gameController.register(username, password, (res) => {
      res match {
        case true =>
          gameController.username = username
          changeView()
        case _ => JOptionPane.showMessageDialog(null, "Utente non registrato")
      }
    })
  })

  borderLayout.add(labsPanel, BorderLayout.CENTER)
  borderLayout.add(butPanelNorth, BorderLayout.NORTH)
  borderLayout.add(butPanelSouth, BorderLayout.SOUTH)

  add(loginPanel, "empty")
  add(borderLayout, "listpane")

  def apply() = {


  }

  def changeView() =
    cl.show(this, "listpane")

  def getJavaList[A](list: scala.List[A]) =
    val javaList: util.ArrayList[A] = util.ArrayList()
    list.foreach((el) => {
      javaList.add(el)
    })
    javaList


}