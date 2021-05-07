package models.db

import com.google.inject.Inject
import models.dto.{FPTPChoiceDTO, FPTPModelDTO}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.H2Profile.api._
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

class FPTPRepository @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)
                              (implicit executionContext: ExecutionContext)
                extends HasDatabaseConfigProvider[JdbcProfile] with DBTableDefinitions {

    def addRun(choice: FPTPChoice): Future[Int] = {
    dbConfig.db.run(fptpResults += choice)
  }

  def addAllRun(choices: Seq[FPTPChoice]): Future[Option[Int]] = {
    dbConfig.db.run(fptpResults ++= choices)
  }

  def getChoicesForBallot(ballotID: Long): Future[Seq[FPTPChoice]] = {
    dbConfig.db.run(fptpResults.filter(_.ballotID === ballotID).result)
  }

  def getChoicesForBallots(ballotIDs: Seq[Long]): Future[Seq[FPTPChoice]] = {
    dbConfig.db.run(fptpResults.filter(_.ballotID.inSet(ballotIDs)).result)
  }

  def listAll() = {
    dbConfig.db.run(fptpResults.result)
  }
}
