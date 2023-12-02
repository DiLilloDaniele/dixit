package grpcService.client.actors.prioritizedQueueExample
import akka.actor.*
import com.typesafe.config.ConfigFactory

object PriorityMailBoxApp { 
    

    def main(args: Array[String]) = {
        val config = ConfigFactory
      .parseString(
        s"""
        akka.cluster.roles = [role]
        """)
      .withFallback(ConfigFactory.load("base-cluster"))
        //DispatcherSelector.fromConfig("your-dispatcher")
        val actorSystem = ActorSystem("HelloAkka", config) 
        val myPriorityActor = actorSystem.actorOf(Props[MyPriorityActor]().withDispatcher("akka.prio-dispatcher"))
        myPriorityActor ! 6.0 
        myPriorityActor ! 1 
        myPriorityActor ! 5.0 
        myPriorityActor ! 3 
        myPriorityActor ! "Hello" 
        myPriorityActor ! 5 
        myPriorityActor ! "I am priority actor" 
        myPriorityActor ! "I process string messages first,then integer, long and others"
    }
} 