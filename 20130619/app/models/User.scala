package models

import anorm._
import anorm.SqlParser._
import play.api.db.DB
import play.api.Play.current

case class User(id: Pk[Int] = NotAssigned, username: String, age: Int) {
  def contacts = id.map { userId =>
	  Contact.loadByUserId(userId)
    }.getOrElse(Nil)
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

  private val userParser: RowParser[User] = {
    get[Pk[Int]]("id") ~
      get[String]("username") ~
      get[Int]("age") map {
        case id ~ username ~ age => User(id, username, age)
      }
  }

  def list: List[User] = {
    DB.withConnection { implicit connection =>
      SQL("SELECT * from User").as(userParser *)
    }
  }

  def load(id: Int): Option[User] = {
    DB.withConnection { implicit connection =>
      SQL("SELECT * from User WHERE id = {id}")
        .on('id -> id)
        .as(userParser.singleOpt)
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