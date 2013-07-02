package models

import anorm._
import anorm.SqlParser._
import play.api.db.DB
import play.api.Play.current

case class User(id: Pk[Int] = NotAssigned, username: String, age: Int) {

  def contacts: List[Contact] =
    id.map { userId =>
      Contact.loadByUserId(userId)
    }.getOrElse(Nil)
}

class UserWithCachedContacts(id: Pk[Int], username: String, age: Int) extends User(id, username, age) with cache.SingleElementCache[List[Contact]] with cache.ExpiryCache[List[Contact]]{
  
  override  val expirationTimeout = 5
  
  override def contacts: List[Contact] = getOrFetch("contacts")(() => super.contacts)
}


object User {

  def save(user: User) = {
    val id = DB.withConnection { implicit connection =>
      SQL(""" 
    		  INSERT INTO User(username, age) 
    		  VALUES({username}, {age})
      """).on(
        'username -> user.username,
        'age -> user.age).executeInsert()
    }
    id.get.toInt
  }

  private val userParser: RowParser[User] = buildUserParser { case id ~ username ~ age => User(id, username, age) }

  private val userWithCacheParser: RowParser[User] = buildUserParser {
    case id ~ username ~ age => new { val expirationTimeout = 5 } with User(id, username, age) with cache.SingleElementCache[List[Contact]] with cache.ExpiryCache[List[Contact]] {

      override def contacts: List[Contact] = getOrFetch("contacts")(() => super.contacts)
    }
  }

  private def buildUserParser(factory: anorm.~[anorm.~[Pk[Int], String], Int] => User) =
    get[Pk[Int]]("id") ~
      get[String]("username") ~
      get[Int]("age") map { factory(_) }

  def list: List[User] = {
    DB.withConnection { implicit connection =>
      SQL("SELECT * from User").as(userParser *)
    }
  }

  def loadWithCache(id: Int): Option[User] = {
    load(id, userWithCacheParser)
  }

  def load(id: Int): Option[User] = {
    load(id, userParser)
  }

  private def load(id: Int, parser: RowParser[User]) = {
    DB.withConnection { implicit connection =>
      SQL("SELECT * from User WHERE id = {id}")
        .on('id -> id)
        .as(parser.singleOpt)
    }
  }

  def update(id: Int, user: User) {
    DB.withConnection { implicit connection =>
      SQL(""" 
    		  UPDATE User SET
    		  username = {username},
    		  age = {age}
    		  WHERE id = {id}
      """).on(
        'id -> id,
        'username -> user.username,
        'age -> user.age).executeUpdate
    }
  }

  def delete(id: Int) {
    DB.withTransaction { implicit connection =>
      SQL(""" 
          DELETE FROM User where id = {id}
      """).on(
        'id -> id).executeUpdate
      SQL(""" 
          DELETE FROM Contact where userId = {userId}
      """).on(
        'userId -> id).executeUpdate
    }
  }
}
