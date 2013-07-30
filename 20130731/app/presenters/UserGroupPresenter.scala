package presenters

class UserGroupPresenter(userGroup: models.UserGroup) {

  def shortDescription = {
    helpers.TextHelper.shortenText(userGroup.description, 25)
  }  
}

object UserGroupPresenter { 
  implicit def userGroup2UserGroupPresenter(userGroup: models.UserGroup) = new UserGroupPresenter(userGroup)
}