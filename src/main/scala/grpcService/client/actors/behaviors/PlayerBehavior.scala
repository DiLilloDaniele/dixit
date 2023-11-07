package grpcService.client.actors.behaviors

import akka.actor.typed.{ActorRef, Behavior, Terminated}
import akka.actor.typed.receptionist.{Receptionist, ServiceKey}
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import grpcService.client.actors.Message
import grpcService.client.actors.behaviors.PlayerBehavior.*
import grpcService.client.actors.behaviors.ForemanBehavior
import grpcService.client.actors.PlayerClusterListener.Event
import grpcService.client.actors.{PlayerClusterListener, Message}

import concurrent.duration.DurationInt
import scala.language.postfixOps
import scala.util.Random

object PlayerBehavior:

  val Service = ServiceKey[Command]("PlayersService")

  sealed trait Command extends Message
  //entry messages
  case class AskToEnter(replyTo: ActorRef[PlayersManagerBehavior.Command]) extends Command
  case object MemberOK extends Command
  case object MemberKO extends Command
  case object Start extends Command
  case class TurnOf(val player: String) extends Command
  case class GameInfo(msg: String) extends Command

  //turn messages
  case class YourTurn(val replyTo: ActorRef[ForemanBehavior.Command]) extends Command

  //guess messages
  case class CardChoosenByOther(val title: String, val replyTo: ActorRef[ForemanBehavior.Command]) extends Command
  case class CardsSubmittedByOthers(val cards: List[String]) extends Command
  case class CardRevealed(val cardId: String) extends Command
  case class CardChoosenForTurn(val cardId: String, val title: String) extends Command
  case object EndTurn extends Command
  case class CardsAssigned(cardsId: List[String]) extends Command
  case class SingleCardSubmitted(cardId: String) extends Command
  case class GuessedCardbyUser(cardId: String) extends Command
  case object TurnCancelled extends Command
  case class EndGame(points: Int) extends Command
  case class NewCard(card: String) extends Command

  def apply(logger: Option[ActorRef[Command]] = Option.empty, interactionExt: Option[ActorRef[InteractionBehavior.Command]] = Option.empty, onStop: (Int) => Unit = (v) => ()): Behavior[Command] =
    Behaviors.setup[Command] { ctx =>
      ctx.system.receptionist ! Receptionist.Register(Service, ctx.self)

      val interaction = interactionExt match {
        case Some(ref) =>
          ctx.log.info("INTERACTION ESTERNO")
          ref
        case _ =>
          ctx.log.info("INTERACTION INTERNO")
          ctx.spawn(Behaviors.setup[InteractionBehavior.Command](ctx1 => InteractionGuiImpl(ctx1, ctx.self)), "gui")
      }
      val player = ctx.spawn(Behaviors.setup[Command | Receptionist.Listing](context => new PlayerBehaviorImpl(context, interaction, ctx.self, onStop)), "player")
      val listener = ctx.spawn(Behaviors.setup[Event | Receptionist.Listing](context => PlayerClusterListener(ctx.self)), "player-cluster-listener")
      player ! Start
      ctx.watch(interaction)

      Behaviors.receiveMessage[Command] {
        case GameInfo(msg) =>
          interaction ! InteractionBehavior.MessageError(msg)
          logger match {
            case Some(ref) => ref ! GameInfo(msg)
            case _ =>
          }
          Behaviors.same
        case msg: Command =>
          ctx.log.info("MESSAGGIO RICEVUTO: " + msg)
          logger match {
            case Some(ref) => ref ! msg
            case _ =>
          }
          player ! msg
          Behaviors.same
      }.receiveSignal { case (ctx, t @ Terminated(_)) =>
        ctx.log.info("System terminated [PLAYER]. Shutting down")
        Behaviors.stopped
      }
    }

