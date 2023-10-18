package grpcService.client.actors.behaviors

import akka.actor.typed.{ActorRef, Behavior, Terminated}
import akka.actor.typed.receptionist.Receptionist
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import grpcService.client.actors.ClusterListener.Event
import grpcService.client.actors.{ClusterListener, Message}
import grpcService.client.actors.behaviors.ForemanBehavior.{CardToGuess, Command, GuessSelection, NewTurn, PlayerRejoined, SelectionToApply, Start, list}
import grpcService.client.actors.behaviors.PlayerBehavior
import grpcService.client.actors.behaviors.PlayerBehavior.{CardChoosenByOther, CardRevealed, CardsSubmittedByOthers, NotYourTurn, YourTurn}
import grpcService.client.model.PlayerSelection
import grpcService.client.model.PumpMyList.filterMap
import grpcService.client.model.GameSettings.{MAX_CARDS, MAX_TURNS}

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
            maxPlayers: Int = 3): Behavior[Command] =
    Behaviors.setup[Command] { ctx =>
      val rootActorRef = ctx.self
      val master = ctx.spawn(Behaviors.setup[Command | Receptionist.Listing](ctx => new ForemanBehaviorImpl(ctx, rootActorRef, MAX_TURNS)), "root")
      val listener = ctx.spawn(Behaviors.setup[PlayersManagerBehavior.Command](ctx => PlayersManagerBehavior(maxPlayers, rootActorRef)), "event-listener")
      val clusterManager = ctx.spawn(Behaviors.setup[ClusterListener.Command | Event | Receptionist.Listing](ctx => ClusterListener(listener)), "cluster-listener")
      ctx.watch(listener)

      Behaviors.receiveMessage[Command] {
        case msg =>
          logger match {
            case Some(ref) => ref ! msg
            case _ =>
          }
          master ! msg
          Behaviors.same
      }.receiveSignal { case (ctx, t @ Terminated(_)) =>
        ctx.log.info("System terminated (FOREMAN). Shutting down")
        // master e event-listener ! close
        Behaviors.stopped
      }
    }

class ForemanBehaviorImpl(context: ActorContext[Command | Receptionist.Listing], root: ActorRef[Command], maxTurns: Int) extends AbstractBehavior[Command | Receptionist.Listing](context):

  var playersList: List[(ActorRef[PlayerBehavior.Command], Int)] = List()
  var playersSelection: List[PlayerSelection[PlayerBehavior.Command]] = List()
  var playersGuessing: List[PlayerSelection[PlayerBehavior.Command]] = List()
  var myList = Random.shuffle(list)
  var turnsFinished = 0

  override def onMessage(msg: Command | Receptionist.Listing): Behavior[Command | Receptionist.Listing] = msg match {
    case Start(players) =>
      context.log.info("START: " + players.toString)
      playersList = players.map { i => Tuple2(i, 0)}.toList
      playersList.foreach { p =>
        context.log.info("INVIO LE CARTE AI PLAYER")
        p._1 ! PlayerBehavior.CardsAssigned(pickCards(MAX_CARDS))
      }
      players.head ! YourTurn(root)
      manageTurnOf(players.head, 0)
      // context.system.receptionist ! Receptionist.Subscribe(PlayerBehavior.Service, context.self)
    case msg: Receptionist.Listing =>
      // val services: List[ActorRef[PlayerBehavior.Command]] = msg.serviceInstances(PlayerBehavior.Service).toList
      Behaviors.same
    case m =>
      context.log.info("Unknown message: " + m.toString)
      Behaviors.same
  }
  // TODO fine gioco
  def manageTurnOf(actor: ActorRef[PlayerBehavior.Command], turn: Int): Behavior[Command | Receptionist.Listing] = Behaviors.receiveMessage {
    case CardToGuess(card, title, replyTo) if replyTo.path == actor.path =>
      playersSelection = List()
      println("INVIO LE CARTEEEE")
      context.log.info(playersList.toString())
      playersList.map { i => i._1 }.foreach { i =>
        if(i.path != actor.path)
          println("INVIATOOO")
          i ! CardChoosenByOther(title, root)
      }
      waitForSelections(actor, turn, card)
    case CardToGuess(card, title, replyTo) =>
      replyTo ! NotYourTurn
      Behaviors.same

    case PlayerRejoined(address) =>
      playerRejoinedProcedure(address, maxTurns - turnsFinished)
      actor ! YourTurn(root)
      Behaviors.same
      
    case _ => Behaviors.same
  }
  
  def waitForSelections(actor: ActorRef[PlayerBehavior.Command], turn: Int, cardSelected: String): Behavior[Command | Receptionist.Listing] = Behaviors.receiveMessage {
    case SelectionToApply(cardId, replyTo) =>
      context.log.info("UNA SCELTA RICEVUTA")
      playersSelection = playersSelection :+ PlayerSelection(cardId, replyTo)
      if(playersSelection.size < (playersList.size - 1))
        Behaviors.same
      else
        context.log.info("RICEVUTO LE CARTE DA TUTTI")
        val playersSubmissions = playersSelection.map { i => i.card }.toList :+ cardSelected
        playersList foreach { i =>
          i._1 ! CardsSubmittedByOthers(playersSubmissions)
        }
        waitForGuessing(actor, turn, cardSelected)

    case PlayerRejoined(address) =>
      playerRejoinedProcedure(address, maxTurns - turnsFinished)
      actor ! YourTurn(root)
      manageTurnOf(actor, turn)

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

        if(turnsFinished < (maxTurns - 1))
          Behaviors.withTimers { timers =>
            timers.startSingleTimer(NewTurn(newTurn), 5000 milliseconds)
            Behaviors.same
          }
        else
          context.log.info("FINE GIOCO")
          playersList foreach { i =>
            i._1 ! PlayerBehavior.EndGame(i._2)
          }
          Behaviors.stopped

    case NewTurn(turnIndex) =>
      turnsFinished = turnsFinished + 1
      playersList foreach { i =>
        i._1 ! PlayerBehavior.NewCard(pickCards(1).head)
      }
      playersList(turnIndex)._1 ! YourTurn(context.self)
      manageTurnOf(playersList(turnIndex)._1, turnIndex)

    case PlayerRejoined(address) =>
      playerRejoinedProcedure(address, maxTurns - turnsFinished)
      actor ! YourTurn(root)
      manageTurnOf(actor, turn)
      
    case _ => Behaviors.same
  }
  
  def playerRejoinedProcedure(address: String, numCarte: Int): Unit =
    playersList.foreach { p =>
      context.log.info("REINVIO LE CARTE A " + address)
      if(p._1.path.address.toString == address)

        p._1 ! PlayerBehavior.CardsAssigned(pickCards(numCarte))
    }

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
        i._1.path.address != foremanPlayer.path.address
      } { i => Tuple2(i._1, i._2 + 2) }
    else
      playersList = playersList.filterMap { i =>
        i._1.path.address == foremanPlayer.path.address || totalVotes.exists { c => c.player == i._1 }
      } { i => Tuple2(i._1, i._2 + 3) }

    playersList foreach { i =>
      i._1 ! CardRevealed(cardChosen)
    }
