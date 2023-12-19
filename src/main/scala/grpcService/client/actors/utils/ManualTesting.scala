package grpcService.client.actors.utils

import grpcService.client.actors.{ClusterListener}
import grpcService.client.actors.utils.Utils.*
import grpcService.client.actors.behaviors.{ForemanBehavior, InteractionBehavior, PlayerBehavior, PlayersManagerBehavior}

object ManualTesting {

  def testInteractions() =
    import grpcService.client.actors.utils
    val foreman = startupWithRole("foreman", "2551", "127.0.0.1")(ForemanBehavior())
    
    Thread.sleep(2000)
    startupWithRole("player", "2555", "127.0.0.2")(PlayerBehavior())
    Thread.sleep(2000)
    startupWithRole("player", "2553", "127.0.0.3")(PlayerBehavior())
    Thread.sleep(2000)
    startupWithRole("player", "2554", "127.0.0.4")(PlayerBehavior())


  def executeForeman() =
    import grpcService.client.actors.utils
    val foreman = startupWithRole("foreman", "2551", "127.0.0.1")(ForemanBehavior())
    startupWithRole("player", "2552", "127.0.0.1")(PlayerBehavior())

  def executePlayer1() =
    startupWithRole("player", "2553", "127.0.0.1")(PlayerBehavior())

  def executePlayer2() =
    startupWithRole("player", "2554", "127.0.0.1")(PlayerBehavior())

  def main(args: Array[String]) = {
    testInteractions()
  }

}
