package grpcService.server.data.adapters

import grpcService.server.domain.model.User

import java.sql.{Connection, DriverManager, ResultSet, SQLException, Statement}

object AccessAdapter:

  def apply(url: String, port: String, driver: String, dbName: String, username: String, password: String): AccessAdapter = new AccessAdapter(url, port, driver, dbName, username, password)

  @main def main() =
    val adapter = apply("localhost", "3306", "mysql", "DIXIT", "root", "root")
    adapter.createDb()

  @main def queryTest() =
    import grpcService.server.data.adapters.QueryBuilder
    var queryWrapper = QueryBuilder()
    queryWrapper select "this" from "table" where "cs" equal ""

//il query wrapper viene passato dall'accessport all'access adapter, cosicché non venga eseguito codice malevolo nelle query
//ho controllo sulla query inviata
class AccessAdapter(val url: String, val port: String, val driver: String, val dbName: String, val username: String, val password: String):

  val connectionString = "jdbc:" + driver + "://" + url + ":" + port + "/" + dbName + "?autoReconnect=true&useSSL=false"
  var connection: Connection = _

  def connect(): Unit =
    //jdbc:mysql://localhost:3306/DIXIT?autoReconnect=true&useSSL=false
    Class.forName("com.mysql.jdbc.Driver");
    connection = DriverManager.getConnection(connectionString, username, password)

  def createDb(): Unit =
    Class.forName("com.mysql.jdbc.Driver");
    val connection = DriverManager.getConnection(
      "jdbc:mysql://localhost:3306/?autoReconnect=true&useSSL=false", "root", "root")

    try {
      val statement: Statement = connection.createStatement()
      val query = "CREATE DATABASE DIXIT"
      //val result = statement.executeQuery(query)
      statement.executeUpdate(query)
    } catch {
      case e: Exception => e.printStackTrace
    } finally {
      if (connection != null) try connection.close
      catch {
        case e: SQLException => /* Ignored */
      }
    }

  def checkDatabaseExist(): Boolean =

    return true

  def selectSingleRow(queryWrapper: QueryBuilder): Option[User] =
    var user : Option[User] = Option.empty
    try {
      connect()
      val statement: Statement = connection.createStatement()
      val query = queryWrapper.query
      val resultSet = statement.executeQuery(query)
      resultSet.first() match
        case true => user = Option(User(resultSet.getString("Name"), resultSet.getInt("Points"),
          resultSet.getString("Password")))
        case _ => user = Option.empty
    } catch {
      case e: Exception => e.printStackTrace
    } finally {
      if (connection != null) try connection.close
      catch {
        case e: SQLException => /* Ignored */
      }
    }
    user

  def insertQuery(query: InsertBuilder): Unit =
    try {
      connect()
      val statement: Statement = connection.createStatement()
      statement.executeUpdate(query.query)
    } catch {
      case e: Exception => e.printStackTrace
    } finally {
      if (connection != null) try connection.close
      catch {
        case e: SQLException => /* Ignored */
      }
    }

  def update(query: UpdateBuilder): Boolean = try {
    connect()
    val statement: Statement = connection.createStatement()
    statement.executeUpdate(query.query)
    return true
  } catch {
    case e: Exception => e.printStackTrace
      return false
  } finally {
    if (connection != null) try connection.close
    catch {
      case e: SQLException => /* Ignored */
    }
  }

