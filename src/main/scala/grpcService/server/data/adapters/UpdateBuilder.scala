package grpcService.server.data.adapters

import java.sql.SQLException

class UpdateBuilder:
  var query = ""

  def update(table: String) =
    table match
      case "" => throw SQLException()
      case _ =>
        query = "UPDATE " + table
        this

  def set(parameter: String) =
    parameter match
      case "" => throw SQLException()
      case _ =>
        query += " SET " + parameter
        this

  def equalTo(value: String) =
    value match
      case "" => throw SQLException()
      case _ =>
        query += " = " + value
        this

  def where(cond: String) =
    cond match
      case "" => throw SQLException()
      case _ =>
        query += " WHERE " + cond
        this
