package grpcService.server.data.adapters

import java.sql.SQLException

//devo prendere la lista di tabelle disponibili e controllare che from prenda una tabella valida (policy) per evitare sql injection
class QueryBuilder():
  var query = ""

  def select(what: String) =
    what match
      case "" => throw SQLException()
      case _ =>
        query = "select " + what
        this

  def from(from: String) =
    from match
      case "" => throw SQLException()
      case _ =>
        query += " from " + from
        this

  def where(cond: String) =
    cond match
      case "" => throw SQLException()
      case _ =>
        query += " where " + cond
        this

  def and(cond: String) =
    cond match
      case "" => throw SQLException()
      case _ =>
        query += " AND " + cond
        this

  def or(cond: String) =
    cond match
      case "" => throw SQLException()
      case _ =>
        query += " OR " + cond
        this

  def equal(cond: String) =
    cond match
      case "" => throw SQLException()
      case _ =>
        query += " = '" + cond + "'"
        this

  def disequal(cond: String) =
    cond match
      case "" => throw SQLException()
      case _ =>
        query += " != " + cond
        this