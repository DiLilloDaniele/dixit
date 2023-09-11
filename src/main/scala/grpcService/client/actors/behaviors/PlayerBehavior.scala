package grpcService.client.actors.behaviors

import akka.actor.typed.{ActorRef, Behavior, Terminated}
import akka.actor.typed.receptionist.{Receptionist, ServiceKey}
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import grpcService.client.actors.Message
import grpcService.client.actors.behaviors.PlayerBehavior.*
import grpcService.client.actors.behaviors.ForemanBehavior

import scala.util.Random

object PlayerBehavior:

  val Service = ServiceKey[Command]("PlayersService")

  sealed trait Command extends Message
  //entry messages
  case object MemberOK extends Command
  case object MemberKO extends Command
  case class TurnOf(val player: String) extends Command

  //turn messages
  case class YourTurn(val replyTo: ActorRef[ForemanBehavior.Command]) extends Command
  case object NotYourTurn extends Command

  //guess messages
  case class CardChoosen(val title: String, val replyTo: ActorRef[ForemanBehavior.Command]) extends Command

  //choose messages

  def apply(): Behavior[Command] =
    Behaviors.setup[Command] { ctx =>

      val player = ctx.spawn(Behaviors.setup[Command](ctx => new PlayerBehaviorImpl(ctx)), "player")
      ctx.watch(player)

      Behaviors.receiveMessage[Command] {
        case _ =>
          Behaviors.same
      }.receiveSignal { case (ctx, t @ Terminated(_)) =>
        ctx.log.info("System terminated. Shutting down")
        Behaviors.stopped
      }
    }

class PlayerBehaviorImpl(context: ActorContext[Command]) extends AbstractBehavior[Command](context):

  override def onMessage(msg: Command): Behavior[Command] = msg match {
    case MemberOK => gameOn()
    case MemberKO => Behaviors.stopped
    case m =>
      context.log.info("Unknown message: " + m.toString)
      Behaviors.same
  }

  def gameOn(): Behavior[Command] = Behaviors.receiveMessage {
    case TurnOf(player) => ???
    case YourTurn(replyTo) => 
      // deve inviare un CardToGuess ad un attore InteractionActor, che può essere 
      // un attore che sceglie random una carta e la invia qui, o la gui vera e propria
      ???
    case CardChoosen(title, replyTo) => ???
    case _ => Behaviors.same
  }

  def guess(): Behavior[Command] = Behaviors.receiveMessage {
    case _ => ???
  }

  def choose(): Behavior[Command] = Behaviors.receiveMessage {
    case _ => ???
  }
