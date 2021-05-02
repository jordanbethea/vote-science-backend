package models.db

import com.google.inject.Inject
import models.dto.{BallotDTO, BallotDetailsDTO, FPTPChoiceDTO, FPTPModelDTO}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.dbio.DBIOAction
import slick.jdbc.H2Profile.api._
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}


case class Ballot(id: Long, slateID: Long, voter: String, anonymous: Boolean)

object Ballot {
  implicit def ballotDTOToDb(dto: BallotDetailsDTO): Ballot = {
    Ballot(dto.id.getOrElse(0), dto.slateID, dto.voter, dto.anonymous)
  }

  //https://github.com/VirtusLab/unicorn/issues/11
  val tupled = (this.apply _).tupled
}

class BallotTableDef(tag: Tag) extends Table[Ballot](tag, "BALLOTS") {
  def id = column[Long]("ID", O.PrimaryKey, O.AutoInc)
  def slateID = column[Long]("SLATE_ID")
  def voter = column[String]("VOTER")
  def anonymous = column[Boolean]("ANONYMOUS")

  override def * = (id, slateID, voter, anonymous).mapTo[Ballot]
  def slate = foreignKey("BALLOT_SLATE_FK", slateID, SlateRepository.slates)(_.id)
}

object BallotRepository {
  val ballots = TableQuery[BallotTableDef]
  val ballotsInserts = ballots returning ballots.map(_.id)
}

class BallotRepository @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)
                                (implicit executionContext: ExecutionContext)
  extends HasDatabaseConfigProvider[JdbcProfile] {

  def saveBallot(ballot: BallotDTO): Future[Long] ={
    addBallotAndModelData(ballot.details, ballot.fptpModel)
  }

  def addBallotAndModelData(ballot: BallotDetailsDTO, fptpData:Option[FPTPModelDTO]) : Future[Long] = {
    Console.println(s"Adding Ballot and model data to db, ballot: ${ballot.toString}, fptp: ${fptpData.toString}")
    val dbBallot : Ballot = Ballot(ballot.id.getOrElse(0), ballot.slateID, ballot.voter, ballot.anonymous)
    val tx = for {
      newBallotId <- BallotRepository.ballotsInserts += dbBallot
      _ <- { if(fptpData.isDefined) FPTPRepository.addFPTPDataQuery(newBallotId, fptpData.get)
              else DBIOAction.successful()}
    } yield(newBallotId)

    tx.transactionally
    db.run(tx)
  }

  def add(ballot: Ballot) : Future[Long] = {
    dbConfig.db.run(BallotRepository.ballotsInserts += ballot)
  }

  def get(id: Long): Future[Option[Ballot]] = {
    dbConfig.db.run(BallotRepository.ballots.filter(_.id === id).result.headOption)
  }

  def listAll() = {
    dbConfig.db.run(BallotRepository.ballots.result)
  }

  def getBallotsForSlate(slateID: Long): Future[Seq[BallotDTO]] = {
    val ballotsQ: DBIO[Seq[Ballot]] = BallotRepository.ballots.filter(_.slateID === slateID).result

    val tx = for {
      ballots <- ballotsQ
      fptpChoices <- FPTPRepository.getFPTPDataForBallotsQuery(ballots.map(_.id))
    } yield {
      ballots.map{
        ballot =>
          val details = new BallotDetailsDTO(Option(ballot.id), ballot.voter, ballot.slateID, ballot.anonymous)
          val choicesForBallot = fptpChoices.filter(_.ballotID == ballot.id)
          val fptpModel = new FPTPModelDTO(choicesForBallot.map(choice => FPTPChoiceDTO(choice.questionID, choice.candidateID)))
          new BallotDTO(details, Option(fptpModel))
      }
    }
    db.run(tx)
  }

}
