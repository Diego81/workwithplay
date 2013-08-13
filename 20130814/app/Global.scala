import play.api.GlobalSettings
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.AnnotationConfigApplicationContext
import spring.SpringConfiguration

object Global extends GlobalSettings {

  private var ctx: ApplicationContext = new AnnotationConfigApplicationContext(classOf[SpringConfiguration])

  override def getControllerInstance[A](clazz: Class[A]): A = {
    return ctx.getBean(clazz);
  }

}