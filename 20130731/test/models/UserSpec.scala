package models

import org.specs2._
import integration.WithTestDatabase
import org.specs2.specification.Scope

class UserSpec extends mutable.Specification with WithTestDatabase {

  "List" should {
    "return all records" in new users {      
      User.list.size must beEqualTo(1)
    }
  }
  
  "Delete" should {
    "remove a record" in new users {
      User.delete(userId)
      User.list must beEmpty
    }
  }
  
  class users extends Scope {
    val userId = User.save(new User(anorm.NotAssigned, "John", 30))
  }
}