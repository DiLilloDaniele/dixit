package grpcService.client.actors.prioritizedQueue
import akka.actor.*
import akka.dispatch.*
import com.typesafe.config.Config
import grpcService.client.actors.behaviors.PlayersManagerBehavior.*

class MyPriorityActorMailbox(settings:
    ActorSystem.Settings, config: Config) extends 
    UnboundedPriorityMailbox ( 
    // Create a new PriorityGenerator, lower prio means more important 
    PriorityGenerator { 
        case Stop =>
            0
        // other messages 
        case _ => 1
})