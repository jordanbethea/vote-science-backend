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

  def getSlatesMadeByUser(user:User) : Future[Seq[SlateLoadDTO]] =  {
    val r = for {
      slateIDList <- slates.filter(_.creator === user.userID.toString).result
      slateLoads <- getFullSlatesHelper(slateIDList.map(_.id))
    } yield {
      slateLoads
    }
    db.run(r)
  }

  def getSingleSlate(id: Long) : Future[Option[SlateLoadDTO]] = {
    val q = for {
      slate <- slates.filter(_.id === id).result
      slateLoads <- getFullSlatesHelper(slate.map(_.id))
    } yield {
      Option(slateLoads.head)
    }
    db.run(q)
  }

  def getFullSlates(): Future[Seq[SlateLoadDTO]] ={
    val query = for {
      slates <- slates.result
      slateLoads <- getFullSlatesHelper(slates.map(_.id))
    } yield {
      slateLoads
    }
    db.run(query)
  }

  def getSlatesFromList(slateIDs: Seq[Long]): Future[Seq[SlateLoadDTO]] = {
    val query = for {
      slates <- slates.filter(_.id.inSet(slateIDs)).result
      slateLoads <- getFullSlatesHelper(slates.map(_.id))
    } yield {
      slateLoads
    }
    db.run(query)
  }

  private def getFullSlatesHelper(slateIDs: Seq[Long]): DBIOAction[Seq[SlateLoadDTO], NoStream, Effect.Read] = {
    for {
      slate <- slates.filter(_.id.inSet(slateIDs)).result
      question <- questions.filter(_.slateID.inSet(slateIDs)).result
      candidates <- candidates.filter(_.slateID.inSet(slateIDs)).result
      user <- slickUsers.filter(_.id.inSet(slate.map(_.creator))).result
    } yield {
      constructSlateDTO(slate, question, candidates, Option(user))
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



  def constructSlateDTO(slates: Seq[Slate],
                        questions: Seq[Question],
                        candidates:Seq[Candidate],
                        creators:Option[Seq[DBUser]] = None): Seq[SlateLoadDTO] = {
    for(s <- slates) yield (
      new SlateLoadDTO(Option(s.id), s.title,
        if(creators.nonEmpty){
          val result = creators.get.find(_.userID == s.creator);
          if(result.nonEmpty){ Right(result.get)} else {Left(s.creator)}
        } else { Left(s.creator) },
        for(q <- questions.filter(_.slateID == s.id)) yield (
          new QuestionDTO(Option(q.id), q.text,
            candidates.filter(_.questionID == q.id).map(
              c => new CandidateDTO(Option(c.id), c.name, c.description)
            )))
      ))
  }

  //Might not be best practice, but I just want user info to pass through to slate,
  //I don't want to use DB object, and don't need login info
  //TODO - think about a better solution for this
  private implicit def dbUserToGenUser(user: DBUser):User = {
    User(UUID.fromString(user.userID), null,
      user.firstName, user.lastName, user.fullName, user.email, user.avatarURL)
  }
}
