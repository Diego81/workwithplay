package spring

import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Bean
import controllers._
import models._

@Configuration
class SpringConfiguration {

  private val userGroupManager = new UserGroupManager
  private val contactManager = new ContactManager
  private val userManager = new UserManager(userGroupManager, contactManager)

  @Bean
  def applicationController = new Application

  @Bean
  def usersController = new Users(userManager, userGroupManager)

  @Bean
  def groupsController = new UserGroups(userGroupManager)

  @Bean
  def contactsController = new Contacts(contactManager, userManager)
}