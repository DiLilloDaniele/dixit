package grpcService.client.actors

import grpcService.client.actors.{ClusterListener, utils}
import grpcService.client.actors.behaviors.{ForemanBehavior, InteractionBehavior, PlayerBehavior, PlayersManagerBehavior}

object ManualTesting {

  def testInteractions() =
    import grpcService.client.actors.utils
    val foreman = utils.startupWithRole("foreman", "2551", "127.0.0.1")(ForemanBehavior())
    
    Thread.sleep(2000)
    utils.startupWithRole("player", "2555", "127.0.0.1")(PlayerBehavior())
    Thread.sleep(2000)
    utils.startupWithRole("player", "2553", "127.0.0.1")(PlayerBehavior())
    //Thread.sleep(2000)
    //utils.startupWithRole("player", "2554", "127.0.0.1")(PlayerBehavior())


  def executeForeman() =
    import grpcService.client.actors.utils
    val foreman = utils.startupWithRole("foreman", "2551", "127.0.0.1")(ForemanBehavior())
    utils.startupWithRole("player", "2552", "127.0.0.1")(PlayerBehavior())

  def executePlayer1() =
    utils.startupWithRole("player", "2553", "127.0.0.1")(PlayerBehavior())

  def executePlayer2() =
    utils.startupWithRole("player", "2554", "127.0.0.1")(PlayerBehavior())

  def main(args: Array[String]) = {
    testInteractions()
  }

}
