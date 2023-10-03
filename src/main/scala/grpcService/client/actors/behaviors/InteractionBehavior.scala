package grpcService.client.actors.behaviors

import akka.actor.typed.{ActorRef, Behavior, Terminated}
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import grpcService.client.actors.Message
import grpcService.client.actors.behaviors.InteractionBehavior.{CardsAssigned, ChooseTheCard, Command, GuessCard, MessageError, ShowCardsProposed}
import grpcService.client.controller.GameController
import grpcService.client.ClientImpl
import grpcService.client.view.MainGui

import javax.swing.{JFrame, SwingUtilities, WindowConstants}

object InteractionBehavior:

  @main def guiTest =
    import grpcService.client.actors.utils
    utils.startupWithRole("gui", "2551", "127.0.0.1")(InteractionBehavior())

  sealed trait Command extends Message
  case class CardsAssigned(list: List[String]) extends Command
  case class ChooseTheCard(val replyTo: ActorRef[PlayerBehavior.Command]) extends Command
  case class ShowCardsProposed(val replyTo: ActorRef[PlayerBehavior.Command], val cards: List[String], title: String) extends Command
  case class GuessCard(val replyTo: ActorRef[PlayerBehavior.Command], title: String) extends Command
  case class MessageError(message: String) extends Command

  def apply(): Behavior[Command] =
    Behaviors.setup[Command] { ctx =>
      val rootActorRef = ctx.self
      ctx.spawn(Behaviors.setup[InteractionBehavior.Command](ctx1 => InteractionGuiImpl(ctx1, null)), "gui")

      Behaviors.receiveMessage[Command] {
        case _ =>
          Behaviors.same
      }.receiveSignal { case (ctx, t @ Terminated(_)) =>
        ctx.log.info("System terminated. Shutting down")
        Behaviors.stopped
      }
    }

class InteractionGuiImpl(context: ActorContext[Command], replyTo: ActorRef[PlayerBehavior.Command]) extends AbstractBehavior[Command](context):

  var cards: List[String] = List()

  def guessImageConsumer(replyTo: ActorRef[PlayerBehavior.Command])(i: Int): Unit =
    replyTo ! PlayerBehavior.GuessedCardbyUser(s"$i")

  def chooseImageConsumer(replyTo: ActorRef[PlayerBehavior.Command])(i: Int, title: String): Unit =
    replyTo ! PlayerBehavior.CardChoosenForTurn(s"$i", title)

  def guessImageConsumerFromMine(replyTo: ActorRef[PlayerBehavior.Command])(i: Int): Unit =
    replyTo ! PlayerBehavior.SingleCardSubmitted(s"$i")

  val clientImpl = ClientImpl()
  val controller = GameController(clientImpl)
  val gui: MainGui = MainGui(chooseImageConsumer(replyTo), guessImageConsumer(replyTo), guessImageConsumerFromMine(replyTo))
  gui.createAndShowGui()
  gui.changeWarnText("AVVIO GIOCO")

  override def onMessage(msg: Command): Behavior[Command] = msg match {

    case CardsAssigned(list) =>
      cards = list
      Behaviors.same

    case ChooseTheCard(replyTo) =>
      gui.getImageToChoose(cards)
      Behaviors.same

    case ShowCardsProposed(replyTo, cards, title) =>
      //gui change images
      gui.getImageForGuessing(cards, title)
      Behaviors.same

    case GuessCard(replyTo, title: String) =>
      gui.getImageForGuessingFromMine(cards, title)
      Behaviors.same

    case MessageError(msg) => 
      gui.changeWarnText(msg)
      Behaviors.same

    case m =>
      context.log.info("Unknown message: " + m.toString)
      Behaviors.same
  }
