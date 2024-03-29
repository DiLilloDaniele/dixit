package grpcService.client.actors

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.receptionist.Receptionist
import akka.actor.typed.scaladsl.Behaviors
import akka.cluster.ClusterEvent.{MemberEvent, MemberRemoved, MemberUp, ReachabilityEvent, ReachableMember, UnreachableMember}
import akka.cluster.typed.{Cluster, Subscribe}
import grpcService.client.actors.behaviors.PlayerBehavior

object PlayerClusterListener:

  enum Event:
    case ReachabilityChange(reachabilityEvent: ReachabilityEvent)
    case MemberChange(event: MemberEvent)

  import Event.*

  def apply(player: ActorRef[PlayerBehavior.Command]): Behavior[Event | Receptionist.Listing] = Behaviors.setup { ctx =>

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
              ctx.log.info(s"ROLE MEMBER: ${member.roles.contains("foreman")}")
              if(member.roles.contains("foreman") || member.roles.contains("root"))
                ctx.log.info("OUT: {}", member.roles)
                ctx.log.info("Member is Removed: {} after {}", member.address, previousStatus)
                player ! PlayerBehavior.GameInfo("Il capopartita è uscito, partita annullata :(")
            case _: MemberEvent => // ignore
          }
        case _ => Behaviors.same
      }
      Behaviors.same
    }
  }
