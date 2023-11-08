package model

import org.scalatest.BeforeAndAfterAll
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.concurrent.ScalaFutures.*

import concurrent.duration.DurationInt
import scala.language.postfixOps

import grpcService.client.model.PumpMyList.*

class PumpMyListTest extends AnyFunSpec with Matchers {

    describe("A list") {
        describe("extended with new methods") {
            it("should use them correctly") {
                var l = List(1,2,3,4,5,6)
                var newL = l.filterMap {
                    i => i % 2 == 0
                } {
                    i => i * 2
                }
                newL = newL :+ 7
                newL = newL - 8
                assert(newL == List(1, 4, 3, 5, 12, 7))
                val l1 = List(1,2,3,4)
                val l2= List(3,4,5,6)
                assert(l1.removeIntersection(l2) == List(1,2))
                assert(!l1.equals(l2))
            }
        }
    }

}