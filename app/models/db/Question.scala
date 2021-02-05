package models.db

import com.google.inject.Inject
import models.dto.QuestionDTO
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.H2Profile.api._
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}


case class Question (id: Long, slateID: Long, text:String)

object Question {
  implicit def questionDTOToQuestions(slateID: Long, questionDTO: QuestionDTO): Question = {
    new Question(slateID, questionDTO.id.getOrElse(0), questionDTO.text)
  }

  //https://github.com/VirtusLab/unicorn/issues/11
  val tupled = (this.apply _).tupled
}

class QuestionTableDef(tag: Tag) extends Table[Question](tag, "QUESTIONS") {
  def id = column[Long]("ID", O.PrimaryKey, O.AutoInc)
  def slateID = column[Long]("SLATE_ID")
  def text = column[String]("TEXT")

  override def * = (id, slateID, text).mapTo[Question]
  def slate = foreignKey("SLATE_QUESTION_FK", slateID, SlateRepository.slates)(_.id)
}

object QuestionRepository {
  val questions = TableQuery[QuestionTableDef]
  val questionsInserts = questions returning questions.map(_.id)
}

class QuestionRepository @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)
                                  (implicit executionContext: ExecutionContext)
  extends HasDatabaseConfigProvider[JdbcProfile] {

  def createSchema = {
    db.run(QuestionRepository.questions.schema.create)
  }

  def dropSchema = {
    db.run(QuestionRepository.questions.schema.drop)
  }

  def listAll: Future[Seq[Question]] = {
    dbConfig.db.run(QuestionRepository.questions.result)
  }

  def get(id: Long): Future[Option[Question]] = {
    dbConfig.db.run(QuestionRepository.questions.filter(_.id === id).result.headOption)
  }

  def getForSlate(id: Long): Future[Seq[Question]] = {
    dbConfig.db.run(QuestionRepository.questions.filter(_.slateID === id).result)
  }

  def add(question: Question): Future[Long] = {
    dbConfig.db.run(QuestionRepository.questionsInserts += question)
  }

  def addAll(questions: Seq[Question]) = {
    dbConfig.db.run(QuestionRepository.questionsInserts ++= questions)
  }

  def delete(id: Long): Future[Int] = {
    dbConfig.db.run(QuestionRepository.questions.filter(_.id === id).delete)
  }
}