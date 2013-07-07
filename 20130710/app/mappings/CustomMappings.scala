package mappings

import models._
import play.api.data.format.Formatter
import play.api.data.Mapping
import play.api.data.Forms
import play.api.data.FormError
import play.api.data.FormError

object CustomMappings {

  implicit val contactTypeFormatter = new Formatter[ContactType.ContactType] {
    
    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], ContactType.ContactType] = {
      data.get(key).map { value =>
        try {
        	Right(ContactType.withName(value))
        } catch {
          case e: NoSuchElementException => error(key, value + " is not a valid contact type")
        }
      }.getOrElse(error(key, "No contact type provided."))
    }
    
    private def error(key: String, msg: String) = Left(List(new FormError(key, msg)))
    
    override def unbind(key: String, value: ContactType.ContactType): Map[String, String] = {
      Map(key -> value.toString())
    }
  }
  
  def contactType: Mapping[ContactType.ContactType] = Forms.of[ContactType.ContactType]  
}