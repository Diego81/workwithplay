package models

case class User(username: String, email: String) {
  var id:Int = User.nextId
}

object User {
  
  private var currentId = 0
  
  def nextId: Int = {
    currentId += 1
    currentId
  }
}