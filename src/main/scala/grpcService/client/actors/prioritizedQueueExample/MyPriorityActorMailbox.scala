package grpcService.client.actors.prioritizedQueueExample
import akka.actor.*
import akka.dispatch.*
import com.typesafe.config.Config
import grpcService.client.actors.prioritizedQueueExample.MyPriorityActor.Hello
import grpcService.client.actors.prioritizedQueueExample.MyPriorityActor.Exit

class MyPriorityActorMailbox(settings:
    ActorSystem.Settings, config: Config) extends 
    UnboundedPriorityMailbox ( 
    // Create a new PriorityGenerator, lower prio means more important 
    PriorityGenerator { 
        case Hello(addr) =>
            0
        case Exit(addr) =>
            1
        // Int Messages 
        case x: Int =>
            1 
        // String Messages 
        case x: String =>
            0 
        // Long messages 
        case x: Long =>
            2 
        // other messages 
        case _ => 3 
})