package models.db

import com.google.inject.Inject
import models.dto.{FPTPChoiceDTO, FPTPModelDTO}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.H2Profile.api._
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

case class FPTPChoice(ballotID: Long, questionID: Long, candidateID:Long)

object FPTPChoice {
  implicit def choiceDTOToDB(dto: FPTPChoiceDTO, ballotID: Long): FPTPChoice = {
    FPTPChoice(ballotID, dto.questionID, dto.candidateID)
  }

  implicit def seqDTOToSeqDB(dto: Seq[FPTPChoiceDTO], ballotID: Long): Seq[FPTPChoice] = {
    for {
      i <- dto
    } yield {
      choiceDTOToDB(i, ballotID)
    }
  }

  //https://github.com/VirtusLab/unicorn/issues/11
  val tupled = (this.apply _).tupled
}

class FPTPTableDef(tag: Tag) extends Table[FPTPChoice](tag, "FPTP_CHOICES") {
  def ballotID = column[Long]("BALLOT_ID")
  def questionID = column[Long]("QUESTION_ID")
  def candidateID = column[Long]("CANDIDATE_ID")

  override def * = (ballotID, questionID, candidateID).mapTo[FPTPChoice]
  def ballotKey = foreignKey("FPTP_BALLOT_FK", ballotID, BallotRepository.ballots)(_.id)
  def questionKey = foreignKey("FPTP_QUESTION_FK", questionID, QuestionRepository.questions)(_.id)
  def candidateKey = foreignKey("FPTP_CANDIDATE_FK", candidateID, CandidateRepository.candidates)(_.id)
}

object FPTPRepository {
  val fptpResults = TableQuery[FPTPTableDef]

  def addFPTPDataQuery(ballotID: Long, fptpData: FPTPModelDTO): DBIO[Option[Int]] = {
    val choicesWithBallotID = fptpData.choices.map(entry => FPTPChoice(ballotID, entry.questionID, entry.candidateID))
    FPTPRepository.fptpResults ++= choicesWithBallotID
  }

  def getFPTPDataForBallotsQuery(ballotIDs: Seq[Long]): DBIO[Seq[FPTPChoice]] = {
    FPTPRepository.fptpResults.filter(_.ballotID.inSet(ballotIDs)).result
  }
}

class FPTPRepository @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)
                              (implicit executionContext: ExecutionContext)
                extends HasDatabaseConfigProvider[JdbcProfile]{

    def addRun(choice: FPTPChoice): Future[Int] = {
    dbConfig.db.run(FPTPRepository.fptpResults += choice)
  }

  def addAllRun(choices: Seq[FPTPChoice]): Future[Option[Int]] = {
    dbConfig.db.run(FPTPRepository.fptpResults ++= choices)
  }

  def getChoicesForBallot(ballotID: Long): Future[Seq[FPTPChoice]] = {
    dbConfig.db.run(FPTPRepository.fptpResults.filter(_.ballotID === ballotID).result)
  }

  def getChoicesForBallots(ballotIDs: Seq[Long]): Future[Seq[FPTPChoice]] = {
    dbConfig.db.run(FPTPRepository.fptpResults.filter(_.ballotID.inSet(ballotIDs)).result)
  }

  def listAll() = {
    dbConfig.db.run(FPTPRepository.fptpResults.result)
  }
}
