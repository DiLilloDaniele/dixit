package grpcService.client.actors.behaviors

import akka.actor.typed.{ActorRef, Behavior, Terminated, MailboxSelector, PostStop }
import akka.actor.typed.receptionist.Receptionist
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import grpcService.client.actors.ClusterListener.Event
import grpcService.client.actors.{ClusterListener}
import grpcService.client.actors.utils.Message
import grpcService.client.actors.behaviors.ForemanBehavior.*
import grpcService.client.actors.behaviors.PlayerBehavior
import grpcService.client.actors.behaviors.PlayerBehavior.{TurnCancelled, CardChoosenByOther, CardRevealed, CardsSubmittedByOthers, YourTurn}
import grpcService.client.model.PlayerSelection
import grpcService.client.model.PumpMyList.*
import grpcService.client.model.GameSettings.{MAX_CARDS, MAX_TURNS}
import scala.collection.mutable.ListBuffer

import concurrent.duration.DurationInt
import scala.language.postfixOps
import scala.util.Random

object ForemanBehavior:

  import grpcService.client.actors.ClusterListener

  sealed trait Command extends Message

  case class Start(playersList: Set[ActorRef[PlayerBehavior.Command]]) extends Command
  case class CardToGuess(val cardId: String, val title: String, val replyTo: ActorRef[PlayerBehavior.Command]) extends Command
  case class SelectionToApply(val cardId: String, val replyTo: ActorRef[PlayerBehavior.Command]) extends Command
  case class GuessSelection(val cardId: String, val replyTo: ActorRef[PlayerBehavior.Command]) extends Command
  case class NewTurn(turnIndex: Int) extends Command
  case class PlayerRejoined(address: String) extends Command
  case object Stop extends Command
  case object RestartTurn extends Command
  case object InterruptTurn extends Command

  case class PlayerItem(player: ActorRef[PlayerBehavior.Command], score: Int, cards: ListBuffer[String])

  val list = LazyList.iterate(1) { i => i + 1}.take(100).map { i => s"$i"}.toList

  @main def test =
    var myList = Random.shuffle(list)
    LazyList.iterate(1) { i => i + 1}.take(3).foreach { i =>
      val myNewList = myList.take(10)
      myList = myList.drop(10)
      println(myNewList)
      println("----")
    }
  def apply(logger: Option[ActorRef[Command]] = Option.empty,
            maxPlayers: Int = 3, closeHandler: () => Unit = () => (), timeoutMillis: Int = 300000): Behavior[Command] =
    Behaviors.setup[Command] { ctx =>
      val rootActorRef = ctx.self
      val props = MailboxSelector.fromConfig("akka.prio-dispatcher")
      val master = ctx.spawn(Behaviors.setup[Command | Receptionist.Listing](ctx => new ForemanBehaviorImpl(ctx, rootActorRef, MAX_TURNS)), "root")
      val listener = ctx.spawn(Behaviors.setup[PlayersManagerBehavior.Command](ctx => PlayersManagerBehavior(maxPlayers, rootActorRef, timeoutMillis)), "event-listener", props)
      val clusterManager = ctx.spawn(Behaviors.setup[ClusterListener.Command | Event | Receptionist.Listing](ctx => ClusterListener(listener)), "cluster-listener")
      ctx.watch(listener)

      Behaviors.receiveMessage[Command] {
        case start: Start => 
          closeHandler()
          logger match {
            case Some(ref) => ref ! start
            case _ =>
          }
          master ! start
          Behaviors.same
        case msg =>
          logger match {
            case Some(ref) => ref ! msg
            case _ =>
          }
          master ! msg
          Behaviors.same
      }.receiveSignal { 
        case (ctx, t @ Terminated(_)) =>
          ctx.log.info("System terminated (FOREMAN). Shutting down")
          closeHandler()
          // master e event-listener ! close
          logger match {
              case Some(ref) => ref ! Stop
              case _ =>
            }
          Behaviors.stopped
        case (ctx, PostStop) =>
          ctx.log.info("Master Control Program stopped")
          Behaviors.stopped
      }
    }

