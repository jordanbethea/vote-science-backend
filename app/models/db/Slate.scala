package models.db

import javax.inject.Inject
import models.dto.{CandidateDTO, QuestionDTO, SlateDTO}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.H2Profile.api._
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

case class Slate(id: Long, title: String, creator: String, anonymous: Boolean)

object Slate {
  implicit def slateDTOToDB(dto: SlateDTO): Slate = {
    return new Slate(dto.id.getOrElse(0), dto.title, dto.creator, dto.anonymous)
  }

  //https://github.com/VirtusLab/unicorn/issues/11
  val tupled = (this.apply _).tupled
}

class SlateTableDef(tag: Tag) extends Table[Slate](tag, "SLATES") {
  def id = column[Long]("ID", O.PrimaryKey, O.AutoInc)
  def title = column[String]("TITLE")
  def creator = column[String]("CREATOR")
  def anonymous = column[Boolean]("ANONYMOUS")

  override def * = (id, title, creator, anonymous).mapTo[Slate]
}

object SlateRepository {
  val slates = TableQuery[SlateTableDef]
  val slatesInserts = slates returning slates.map(_.id)

  def constructSlateDTO(slates: Seq[Slate],
                        questions: Seq[Question],
                        candidates:Seq[Candidate]): Seq[SlateDTO] = {
    for(s <- slates) yield (
      new SlateDTO(Option(s.id), s.title, s.creator, s.anonymous,
        for(q <- questions.filter(_.slateID == s.id)) yield (
          new QuestionDTO(Option(q.id), q.text,
            candidates.filter(_.questionID == q.id).map(
              c => new CandidateDTO(Option(c.id), c.name, c.description)
            )))))
  }
}

class SlateRepository @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)
                               (implicit executionContext: ExecutionContext)
  extends HasDatabaseConfigProvider[JdbcProfile] {

  def createSchema = {
    db.run(SlateRepository.slates.schema.create)
  }

  def dropSchema = {
    db.run(SlateRepository.slates.schema.drop)
  }

  def createAllLinkedSlateSchemas = {
    val allSchemas = SlateRepository.slates.schema ++ QuestionRepository.questions.schema ++ CandidateRepository.candidates.schema
    db.run(allSchemas.create)
  }

  def dropAllLinkedSlateSchemas = {
    val allSchemas = SlateRepository.slates.schema ++ QuestionRepository.questions.schema ++ CandidateRepository.candidates.schema
    db.run(allSchemas.drop)
  }

  def listAll: Future[Seq[Slate]] = {
    db.run(SlateRepository.slates.result)
  }

  def get(id: Long): Future[Option[Slate]] = {
    db.run(SlateRepository.slates.filter(_.id === id).result.headOption)
  }

  def getFullSlate(id: Long) : Future[Option[SlateDTO]] = {
    val slatesF: Future[Seq[Slate]] = db.run(SlateRepository.slates.filter(_.id === id).result)
    val questionsF: Future[Seq[Question]] = db.run(QuestionRepository.questions.filter(_.slateID === id).result)
    val candidatesF: Future[Seq[Candidate]] = db.run(CandidateRepository.candidates.filter(_.slateID === id).result)
    for {
      slate <- slatesF
      question <- questionsF
      candidates <- candidatesF
    } yield {
      Option(SlateRepository.constructSlateDTO(slate, question, candidates).head)
    }
  }

  def fullAdd(slate: SlateDTO): Future[Long] = {
    def addCandidates(sID: Long, qID: Long, candidates: Seq[CandidateDTO]) : DBIO[Option[Int]] = {
      val dbCandidates = candidates.map(cDTO => Candidate(cDTO.id.getOrElse(0), cDTO.name, cDTO.description, questionID = qID, slateID = sID))
      CandidateRepository.candidates ++= dbCandidates
    }

    def addQuestion(sID: Long, question: QuestionDTO) : DBIO[Unit] = {
      val dbquestion = Question(question.id.getOrElse(0), sID, question.text)
      for {
        newQid <- QuestionRepository.questionsInserts += dbquestion
        _ <- addCandidates(sID, newQid, question.candidates)
      } yield ()
    }

    def addSlateOnly(slate: SlateDTO) : DBIO[Long] = {
      SlateRepository.slatesInserts += Slate(slate.id.getOrElse(0), slate.title, slate.creator, slate.anonymous)
    }

    val tx = addSlateOnly(slate).flatMap {
      slateID =>
        DBIO.sequence(slate.questions.map(addQuestion(slateID, _))).map(_ => slateID)
    }

    tx.transactionally
    db.run(tx)
  }

  def add(slate: Slate): Future[Long] = {
    db.run(SlateRepository.slatesInserts += slate)
  }

  def addAll(slates:Seq[Slate]): Future[Seq[Long]] = {
    db.run(SlateRepository.slatesInserts ++= slates)
  }

  def delete(id: Long): Future[Int] = {
    db.run(SlateRepository.slates.filter(_.id === id).delete)
  }

  def deleteAll() : Future[Int] = {
    db.run(SlateRepository.slates.delete)
  }
}
