package grpcService.client.actors

import akka.actor.typed.receptionist.Receptionist
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, ActorSystem, Behavior, Terminated}
import akka.cluster.Cluster
import com.typesafe.config.ConfigFactory
import grpcService.client.actors.behaviors.{ForemanBehavior, ForemanBehaviorImpl}

import java.io.{BufferedReader, InputStreamReader}

object utils {

  case class Request(replyTo: ActorRef[Message]) extends Message

  case object Reply extends Message

  //spawn di Foreman, cluster listener e gui
  def apply(): Behavior[Message] = Behaviors.setup { ctx =>
    val cluster = Cluster(ctx.system)

    Behaviors.receiveMessage[Message] {
      case Request(replyTo) =>
        ctx.log.info("REQUEST RECEIVED")
        replyTo ! Reply
        Behaviors.same
      case Reply =>
        ctx.log.info("REPLY RECEIVED")
        Behaviors.same
    }.receiveSignal { case (ctx, t @ Terminated(_)) =>
      ctx.log.info("System terminated. Shutting down")
      Behaviors.stopped
    }

  }

  def startupWithRole[X](role: String, port: String, ip: String)(root: => Behavior[X]): ActorSystem[X] =
    val config = ConfigFactory
      .parseString(
        s"""
        akka.remote.artery.canonical.hostname=$ip
        akka.remote.artery.canonical.port=$port
        akka.cluster.roles = [$role]
        """)
      .withFallback(ConfigFactory.load("base-cluster"))

    // Create an Akka system
    ActorSystem(root, "ClusterSystem", config)

  @main def startSeedNode() =
    startupWithRole("seed", "2551", "192.168.1.118")(ForemanBehavior());

  @main def createListener() = startupWithRole("listener", "2552", "192.168.1.118")(apply());

  @main def createActor1() = startupWithRole("actor1", "2553", "192.168.1.118")(apply());

  @main def createActor2() = startupWithRole("actor2", "2554", "192.168.1.118")(apply());

  @main def main() =

    val reader: BufferedReader = new BufferedReader(new InputStreamReader(System.in));
    System.out.print("Inserisci l'ip")
    val ipAddress: String = reader.readLine();

    val port: String = reader.readLine();

    System.out.print(ipAddress + ":" + port)
    val sender = startupWithRole("sender", port, ipAddress)(ForemanBehavior());


}
