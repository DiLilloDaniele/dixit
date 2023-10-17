package akka

import akka.actor.testkit.typed.scaladsl.{ActorTestKit, TestProbe}
import akka.actor.typed.{Behavior, Terminated}
import akka.actor.typed.receptionist.Receptionist
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import com.typesafe.config.ConfigFactory
import grpcService.client.actors.ClusterListener
import grpcService.client.actors.behaviors.{ForemanBehavior, InteractionBehavior, PlayerBehavior}
import org.scalatest.BeforeAndAfterAll
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.funspec.AnyFunSpec

import concurrent.duration.DurationInt
import scala.language.postfixOps



//simple actor interaction that gives a card pre-prepared
def interactionTestActor(): Behavior[InteractionBehavior.Command] =
  Behaviors.setup[InteractionBehavior.Command] { ctx =>
    Behaviors.receiveMessage[InteractionBehavior.Command] {
      case InteractionBehavior.ChooseTheCard(replyTo) =>
        replyTo ! PlayerBehavior.CardChoosenForTurn("1", "myTitle")
        Behaviors.same
      case m =>
        ctx.log.info("Unknown message: " + m.toString)
        Behaviors.same
    }.receiveSignal { case (ctx, t @ Terminated(_)) =>
      ctx.log.info("System terminated. Shutting down")
      Behaviors.stopped
    }
  }

class AsyncTestingExampleSpec
  extends AnyFunSpec
    with BeforeAndAfterAll
    with Matchers {

  describe("The player actors") {
    describe("in a game with 2 players") {
      describe("when the game starts") {
        it("a player should send the choosen card for its turn") {
          val testKit = ActorTestKit()
          val probe = testKit.createTestProbe[ForemanBehavior.Command]()
          val interaction = testKit.spawn(interactionTestActor(), "interaction")
          val foreman = testKit.spawn(ForemanBehavior(Option(probe.ref), Option(interaction), 2), "foreman")
          Thread.sleep(2000)
          val player = testKit.spawn(PlayerBehavior(interactionExt = Option(interaction)), "player")
          val player1 = testKit.spawn(PlayerBehavior(interactionExt = Option(interaction)), "player1")
          probe.expectMessage(20 seconds, ForemanBehavior.Start(List(player, player1)))
          val message: ForemanBehavior.Command = probe.receiveMessage()
          message match {
            case ForemanBehavior.CardToGuess("1", "myTitle", _) => succeed
            case m => fail("Unexpected message: " + m)
          }

          testKit.shutdownTestKit()
        }
      }
    }
    describe("in a game with 2 players") {
      describe("when a turn is terminated") {
        it("the other players should receive the cards selected for the guess phase") {
          import PlayerBehavior._
          val testKit = ActorTestKit()
          val probe = testKit.createTestProbe[PlayerBehavior.Command | Receptionist.Listing]()
          val interaction = testKit.spawn(interactionTestActor(), "interaction")
          val foreman = testKit.spawn(ForemanBehavior(interactionExt = Option(interaction), 3), "foreman")
          Thread.sleep(2000)
          val player2 = testKit.spawn(PlayerBehavior(interactionExt = Option(interaction)), "player2")
          val player3 = testKit.spawn(PlayerBehavior(interactionExt = Option(interaction)), "player3")
          val player1 = testKit.spawn(PlayerBehavior(logger = Option(probe.ref), interactionExt = Option(interaction)), "player1")
          val message1: PlayerBehavior.Command | Receptionist.Listing = probe.receiveMessage()
          println(message1)
          val message2: PlayerBehavior.Command | Receptionist.Listing = probe.receiveMessage()
          println(message2)
          val message3: PlayerBehavior.Command | Receptionist.Listing = probe.receiveMessage()
          println(message3)
          (message1, message2, message3) match {
            case (MemberOK, CardsAssigned(list), CardChoosenByOther("myTitle", _)) => succeed
            case (MemberOK, CardsAssigned(list), YourTurn(_)) => succeed
            case m => fail("Unexpected message: " + m)
          }

          testKit.shutdownTestKit()
        }
      }
    }
  }
}
