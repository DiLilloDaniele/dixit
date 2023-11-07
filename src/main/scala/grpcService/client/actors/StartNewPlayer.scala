package grpcService.client.actors

import grpcService.client.actors.behaviors.PlayerBehavior

object StartNewPlayer {

    def executePlayer(role: String, port: String, ip: String) =
        utils.startupWithRole(role, port, ip)(PlayerBehavior())

    def main(args: Array[String]) = {
        println(args)
        executePlayer("player", "2554", "127.0.0.10")
  }
}