class PlayerBehaviorImpl(context: ActorContext[Command | Receptionist.Listing], interactionActor: ActorRef[InteractionBehavior.Command], rootActor: ActorRef[PlayerBehavior.Command], onStop: (Int) => Unit) extends AbstractBehavior[Command | Receptionist.Listing](context):

  override def onMessage(msg: Command | Receptionist.Listing): Behavior[Command | Receptionist.Listing] = msg match {
    case msg: Receptionist.Listing =>
      context.log.info("RECEIVED SOMETHING")
      val services: List[ActorRef[PlayersManagerBehavior.Command]] = msg.serviceInstances(PlayersManagerBehavior.Service).toList
      if(!services.isEmpty)
        context.self ! AskToEnter(services.head)
      Behaviors.same
    case Start =>
      context.system.receptionist ! Receptionist.Subscribe(PlayersManagerBehavior.Service, context.self)
      Behaviors.same
    case AskToEnter(replyTo) =>
      replyTo ! PlayersManagerBehavior.AskEnter(rootActor)
      Behaviors.same
    case MemberOK =>
      context.log.info("MEMBER OK")
      waitForCards()
    case MemberKO =>
      interactionActor ! InteractionBehavior.MessageError("Impossibile entrare")
      Behaviors.stopped
    case m =>
      context.log.info("Unknown message: " + m.toString)
      Behaviors.same
  }

  def waitForCards(): Behavior[Command | Receptionist.Listing] = Behaviors.receiveMessage {
    case CardsAssigned(list) =>
      //interaction actor deve ricevere le carte
      context.log.info("Carte ottenute")
      interactionActor ! InteractionBehavior.CardsAssigned(list)
      gameOn()
    case _ => Behaviors.same
  }

  def gameOn(): Behavior[Command | Receptionist.Listing] = Behaviors.receiveMessage {
    case YourTurn(replyTo) => 
      // deve inviare un CardToGuess ad un attore InteractionActor, che può essere 
      // un attore che sceglie random una carta e la invia qui, o la gui vera e propria
      context.log.info("SONO IL PLAYER, E' IL MIO TURNO")
      interactionActor ! InteractionBehavior.ChooseTheCard(context.self)
      choose(replyTo)
    case CardChoosenByOther(title, replyTo) =>
      interactionActor ! InteractionBehavior.GuessCard(rootActor, title)
      guess(title, replyTo)
    case NewCard(card) =>
      interactionActor ! InteractionBehavior.NewCard(card)
      Behaviors.same
    case TurnCancelled => Behaviors.same

    case EndGame(points) =>
      interactionActor ! InteractionBehavior.MessageError(s"Gioco terminato, punti guadagnati: $points")
      onStop(points)
      Behaviors.stopped

    case _ => Behaviors.same
  }

  def guess(title: String, replyTo: ActorRef[ForemanBehavior.Command]): Behavior[Command | Receptionist.Listing] = Behaviors.receiveMessage {
    case SingleCardSubmitted(card) =>
      replyTo ! ForemanBehavior.SelectionToApply(card, rootActor)
      Behaviors.same
    case CardsSubmittedByOthers(cards) =>
      interactionActor ! InteractionBehavior.ShowCardsProposed(rootActor, cards, title)
      choose(replyTo)
    case TurnCancelled => gameOn()

    case _ => Behaviors.same
  }

  def choose(replyTo: ActorRef[ForemanBehavior.Command]): Behavior[Command | Receptionist.Listing] = Behaviors.receiveMessage {
    case CardChoosenForTurn(cardId, title) =>
      replyTo ! ForemanBehavior.CardToGuess(cardId, title, rootActor)
      interactionActor ! InteractionBehavior.ResetInteraction
      gameOn()
    case GuessedCardbyUser(cardId) =>
      replyTo ! ForemanBehavior.GuessSelection(cardId, rootActor)
      waitForCardRevealed(replyTo)

    case TurnCancelled => gameOn()

    case msg => context.log.info("Unknown message: " + msg)
      Behaviors.same
  }

  def waitForCardRevealed(replyTo: ActorRef[ForemanBehavior.Command]): Behavior[Command | Receptionist.Listing] = Behaviors.receiveMessage {
    case CardRevealed(cardId) =>
      // TODO reset interaction with image revealed (optional)
      gameOn()

    case TurnCancelled => gameOn()

    case msg => context.log.info("Unknown message: " + msg)
      Behaviors.same
  }

