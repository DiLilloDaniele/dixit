package grpcService.server.data.ports

import grpcService.server.applicationService.ServiceLocator
import grpcService.server.data.adapters.AccessAdapter
import grpcService.server.data.adapters.QueryBuilder
import grpcService.server.data.adapters.InsertBuilder
import grpcService.server.data.adapters.UpdateBuilder
import grpcService.server.domain.model.User
import grpcService.server.domain.repository.AccessRepository

import java.sql.{ResultSet, SQLException}

object AccessPort:

  @main def main() =
    val accessPort = AccessPort(ServiceLocator.getDataAdapter())
    //accessPort.insertNewUser("Daniele", "password")
    //accessPort.addPointsToUser("1", 10)
    accessPort.selectUser("Daniele") match {
      case Some(value) => println(value.name + " - " + value.password)
      case _ => ()
    }

class AccessPort(accessAdapter: AccessAdapter) extends AccessRepository {
  
  override def selectUser(userId: String): Option[User] =
    val queryWrapper = QueryBuilder()
    val query = queryWrapper select "*" from "User" where "Name" equal userId
    println(query.query)
    accessAdapter.selectSingleRow(query)

  override def insertNewUser(userName: String, password: String): Unit =
    val insertBuilder = InsertBuilder()
    val query = insertBuilder insertInto "User(Name, Password, Points)" values {
      "'" + userName + "', '" + password + "', " + 0
    }
    println(query.query)
    accessAdapter.insertQuery(query)

  override def addPointsToUser(userId: String, points: Int): Boolean =
    val updateQuery = UpdateBuilder()
    val query = updateQuery update "User" set "Points" equalTo s"Points + $points" where s"UserId = $userId"
    println(query.query)
    accessAdapter.update(query)
    
  
}
  
