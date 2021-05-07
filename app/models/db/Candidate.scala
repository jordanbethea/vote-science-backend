package models.db

import com.google.inject.Inject
import models.dto.CandidateDTO
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.H2Profile.api._
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

class CandidateRepository @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)
                                   (implicit executionContext: ExecutionContext)
  extends HasDatabaseConfigProvider[JdbcProfile] with DBTableDefinitions {

  def createSchema = {
    dbConfig.db.run(candidates.schema.create)
  }

  def dropSchema = {
    dbConfig.db.run(candidates.schema.drop)
  }

  def listAll: Future[Seq[Candidate]] = {
    dbConfig.db.run(candidates.result)
  }

  def get(id: Long): Future[Option[Candidate]] = {
    dbConfig.db.run(candidates.filter(_.id === id).result.headOption)
  }

  def getForQuestions(ids: Seq[Long]): Future[Seq[Candidate]] = {
    //For future reference - first version doesn't work because contains uses == and slick
    //requires ===, =!=
    //dbConfig.db.run(CandidateRepository.candidates.filter(c => ids.contains(c.questionID)).result.headOption)
    dbConfig.db.run(candidates.filter(_.questionID.inSet(ids)).result)
  }

  def add(candidate: Candidate): Future[Long] = {
    dbConfig.db.run(candidatesInsert += candidate)
  }

  def addAll(candidates: Seq[Candidate]): Future[Seq[Long]] = {
    dbConfig.db.run(candidatesInsert ++= candidates)
  }

  def delete(id: Long): Future[Int] = {
    dbConfig.db.run(candidates.filter(_.id === id).delete)
  }
}