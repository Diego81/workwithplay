package helpers

import org.specs2._

class TextHelperSpec extends mutable.Specification {

  import helpers.TextHelper._
  
  "shortenText" should {
    "not shorten strings shorter than maxLength" in {
      shortenText("Short text", 100) must beEqualTo("Short text")
    }
    "shorten strings longer than maxLength" in {
      shortenText("This text is longer than 10 characters", 10) must beEqualTo("This text ...")
    }
    "not truncate any word in the text" in {
      shortenText("This text is longer than 15 characters", 15) must beEqualTo("This text is ...")
    }
  }
}