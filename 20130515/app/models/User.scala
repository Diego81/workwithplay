package models

import anorm._
import anorm.SqlParser._
import play.api.db.DB
import play.api.Play.current

case class User(id: Pk[Int] = NotAssigned, username: String, email: String, age: Int) 

object User {
  def save(user: User) {
    DB.withConnection { implicit connection =>
      SQL(""" 
    		  INSERT INTO User(username, email, age) 
    		  VALUES({username}, {email}, {age})
      """).on(
          'username -> user.username,
          'email -> user.email,
          'age -> user.age
      ).executeUpdate
    }
  }
  
  private val userParser: RowParser[User] = {
    get[Pk[Int]]("id") ~
      get[String]("username") ~
      get[String]("email") ~
      get[Int]("age") map {
      case id ~ username ~ email ~ age => User(id, username, email, age)
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
    		  email = {email},
    		  age = {age}
    		  WHERE id = {id}
      """).on(
          'id -> id,
          'username -> user.username,
          'email -> user.email,
          'age -> user.age
      ).executeUpdate
    }
  }
  
  def delete(id: Int) {
    DB.withConnection { implicit connection =>
      SQL(""" 
          DELETE FROM User where id = {id}
      """).on(
          'id -> id
      ).executeUpdate
    }
  }
}