package models

import anorm._
import anorm.SqlParser._
import play.api.db.DB
import play.api.Play.current

case class User(id: Pk[Int] = NotAssigned, username: String, age: Int, private val groups: List[UserGroup]) {

  def this(id: Pk[Int], username: String, age: Int) = this(id, username, age, List())

  def contacts: List[Contact] =
    id.map { userId =>
      Contact.loadByUserId(userId)
    }.getOrElse(Nil)

  lazy val userGroups: List[UserGroup] =
      id.map { userId =>
        UserGroup.listByUser(userId)
      }.getOrElse(List[UserGroup]())
}

object User {

  def save(user: User) = {
    DB.withTransaction { implicit connection =>
      val idLong = SQL(""" 
    		  INSERT INTO User(username, age) 
    		  VALUES({username}, {age})
      """).on(
        'username -> user.username,
        'age -> user.age).executeInsert()
      val id = idLong.get.toInt
      addGroups(id, user.groups)
      id
    }
  }

  private def addGroups(userId: Int, userGroups: List[UserGroup])(implicit connection: java.sql.Connection) {
    for (userGroup <- userGroups) {
      SQL("""
        		INSERT INTO UserToUserGroup(userId, userGroupId)
        		VALUES({userId}, {userGroupId})
            """).on('userId -> userId,
        'userGroupId -> userGroup.id).executeInsert()
    }
  }

  private val userParser: RowParser[User] =
    get[Pk[Int]]("id") ~
      get[String]("username") ~
      get[Int]("age") map {
        case id ~ username ~ age => new User(id, username, age)
      }

  def list = {
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
    DB.withTransaction { implicit connection =>
      SQL(""" 
    		  UPDATE User SET
    		  username = {username},
    		  age = {age}
    		  WHERE id = {id}
      """).on(
        'id -> id,
        'username -> user.username,
        'age -> user.age).executeUpdate
      deleteGroups(id)
      addGroups(id, user.groups)
    }
  }

  private def deleteGroups(id: Int)(implicit connection: java.sql.Connection) {
    SQL(""" 
          DELETE FROM UserToUserGroup where userId = {userId}
      """).on(
      'userId -> id).executeUpdate
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
      deleteGroups(id)
    }
  }
}