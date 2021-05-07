package models.db

import java.util.UUID

import javax.inject.Inject
import models.dto.{CandidateDTO, QuestionDTO, SlateDTO, SlateLoadDTO, SlateSaveDTO}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.H2Profile.api._
import slick.jdbc.JdbcProfile
import models.User

import scala.concurrent.{ExecutionContext, Future}

class SlateRepository @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)
                               (implicit executionContext: ExecutionContext)
  extends HasDatabaseConfigProvider[JdbcProfile] with DBTableDefinitions {

  def createSchema = {
    db.run(slates.schema.create)
  }

  def dropSchema = {
    db.run(slates.schema.drop)
  }

  def createAllLinkedSlateSchemas = {
    val allSchemas = slates.schema ++ questions.schema ++ candidates.schema
    db.run(allSchemas.create)
  }

  def dropAllLinkedSlateSchemas = {
    val allSchemas = slates.schema ++ questions.schema ++ candidates.schema
    db.run(allSchemas.drop)
  }

  def listAll: Future[Seq[Slate]] = {
    db.run(slates.result)
  }

  def get(id: Long): Future[Option[Slate]] = {
    db.run(slates.filter(_.id === id).result.headOption)
  }

  def getFullSlate(id: Long) : Future[Option[SlateLoadDTO]] = {
    val slatesF: Future[Seq[Slate]] = db.run(slates.filter(_.id === id).result)
    val questionsF: Future[Seq[Question]] = db.run(questions.filter(_.slateID === id).result)
    val candidatesF: Future[Seq[Candidate]] = db.run(candidates.filter(_.slateID === id).result)
    for {
      slate <- slatesF
      question <- questionsF
      candidates <- candidatesF
    } yield {
      Option(constructSlateDTO(slate, question, candidates).head)
    }
  }

  def fullAdd(slate: SlateSaveDTO): Future[Long] = {
    def addCandidates(sID: Long, qID: Long, inputcandidates: Seq[CandidateDTO]) : DBIO[Option[Int]] = {
      val dbCandidates = inputcandidates.map(cDTO => Candidate(cDTO.id.getOrElse(0), cDTO.name, cDTO.description, questionID = qID, slateID = sID))
      candidates ++= dbCandidates
    }

    def addQuestion(sID: Long, question: QuestionDTO) : DBIO[Unit] = {
      val dbquestion = Question(question.id.getOrElse(0), sID, question.text)
      for {
        newQid <- questionsInserts += dbquestion
        _ <- addCandidates(sID, newQid, question.candidates)
      } yield ()
    }

    def addSlateOnly(slate: SlateSaveDTO) : DBIO[Long] = {
      slatesInserts += Slate(slate.id.getOrElse(0), slate.title, slate.creator, slate.anonymous)
    }

    val tx = addSlateOnly(slate).flatMap {
      slateID =>
        DBIO.sequence(slate.questions.map(addQuestion(slateID, _))).map(_ => slateID)
    }

    tx.transactionally
    db.run(tx)
  }

  def add(slate: Slate): Future[Long] = {
    db.run(slatesInserts += slate)
  }

  def addAll(slates:Seq[Slate]): Future[Seq[Long]] = {
    db.run(slatesInserts ++= slates)
  }

  def delete(id: Long): Future[Int] = {
    db.run(slates.filter(_.id === id).delete)
  }

  def deleteAll() : Future[Int] = {
    db.run(slates.delete)
  }

  def getAllFullSlates(): Future[Seq[SlateLoadDTO]] ={
    val query = for {
      slates <- slates.result
      questions <- questions.result
      candidates <- candidates.result
      userIDStrings = slates.filter(_.anonymous == false).map(s => s.creator)
      dbUsers <- slickUsers.filter(_.id.inSet(userIDStrings)).result
    } yield {
      val users = dbUsers.map { user => User(UUID.fromString(user.userID), null,
           user.firstName, user.lastName, user.fullName, user.email, user.avatarURL)}
      constructSlateDTO(slates, questions, candidates, Option(users)).toList
    }

    db.run(query)
  }

  def constructSlateDTO(slates: Seq[Slate],
                        questions: Seq[Question],
                        candidates:Seq[Candidate],
                        creators:Option[Seq[User]] = None): Seq[SlateLoadDTO] = {
    for(s <- slates) yield (
      new SlateLoadDTO(Option(s.id), s.title,
        if(creators.nonEmpty){
          val result = creators.get.find(_.userID.toString() == s.creator);
          if(result.nonEmpty){ Right(result.get)} else {Left(s.creator)}
        } else { Left(s.creator) },
        for(q <- questions.filter(_.slateID == s.id)) yield (
          new QuestionDTO(Option(q.id), q.text,
            candidates.filter(_.questionID == q.id).map(
              c => new CandidateDTO(Option(c.id), c.name, c.description)
            )))
      ))
  }
}