class ForemanBehaviorImpl(context: ActorContext[Command | Receptionist.Listing], root: ActorRef[Command], maxTurns: Int) extends AbstractBehavior[Command | Receptionist.Listing](context):

  var playersList: List[PlayerItem] = List()
  var playersSelection: List[PlayerSelection[PlayerBehavior.Command]] = List()
  var playersGuessing: List[PlayerSelection[PlayerBehavior.Command]] = List()
  var myList = Random.shuffle(list)
  var turnsFinished = 0

  override def onMessage(msg: Command | Receptionist.Listing): Behavior[Command | Receptionist.Listing] = msg match {
    case Start(players) =>
      context.log.info("START: " + players.toString)
      playersList = players.map { 
        i => PlayerItem(i, 0, ListBuffer.empty ++= pickCards(MAX_CARDS))
      }.toList
      playersList.foreach { p =>
        context.log.info("INVIO LE CARTE AI PLAYER")
        p.player ! PlayerBehavior.CardsAssigned(p.cards.toList)
      }
      players.head ! YourTurn(root)
      manageTurnOf(players.head, 0)
    case msg: Receptionist.Listing =>
      Behaviors.same

    case Stop => Behaviors.stopped

    case m =>
      context.log.info("Unknown message: " + m.toString)
      Behaviors.same
  }

  def manageTurnOf(actor: ActorRef[PlayerBehavior.Command], turn: Int): Behavior[Command | Receptionist.Listing] = Behaviors.receiveMessage {
    case CardToGuess(card, title, replyTo) if replyTo.path == actor.path =>
      playersSelection = List()
      println("INVIO LE CARTEEEE")
      context.log.info(playersList.toString())

      playersList.foreach { i =>
        if(i.player.path != actor.path)
          println("INVIATOOO")
          i.player ! CardChoosenByOther(title, root)
        else
          i.cards -= card
      }
      waitForSelections(actor, turn, card)
    
    // notify that the turn is interrupted
    case InterruptTurn =>
      playersList foreach { i =>
        i.player ! TurnCancelled
      }
      Behaviors.same

    case PlayerRejoined(address) =>
      playerRejoinedProcedure(address, MAX_CARDS)
      actor ! YourTurn(root)
      Behaviors.same

    case RestartTurn =>
      restartTurnProcedure(actor)
      Behaviors.same

    case Stop => Behaviors.stopped
      
    case _ => Behaviors.same
  }
  
  def waitForSelections(actor: ActorRef[PlayerBehavior.Command], turn: Int, cardSelected: String): Behavior[Command | Receptionist.Listing] = Behaviors.receiveMessage {
    case SelectionToApply(cardId, replyTo) =>
      context.log.info("UNA SCELTA RICEVUTA")
      playersSelection = playersSelection :+ PlayerSelection(cardId, replyTo)
      // remove the chosen card from the narrator's hand
      playersList foreach { i =>
        if(i.player.path == replyTo.path)
          i.cards -= cardId
      }

      if(playersSelection.size < (playersList.size - 1))
        Behaviors.same
      else
        context.log.info("RICEVUTO LE CARTE DA TUTTI")
        val playersSubmissions = playersSelection.map { i => i.card }.toList :+ cardSelected
        playersList foreach { i =>
          if(i.player.path != actor.path)
            i.player ! CardsSubmittedByOthers(playersSubmissions)
        }
        waitForGuessing(actor, turn, cardSelected)

    case InterruptTurn =>
      playersList foreach { i =>
        i.player ! TurnCancelled
      }
      Behaviors.same

    case PlayerRejoined(address) =>
      playerRejoinedProcedure(address, MAX_CARDS)
      Behaviors.same
    
    case RestartTurn =>
      restartTurnProcedure(actor)
      manageTurnOf(actor, turn)

    case Stop => Behaviors.stopped

    case _ => Behaviors.same
  }

  def waitForGuessing(actor: ActorRef[PlayerBehavior.Command], turn: Int, cardSelected: String): Behavior[Command | Receptionist.Listing] = Behaviors.receiveMessage {
    case GuessSelection(cardId, replyTo) =>
      context.log.info("UNA GUESS RICEVUTA")
      playersGuessing = playersGuessing :+ PlayerSelection(cardId, replyTo)
      if(playersGuessing.size < (playersList.size - 1))
        Behaviors.same
      else
        context.log.info("GUESSED LE CARTE DA TUTTI")
        executeResults(playersGuessing, cardSelected, actor)

        val newTurn = turn match
          case m if m == (playersList.size - 1) => 0
          case _ => turn + 1

        if(turnsFinished >= (maxTurns - 1) || playersList.map { i => i.score }.contains(30))
          context.log.info("FINE GIOCO")
          playersList foreach { i =>
            i.player ! PlayerBehavior.EndGame(i.score)
          }
          Behaviors.stopped
        else
          Behaviors.withTimers { timers =>
            timers.startSingleTimer(NewTurn(newTurn), 5000 milliseconds)
            Behaviors.same
          }

    case NewTurn(turnIndex) =>
      turnsFinished = turnsFinished + 1
      // clean playersGuessing choices
      playersGuessing = List()
      playersList foreach { i =>
        val newCard: String = pickCards(1).head.toString
        i.cards += newCard
        i.player ! PlayerBehavior.NewCard(newCard)
      }
      playersList(turnIndex).player ! YourTurn(root)
      manageTurnOf(playersList(turnIndex).player, turnIndex)

    case InterruptTurn =>
      playersList.foreach { i =>
        i.player ! TurnCancelled
      }
      Behaviors.same

    case PlayerRejoined(address) =>
      playerRejoinedProcedure(address, MAX_CARDS)
      Behaviors.same
    
    case RestartTurn =>
      restartTurnProcedure(actor)
      manageTurnOf(actor, turn)

    case Stop => Behaviors.stopped
      
    case _ => Behaviors.same
  }
  
  def playerRejoinedProcedure(address: String, numCarte: Int): Unit =
    context.log.info("REINVIO LE CARTE A " + address)
    playersList.foreach { p =>
      if(p.player.path.address.toString == address)
        p.player ! PlayerBehavior.CardsAssigned(p.cards.toList)
    }

  def restartTurnProcedure(actor: ActorRef[PlayerBehavior.Command]): Unit = 
    context.log.info("RESTART DEL TURNO")
    // restart of the current turn and drawing a cart for each player (because of interrupted turn)
    playersList.foreach { p =>
      if(p.player.path.address.toString == actor.path.address.toString)
        val newCard: String = pickCards(1).head.toString
        p.cards += newCard
        p.player ! PlayerBehavior.NewCard(newCard)
    }
    actor ! YourTurn(root)

  def pickCards(numCarte: Int): List[String] =
    val myNewList = myList.take(numCarte)
    myList = myList.drop(numCarte)
    myNewList

  def executeResults(playersCards: List[PlayerSelection[PlayerBehavior.Command]], cardChosen: String, foremanPlayer: ActorRef[PlayerBehavior.Command]) =
    val totalVotes = playersCards.filter { p =>
      p.card == cardChosen
    }

    // gives points to users
    if(totalVotes.size == (playersList.size - 1) || totalVotes.isEmpty)
      playersList = playersList.filterMap { i =>
        i.player.path.address != foremanPlayer.path.address
      } { i => PlayerItem(i.player, i.score + 2, i.cards) }
    else
      playersList = playersList.filterMap { i =>
        i.player.path.address == foremanPlayer.path.address || totalVotes.exists { c => c.player == i.player }
      } { i => PlayerItem(i.player, i.score + 3, i.cards) }

    playersList foreach { i =>
      i._1 ! CardRevealed(cardChosen)
    }
