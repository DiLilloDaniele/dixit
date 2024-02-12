package grpcService.client.actors.behaviors

import akka.actor.typed.receptionist.{Receptionist, ServiceKey}
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors, TimerScheduler}
import akka.actor.typed.{ActorRef, Behavior, Terminated}
import akka.actor.Address
import grpcService.client.actors.utils.Message
import grpcService.client.actors.behaviors.PlayersManagerBehavior.*
import grpcService.client.model.PumpMyList.*

import scala.language.postfixOps
import scala.concurrent.duration.{DurationInt, FiniteDuration}

object PlayersManagerBehavior {

  val Service = ServiceKey[Command]("GameManager")

  sealed trait Command extends Message

  case class AskEnter(replyTo: ActorRef[PlayerBehavior.Command]) extends Command
  case object Startup extends Command
  case class PlayerExited(address: Address) extends Command
  case object CheckMembers extends Command
  case object Stop extends Command

  def apply(maxPlayers: Int, foreman: ActorRef[ForemanBehavior.Command]): Behavior[Command] =
    Behaviors.setup[Command] { ctx =>
      ctx.system.receptionist ! Receptionist.Register(Service, ctx.self)
      val manager = ctx.spawn(Behaviors.setup[PlayersManagerBehavior.Command](ctx => PlayersManagerBehaviorImpl(ctx, maxPlayers, foreman)), "manager")
      manager ! Startup
      ctx.watch(manager)
      Behaviors.receiveMessage[Command] {
        case msg: Command =>
          manager ! msg
          Behaviors.same
      }.receiveSignal { case (ctx, t @ Terminated(_)) =>
        ctx.log.info("[MANAGER] System terminated. Shutting down")
        manager ! Stop
        Behaviors.stopped
      }

    }

}

class PlayersManagerBehaviorImpl(context: ActorContext[Command],
                                 maxPlayers: Int,
                                 foreman: ActorRef[ForemanBehavior.Command]) extends AbstractBehavior[Command](context):

  var playersList: List[ActorRef[PlayerBehavior.Command]] = List()
  var currentNumPlayers: Int = maxPlayers

  override def onMessage(msg: Command): Behavior[Command] = msg match {
    case Startup => waitPlayers(0, foreman)
    case _ => Behaviors.same
  }

  def waitPlayers(currentNumPlayers: Int, foreman: ActorRef[ForemanBehavior.Command]): Behavior[Command] = Behaviors.receiveMessage[Command] {
    case AskEnter(replyTo) =>
      val currentPlayers = currentNumPlayers + 1
      if (currentPlayers <= maxPlayers) {
        playersList = playersList :+ replyTo
        replyTo ! PlayerBehavior.MemberOK
        if(currentPlayers == maxPlayers)
          foreman ! ForemanBehavior.Start(playersList.toSet)
          gameOn(foreman)
        else
          waitPlayers(currentPlayers, foreman)
      } else {
        replyTo ! PlayerBehavior.MemberKO
        Behaviors.same
      }
    case PlayerExited(address) =>
      playersList = playersList filter { i => i.path.address != address }
      context.log.info("PLAYER TOLTO: " + playersList)
      waitPlayers(currentNumPlayers - 1, foreman)
    case _ =>
      Behaviors.same
  }

  def gameOn(foreman: ActorRef[ForemanBehavior.Command]): Behavior[Command] = Behaviors.receiveMessage[Command] {
    case AskEnter(replyTo) =>
      val playersAddresses = playersList.map { i => i.path.address.toString}
      if(playersAddresses.contains(replyTo.path.address.toString) && currentNumPlayers < maxPlayers)
        context.log.info("un giocatore è rientrato")
        replyTo ! PlayerBehavior.MemberOK
        foreman ! ForemanBehavior.PlayerRejoined(replyTo.path.address.toString)
        currentNumPlayers = currentNumPlayers + 1
        if(currentNumPlayers == maxPlayers)
          foreman ! ForemanBehavior.RestartTurn
        gameOn(foreman)
      else
        replyTo ! PlayerBehavior.MemberKO
        Behaviors.same
    case PlayerExited(address) =>
      currentNumPlayers = currentNumPlayers - 1
      context.log.info("un giocatore ha quittato")
      foreman ! ForemanBehavior.InterruptTurn
      Behaviors.withTimers { timer =>
        timer.startSingleTimer(CheckMembers, 5000 milliseconds)
        gameOn(foreman)
      }

    case CheckMembers =>
      context.log.info("Checcko....")
      if(currentNumPlayers < maxPlayers)
        context.log.info("Stop game")
        context.self ! Stop
        Behaviors.same
      else
        context.log.info("NO stop game")
        Behaviors.same

    case Stop =>
      playersList.foreach { i =>
        i ! PlayerBehavior.GameInfo("Uno o più giocatori si sono disconnessi. Partita annullata :(")
      }
      Behaviors.stopped
  }


