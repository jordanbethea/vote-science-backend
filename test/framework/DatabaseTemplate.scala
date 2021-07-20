package framework

import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._


trait DatabaseTemplate {

  lazy val inMemoryDataBaseConf: Map[String, Any] = Map[String, Any](
    "slick.dbs.test.profile" -> "slick.jdbc.H2Profile$",
    "slick.dbs.test.db.driver" -> "org.h2.Driver",
    "slick.dbs.test.db.url" -> "jdbc:h2:mem:play",
    "play.evolutions.autoApply" -> true
  )

  val baseApplication: Application = new GuiceApplicationBuilder()
    .configure(inMemoryDataBaseConf).build()

  def exec[T](future: Future[T]): T =
    Await.result(future , 2.seconds)


  //pattern for testing with non-slick databases
  /* def withTestDatabase[T](block: Database => T) = {
    Databases.withInMemory(
     name = "testDatabase",
      urlOptions = Map(
        "MODE" -> "H2"
      ),
      config = Map(
        "logStatements" -> true
      )
    )(block)
  } */
}