package models

import org.specs2._
import org.specs2.specification._
import org.specs2.execute.AsResult

class UserSpec extends mutable.Specification {
  
  "department" should {
    "be the first word for a domain with 3 words" in new users {
      user.department must beSome("hr")
    }
    "be None for a domain with 2 words" in new users("example.com"){
      user.department must beNone
    }
  }
  "managers" should {
    "be older than 40 and part of the management department" in new users("management.example.com", 42) {
      user must beOldManager
    }
  }
  
  class users(domain: String = "hr.example.com", age: Int = 24) extends mutable.After {
      lazy val user = User(anorm.NotAssigned, "John Doe", s"johndoe@$domain", age)
      
      def after = println("This line is executed after the body of each Example")
  }

  def beOldManager = {(u: User) => (u.department must beSome("management")) and (u.age must beGreaterThan(40))}

}

class SpecWithAfterExample extends mutable.Specification with AfterExample {
  
  def after = println("Code executed after the test")

  "Empty test" should {
    "always succeed" in {
      success
    }
  }
}

class SpecWithFakeTransaction extends mutable.Specification with AroundExample {

	def around[T : AsResult](t: =>T) = {
	  println("start transaction")
	  try {
		  val result = AsResult(t)
		  println("commit transaction")
		  result
	  } catch {
	  	case e: Exception => {
	  	  println("rollback transaction")
	  	  failure(e.getMessage())
	  	}
	  }
    }
		
	step(println("before all"))
	"fake transaction" should {
	  "successfully commit to the database" in success
	}

	"fake transaction 2" should {
	  "successfully commit to the database" in success
	}
	"pending test" should {
	  "be skipped" in { 
	    println("some code currently not working")
	    pending
	  }
	}
	
	step(println("after all"))
}

class UserAcceptanceSpec extends Specification { def is = 
  
  "department should"											^
  "be the first word before the first dot in the email domain"  ! example^
  																end
  
  def example = User(anorm.NotAssigned, "John Doe", "johndoe@hr.example.com", 24).department must beSome("hr")
}