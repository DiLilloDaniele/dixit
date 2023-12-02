package grpcService.client.actors.prioritizedQueueExample
import akka.actor.*
object MyPriorityActor:
    case class Hello(addr: String)
    case class Exit(addr: String)

class MyPriorityActor extends Actor {
    import MyPriorityActor.* 
    def receive: PartialFunction[Any, Unit] = { 
    // Int Messages 
    case x: Int => println(x) 
    // String Messages 
    case x: String => println(x) 
    // Long messages 
    case x: Long => println(x) 
    // other messages 
    case x => println(x) 
    } 
}