package models.db

import com.google.inject.Inject
import models.dto.QuestionDTO
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.H2Profile.api._
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}


class QuestionRepository @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)
                                  (implicit executionContext: ExecutionContext)
  extends HasDatabaseConfigProvider[JdbcProfile] with DBTableDefinitions {

  def createSchema = {
    db.run(questions.schema.create)
  }

  def dropSchema = {
    db.run(questions.schema.drop)
  }

  def listAll: Future[Seq[Question]] = {
    dbConfig.db.run(questions.result)
  }

  def get(id: Long): Future[Option[Question]] = {
    dbConfig.db.run(questions.filter(_.id === id).result.headOption)
  }

  def getForSlate(id: Long): Future[Seq[Question]] = {
    dbConfig.db.run(questions.filter(_.slateID === id).result)
  }

  def add(question: Question): Future[Long] = {
    dbConfig.db.run(questionsInserts += question)
  }

  def addAll(questions: Seq[Question]) = {
    dbConfig.db.run(questionsInserts ++= questions)
  }

  def delete(id: Long): Future[Int] = {
    dbConfig.db.run(questions.filter(_.id === id).delete)
  }
}