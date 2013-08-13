package models

import org.specs2._
import org.specs2.mock.Mockito
import integration.WithTestDatabase
import org.specs2.specification.Scope
import java.text.SimpleDateFormat
import java.util.Date

class UserManagerSpec extends mutable.Specification with WithTestDatabase {

  "List" should {
    "return all records" in new users {
      userManager.list.size must beEqualTo(1)
    }
  }

  "Delete" should {
    "remove a record" in new users {
      userManager.delete(userId)
      userManager.list must beEmpty
    }
  }

  class users extends Scope {
    val userManager = new UserManager(new UserGroupManager, new ContactManager)
    val userId = userManager.save(new User(anorm.NotAssigned, "John", 30, List()))
  }
}
