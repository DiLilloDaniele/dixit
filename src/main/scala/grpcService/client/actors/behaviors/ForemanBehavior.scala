package grpcService.client.actors.behaviors

import akka.actor.typed.{ActorRef, Behavior, Terminated}
import akka.actor.typed.receptionist.Receptionist
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import grpcService.client.actors.{ClusterListener, Message}
import grpcService.client.actors.behaviors.ForemanBehavior.{CardToGuess, Command, SelectionToApply, Start}
import grpcService.client.actors.behaviors.PlayerBehavior
import grpcService.client.actors.behaviors.PlayerBehavior.{CardChoosen, NotYourTurn, YourTurn}
import grpcService.client.model.PlayerSelection
import grpcService.client.model.PumpMyList.filterMap

object ForemanBehavior:

  import grpcService.client.actors.ClusterListener

  sealed trait Command extends Message

  case object Start extends Command
  case class CardToGuess(val cardId: String, val title: String, val replyTo: ActorRef[PlayerBehavior.Command]) extends Command
  case class SelectionToApply(val cardId: String, val replyTo: ActorRef[PlayerBehavior.Command]) extends Command
  
  def apply(): Behavior[Command] =
    Behaviors.setup[Command] { ctx =>
      val rootActorRef = ctx.self
      val master = ctx.spawn(Behaviors.setup[Command | Receptionist.Listing](ctx => new ForemanBehaviorImpl(ctx)), "foreman")
      val listener = ctx.spawn(Behaviors.setup[ClusterListener.Event | Receptionist.Listing](ctx => ClusterListener(rootActorRef)), "event-listener")
      val player = ctx.spawn(Behaviors.setup[PlayerBehavior.Command](ctx => new PlayerBehaviorImpl(ctx)), "player")

      ctx.watch(master)
      ctx.watch(listener)
      ctx.watch(player)

      Behaviors.receiveMessage[Command] {
        case Start =>
          ctx.log.info("Il root actor ha ricevuto un messaggio START")
          master ! Start
          Behaviors.same
        case _ =>
          Behaviors.same
      }.receiveSignal { case (ctx, t @ Terminated(_)) =>
        ctx.log.info("System terminated. Shutting down")
        Behaviors.stopped
      }
    }

class ForemanBehaviorImpl(context: ActorContext[Command | Receptionist.Listing]) extends AbstractBehavior[Command | Receptionist.Listing](context):

  var playersList: List[Tuple2[ActorRef[PlayerBehavior.Command], Int]] = List()
  var cont = 0
  var playersSelection: List[PlayerSelection] = List()
  
  context.system.receptionist ! Receptionist.Subscribe(PlayerBehavior.Service, context.self)

  override def onMessage(msg: Command | Receptionist.Listing): Behavior[Command | Receptionist.Listing] = msg match {
    case Start =>
      context.system.receptionist ! Receptionist.Subscribe(PlayerBehavior.Service, context.self)
      Behaviors.same
    case msg: Receptionist.Listing =>
      val services: List[ActorRef[PlayerBehavior.Command]] = msg.serviceInstances(PlayerBehavior.Service).toList
      playersList = services.map { i => Tuple2(i, 0)}.toList
      /* services.foreach { i =>
        i ! ZoneBehavior.StationHello(ctx.self, indexZone)
      } */
      services.head ! YourTurn(context.self)
      manageTurnOf(services.head, 0)
    case m =>
      context.log.info("Unknown message: " + m.toString)
      Behaviors.same
  }

  def manageTurnOf(actor: ActorRef[PlayerBehavior.Command], turn: Int): Behavior[Command | Receptionist.Listing] = Behaviors.receiveMessage {
    case CardToGuess(card, title, replyTo) if replyTo.path == actor.path =>
      playersSelection = List()
      playersList.map { i => i._1 }.foreach { i =>
        if(i.path != actor.path)
          i ! CardChoosen(title, context.self)
      }
      Behaviors.same
    case CardToGuess(card, title, replyTo) =>
      replyTo ! NotYourTurn
      Behaviors.same
    case _ => Behaviors.same
  }
  
  def waitForSelections(actor: ActorRef[PlayerBehavior.Command], turn: Int, cardSelected: String): Behavior[Command | Receptionist.Listing] = Behaviors.receiveMessage {
    case SelectionToApply(cardId, replyTo) =>
      cont = cont + 1
      playersSelection = playersSelection :+ PlayerSelection(cardId, replyTo.path.address.toString)
      if(playersList.size < cont)
        Behaviors.same
      else
        val votes = playersSelection.filter { p =>
          p.card == cardSelected
        }

        if(votes.size == playersList.size || votes.size == 0)
          playersList = playersList.filterMap { i =>
            i._1.path.address != actor.path.address
          } { i => Tuple2(i._1, i._2 + 2) }
        else
          playersList = playersList.filterMap { i =>
            i._1.path.address == actor.path.address || votes.exists { c => c.player == i._1.path.address.toString }
          } { i => Tuple2(i._1, i._2 + 3) }

        playersSelection foreach { i =>

        }
        //gestire turno circolare
        //playersList(turn + 1) ! YourTurn(context.self)
        manageTurnOf(playersList(turn + 1)._1, turn + 1)
    case _ => Behaviors.same
  }

