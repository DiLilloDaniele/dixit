package grpcService.client.model

import akka.actor.typed.ActorRef
import grpcService.client.actors.utils.Message

case class PlayerSelection[A](val card: String, val player: ActorRef[A])
