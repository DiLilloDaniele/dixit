package grpcService.client.actors.behaviors

import akka.actor.typed.receptionist.{Receptionist, ServiceKey}
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior, Terminated}
import grpcService.client.actors.Message
import grpcService.client.actors.behaviors.PlayersManagerBehavior.{AskEnter, Command, Service, Startup}

object PlayersManagerBehavior {

  val Service = ServiceKey[Command]("GameManager")

  sealed trait Command extends Message

  case class AskEnter(replyTo: ActorRef[PlayerBehavior.Command]) extends Command
  case object Startup extends Command


  def apply(maxPlayers: Int, foreman: ActorRef[ForemanBehavior.Command]): Behavior[Command] =
    Behaviors.setup[Command] { ctx =>
      ctx.system.receptionist ! Receptionist.Register(Service, ctx.self)
      val manager = ctx.spawn(Behaviors.setup[PlayersManagerBehavior.Command](ctx => PlayersManagerBehaviorImpl(ctx, maxPlayers, foreman)), "manager")
      manager ! Startup
      Behaviors.receiveMessage[Command] {
        case msg: Command =>
          manager ! msg
          Behaviors.same
      }.receiveSignal { case (ctx, t @ Terminated(_)) =>
        ctx.log.info("System terminated. Shutting down")
        Behaviors.stopped
      }

    }

}

class PlayersManagerBehaviorImpl(context: ActorContext[Command],
                                 maxPlayers: Int,
                                 foreman: ActorRef[ForemanBehavior.Command]) extends AbstractBehavior[Command](context):

  context.log.info("Manager on")

  override def onMessage(msg: Command): Behavior[Command] = msg match {
    case Startup => waitPlayers(maxPlayers, 0, foreman)
  }

  def waitPlayers(maxPlayers: Int, currentNumPlayers: Int, foreman: ActorRef[ForemanBehavior.Command]): Behavior[Command] = Behaviors.receiveMessage[Command] {
    case AskEnter(replyTo) =>
      val currentPlayers = currentNumPlayers + 1
      context.log.info("Enter requested")
      if (currentPlayers <= maxPlayers) {
        replyTo ! PlayerBehavior.MemberOK
        if(currentPlayers == maxPlayers)
          foreman ! ForemanBehavior.Start
        waitPlayers(maxPlayers, currentPlayers, foreman)
      } else {
        replyTo ! PlayerBehavior.MemberKO
        Behaviors.same
      }
    case _ =>
      Behaviors.same
  }


