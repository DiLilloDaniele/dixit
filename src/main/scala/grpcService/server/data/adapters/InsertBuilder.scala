package grpcService.server.data.adapters

import java.sql.SQLException

class InsertBuilder:
  var query = ""

  def insertInto(table: String) =
    table match
      case "" => throw SQLException()
      case _ =>
        query = "INSERT INTO " + table
        this

  def values(values: String) =
    values match
      case "" => throw SQLException()
      case _ =>
        query += " VALUES (" + values + ")"
        this
