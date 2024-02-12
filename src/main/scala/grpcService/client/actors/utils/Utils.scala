package grpcService.client.actors.utils

import akka.actor.typed.receptionist.Receptionist
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, ActorSystem, Behavior, Terminated}
import akka.cluster.Cluster
import com.typesafe.config.ConfigFactory
import grpcService.client.actors.behaviors.{ForemanBehavior, ForemanBehaviorImpl, PlayerBehavior}

import java.io.{BufferedReader, InputStreamReader}

object Utils:

  def startupWithRoleForTest[X](role: String, port: String, ip: String, clusterName: String = "ClusterSystem")(root: => Behavior[X]): ActorSystem[X] =
    val config = ConfigFactory
      .parseString(
        s"""
        akka.remote.artery.canonical.hostname=$ip
        akka.remote.artery.canonical.port=$port
        akka.cluster.roles = [$role]
        akka.cluster.seed-nodes = [
        ]
        """)
      .withFallback(ConfigFactory.load("application-test"))
      // Create an Akka system
    ActorSystem(root, clusterName, config)

  def startupWithRole[X](role: String, port: String, ip: String, clusterName: String = "ClusterSystem")(root: => Behavior[X]): ActorSystem[X] =
    val config = ConfigFactory
      .parseString(
        s"""
        akka.remote.artery.canonical.hostname=$ip
        akka.remote.artery.canonical.port=$port
        akka.cluster.roles = [$role]
        akka.cluster.seed-nodes = [
          "akka://$clusterName@127.0.0.1:2551",
          "akka://$clusterName@127.0.0.1:2552"
        ]
        """)
      .withFallback(ConfigFactory.load("base-cluster"))

    // Create an Akka system
    ActorSystem(root, clusterName, config)

  @main def main() =

    val reader: BufferedReader = new BufferedReader(new InputStreamReader(System.in));
    System.out.print("Inserisci l'ip")
    val ipAddress: String = reader.readLine();

    val port: String = reader.readLine();

    System.out.print(ipAddress + ":" + port)
    val sender = startupWithRole("sender", port, ipAddress)(ForemanBehavior());

