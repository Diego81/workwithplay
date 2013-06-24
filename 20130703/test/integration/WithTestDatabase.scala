package integration

import org.specs2.specification._
import org.specs2.execute.AsResult
import play.api.test._
import play.api.test.Helpers._
import play.api.test.{ WithApplication, FakeApplication }
import org.apache.commons.io.FileUtils
import play.api.db.DB
import java.io.File
import scala.collection.immutable.TreeSet

trait WithTestDatabase extends AroundExample {

  val testDb = Map("db.default.url" -> "jdbc:mysql://localhost:3306/user_admin_test?characterEncoding=UTF-8")

  def around[T: AsResult](t: => T) = {
    val app = FakeApplication(additionalConfiguration = testDb)
    running(app) {
      new SchemaManager(app).dropCreateSchema
      AsResult(t)
    }
  }

  private class SchemaManager(val app: FakeApplication) {

    private val evolutions = parseEvolutions

    private def parseEvolutions = {
      import scala.collection.JavaConversions._
      val evolutions = FileUtils.listFiles(app.getFile("conf/evolutions/default/"), Array("sql"), false)
      evolutions.map { evolution =>
        val evolutionContent = FileUtils.readFileToString(evolution)
        val splittedEvolutionContent = evolutionContent.split("# --- !Ups")
        val upsDowns = splittedEvolutionContent(1).split("# --- !Downs")
        val index = evolution.getName().replace(".sql", "").toInt
        new Evolution(index, upsDowns(0), upsDowns(1))
      }
    }

    def dropCreateSchema {
      dropSchema
      createSchema
    }

    private def dropSchema = orderEvolutions(new EvolutionsOrderingDesc).foreach { _.runDown(app) }

    private def createSchema = orderEvolutions(new EvolutionsOrderingAsc).foreach { _.runUp(app) }

    private def orderEvolutions(ordering: Ordering[Evolution]) = {
      evolutions.foldLeft(new TreeSet[Evolution]()(ordering)) { (treeSet, evolution) =>
        treeSet + evolution
      }
    }
  }

  private class EvolutionsOrderingDesc extends Ordering[Evolution] {
    override def compare(a: Evolution, b: Evolution): Int = b.index compare a.index
  }

  private class EvolutionsOrderingAsc extends Ordering[Evolution] {
    def compare(a: Evolution, b: Evolution) = a.index compare b.index
  }

  private case class Evolution(val index: Int, up: String, down: String) {

    private val upQueries = up.trim.split(";")
    private val downQueries = down.trim.split(";")

    def runUp(implicit app: FakeApplication) = {
      runQueries(upQueries)
    }

    def runDown(implicit app: FakeApplication) = {
      runQueries(downQueries)
    }

    private def runQueries(queries: Array[String])(implicit app: FakeApplication) {
      DB.withTransaction { conn =>
        queries.foreach { conn.createStatement.execute(_) }
      }
    }
    
  }
  
}