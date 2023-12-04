package grpcService.client.actors

import akka.actor.typed.receptionist.Receptionist
import akka.actor.typed.scaladsl.{Behaviors, TimerScheduler}
import akka.actor.typed.{ActorRef, Behavior}
import akka.cluster.ClusterEvent.*
import akka.cluster.typed.{Cluster, Down, Leave, SelfRemoved, Subscribe}
import grpcService.client.actors.ClusterListener.Event
import grpcService.client.actors.ClusterListener.Event.{MemberChange, ReachabilityChange}
import grpcService.client.actors.behaviors.ForemanBehavior.{Start, list}
import akka.actor.ActorSelection
import akka.util.Timeout
import grpcService.client.actors.behaviors.{ForemanBehavior, PlayerBehavior, PlayersManagerBehavior}
import grpcService.client.actors.utils.Message

import java.util.concurrent.TimeUnit
import scala.language.postfixOps
import scala.concurrent.duration.{DurationInt, FiniteDuration}
import scala.util.{Failure, Success}

object ClusterListener:

  // internal adapted cluster events only
  sealed trait Command extends Message
  case class ListingResponse(listing: Receptionist.Listing) extends Command
  case object CheckMembers extends Command

  enum Event:
    case ReachabilityChange(reachabilityEvent: ReachabilityEvent)
    case MemberChange(event: MemberEvent)

  import Event.*

  def apply(manager: ActorRef[PlayersManagerBehavior.Command]): Behavior[Command | Event | Receptionist.Listing] = Behaviors.setup { ctx =>

    // MemberEvent extends ClusterDomainEvent
    // We use the "message adapter" pattern to avoid the need of directly supporting the whole MemberEvent messaging interface
    val memberEventAdapter: ActorRef[MemberEvent] = ctx.messageAdapter(MemberChange.apply)
    // To subscribe, you must provide your ActorRef[A<:ClusterDomainEvent] and the ClusterDomainEvent class
    Cluster(ctx.system).subscriptions ! Subscribe(memberEventAdapter, classOf[MemberEvent])

    // ReachabilityEvent also extends ClusterDomainEvent
    val reachabilityAdapter: ActorRef[ReachabilityEvent] = ctx.messageAdapter(ReachabilityChange.apply)
    Cluster(ctx.system).subscriptions ! Subscribe(reachabilityAdapter, classOf[ReachabilityEvent])

    // Our behaviour listens for cluster events (membership and reachability events)
    Behaviors.receiveMessage { message =>
      message match {

        case ReachabilityChange(reachabilityEvent) =>
          reachabilityEvent match {
            case UnreachableMember(member) =>
              ctx.log.info("Member detected as unreachable: {}", member)
            case ReachableMember(member) =>
              ctx.log.info("Member back to reachable: {}", member)
          }

        case MemberChange(changeEvent) =>
          changeEvent match {
            case MemberUp(member) =>
              ctx.log.info("Member is Up: {} - I'm {}", member.address, ctx.system.address.port)
            case MemberRemoved(member, previousStatus) =>
              ctx.log.info(s"ROLE MEMBER: ${member.roles.contains("player")}")
              if(member.roles.contains("player"))
                ctx.log.info("OUT: {}", member.roles)
                ctx.log.info("Member is Removed: {} after {}", member.address, previousStatus)
                manager ! PlayersManagerBehavior.PlayerExited(member.address)
            case _: MemberEvent => // ignore
          }
        case _ => Behaviors.same
      }
      Behaviors.same
    }
  }
