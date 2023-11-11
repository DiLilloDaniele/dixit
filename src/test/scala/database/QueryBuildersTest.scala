package database

import org.scalatest.BeforeAndAfterAll
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.concurrent.ScalaFutures.*

import java.sql.SQLException

import grpcService.server.data.adapters.*

import concurrent.duration.DurationInt
import scala.language.postfixOps

class QueryBuildersTest extends AnyFunSpec with Matchers {

    def assertFail[T](fun: () => T) = {
        try {
            fun()
            fail()
        }
        catch {
            case _: SQLException =>  // Expected, so continue
            case _ => fail()
        }
    }

    describe("A query builder") {
        describe("that is Insert type") {
            it("should create the query string correctly") {
                val builder = InsertBuilder()
                val queryString = builder insertInto "TABLE" values("A = B")
                assert(queryString.query == "INSERT INTO TABLE VALUES (A = B)")
                assertFail(() => { builder insertInto "" })
                assertFail(() => { builder values "" })
            }
        }
        describe("that is Query type") {
            it("should create the query string correctly") {
                val builder = QueryBuilder()
                val queryString = builder select "*" from "TABLE" where "D = F" and "A" equal "B" or "B" disequal "C" 
                assert(queryString.query == "select * from TABLE where D = F AND A = B OR B != C")
                assertFail(() => { builder select "" })
                assertFail(() => { builder from "" })
                assertFail(() => { builder where "" })
                assertFail(() => { builder and "" })
                assertFail(() => { builder or "" })
                assertFail(() => { builder equal "" })
                assertFail(() => { builder disequal "" })
            }
        }
        describe("that is Update type") {
            it("should create the query string correctly") {
                val builder = UpdateBuilder()
                val queryString = builder update "User" set "Points" equalTo s"Points + 3" where s"UserId = 1"
                assert(queryString.query == "UPDATE User SET Points = Points + 3 WHERE UserId = 1")
                assertFail(() => { builder update "" })
                assertFail(() => { builder set "" })
                assertFail(() => { builder equalTo "" })
                assertFail(() => { builder where "" })
            }
        }
    }

}
