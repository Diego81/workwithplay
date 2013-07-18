package models

import anorm._
import anorm.SqlParser._
import play.api.db.DB
import play.api.Play.current

case class UserGroup(id: anorm.Pk[Int], name: String, description: String)

object UserGroup {

  def save(group: UserGroup) = {
    val id = DB.withConnection { implicit connection =>
      SQL(""" 
    		  INSERT INTO UserGroup(name, description) 
    		  VALUES({name}, {description})
      """).on(
        'name-> group.name,
        'description -> group.description).executeInsert()
    }
    id.get.toInt
  }

  private def parser: RowParser[UserGroup] = {
    get[Pk[Int]]("id") ~
      get[String]("name") ~
      get[String]("description") map {
	  case id ~ name ~ description => UserGroup(id, name, description)
  }
  }

  def list: List[UserGroup] = {
    DB.withConnection { implicit connection =>
      SQL("SELECT * from UserGroup").as(parser *)
    }
  }

  def load(id: Int): Option[UserGroup] = {
    DB.withConnection { implicit connection =>
      SQL("SELECT * from UserGroup WHERE id = {id}")
        .on('id -> id)
        .as(parser.singleOpt)
    }
  }

  def update(id: Int, group: UserGroup) {
    DB.withConnection { implicit connection =>
      SQL(""" 
    		  UPDATE UserGroup SET
    		  name = {name},
    		  description = {description}
    		  WHERE id = {id}
      """).on(
        'id -> id,
        'name-> group.name,
        'description-> group.description).executeUpdate
    }
  }

  def delete(id: Int) {
    DB.withConnection { implicit connection =>
      SQL(""" 
          DELETE FROM UserGroup where id = {id}
      """).on(
        'id -> id).executeUpdate
    }
  }
}