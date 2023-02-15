import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, ActorSystem, Behavior}
import akka.cluster.Cluster
import com.typesafe.config.ConfigFactory

import java.io.{BufferedReader, InputStreamReader}

object utils {

  case class Request(replyTo: ActorRef[Message]) extends Message
  case object Reply extends Message

  def apply(): Behavior[Message] = Behaviors.setup { ctx =>
    val cluster = Cluster(ctx.system)

    Behaviors.receiveMessage {
      case Request(replyTo) =>
        ctx.log.info("REQUEST RECEIVED")
        replyTo ! Reply
        Behaviors.same
      case Reply =>
        ctx.log.info("REPLY RECEIVED")
        Behaviors.same
    }

  }

  def startupWithRole[X](role: String, port: String, ip: String)(root: => Behavior[X]): ActorSystem[X] =
    val config = ConfigFactory
      .parseString(s"""
        akka.remote.artery.canonical.hostname=$ip
        akka.remote.artery.canonical.port=$port
        akka.cluster.roles = [$role]
        """)
      .withFallback(ConfigFactory.load("base-cluster"))

      // Create an Akka system
      ActorSystem(root, "ClusterSystem", config)

  @main def startSeedNode() =
    startupWithRole("seed", "2551", "192.168.1.118")(apply());

  @main def createListener() = startupWithRole("listener", "2552", "192.168.1.118")(apply());

  @main def main() =

    val reader: BufferedReader = new BufferedReader(new InputStreamReader(System.in));

    val ipAddress: String = reader.readLine();

    val port: String = reader.readLine();

    System.out.print(ipAddress + ":"+ port)
    val sender = startupWithRole("sender", port, ipAddress)(apply());




}
