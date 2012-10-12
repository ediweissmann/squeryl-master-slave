package com.ediweissmann.squeryl.test.repository

/**
 * Squeryl schema
 */
protected object Schema extends org.squeryl.Schema {
  val apples = table[AppleDo]("test_apples")
  val oranges = table[OrangeDo]("test_oranges")
}

import org.squeryl.annotations._
import org.squeryl.KeyedEntity

/**
 * DOs apples and oranges
 */
class AppleDo private(
                       @Column("apple_id")
                       var id: Int = 0,
                       var nickname: String)
  extends KeyedEntity[Int] {

}

object AppleDo {
  def apply(nickname: String) = new AppleDo(nickname = nickname)
}

class OrangeDo private(
                        @Column("orange_id")
                        var id: Int = 0,
                        var nickname: String)
  extends KeyedEntity[Int] {

}

object OrangeDo {
  def apply(nickname: String) = new OrangeDo(nickname = nickname)
}

import org.squeryl.PrimitiveTypeMode._

/**
 * Repositories
 */
class ApplesRepository {

  def contains(name: String): Boolean = Schema.apples.where(_.nickname === name).headOption.isDefined

  def save(name:String) = Schema.apples.insertOrUpdate(AppleDo(name))

  def deleteAll = Schema.apples.deleteWhere(_.id gte 0)
}

class OrangesRepository {

  def contains(name: String): Boolean = Schema.oranges.where(_.nickname === name).headOption.isDefined

  def save(name:String) = Schema.oranges.insertOrUpdate(OrangeDo(name))

  def deleteAll = Schema.oranges.deleteWhere(_.id gte 0)
}


