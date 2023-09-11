package grpcService.client.actors

import akka.actor.typed.receptionist.Receptionist
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import akka.cluster.ClusterEvent.*
import akka.cluster.typed.{Cluster, Subscribe}
import grpcService.client.actors.ClusterListener.Event
import grpcService.client.actors.ClusterListener.Event.{MemberChange, ReachabilityChange}
import grpcService.client.actors.behaviors.ForemanBehavior.Start
import akka.actor.ActorSelection
import akka.util.Timeout
import grpcService.client.actors.behaviors.ForemanBehavior

import java.util.concurrent.TimeUnit
import scala.concurrent.duration.FiniteDuration
import scala.util.{Failure, Success}

object ClusterListener:

  var members = 0

  // internal adapted cluster events only
  sealed trait Command extends Message

  enum Event:
    case ReachabilityChange(reachabilityEvent: ReachabilityEvent)
    case MemberChange(event: MemberEvent)

  import Event.*

  def resetMembers() = members = 0

  def apply(rootActor: ActorRef[ForemanBehavior.Command]): Behavior[Event | Receptionist.Listing] = Behaviors.setup { ctx =>
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
              members = members + 1

              ctx.log.info("" + members)
              if(members == 3)
                ctx.system.terminate()
            case MemberRemoved(member, previousStatus) =>
              ctx.log.info("OUT: {}", member.roles)
              ctx.log.info("Member is Removed: {} after {}", member.address, previousStatus)
              members = members - 1
              ctx.log.info("" + members)
            case _: MemberEvent => // ignore
          }
      }
      Behaviors.same
    }
  }
