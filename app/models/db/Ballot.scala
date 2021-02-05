package models.db

import com.google.inject.Inject
import models.dto.{BallotDetailsDTO, FPTPModelDTO}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.dbio.DBIOAction
import slick.jdbc.H2Profile.api._
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}


case class Ballot(id: Long, slateID: Long, voter: String)

object Ballot {
  implicit def ballotDTOToDb(dto: BallotDetailsDTO): Ballot = {
    Ballot(dto.id.getOrElse(0), dto.slateID, dto.voter)
  }

  //https://github.com/VirtusLab/unicorn/issues/11
  val tupled = (this.apply _).tupled
}

class BallotTableDef(tag: Tag) extends Table[Ballot](tag, "BALLOTS") {
  def id = column[Long]("ID", O.PrimaryKey, O.AutoInc)
  def slateID = column[Long]("SLATE_ID")
  def voter = column[String]("VOTER")

  override def * = (id, slateID, voter).mapTo[Ballot]
  def slate = foreignKey("BALLOT_SLATE_FK", slateID, SlateRepository.slates)(_.id)
}

object BallotRepository {
  val ballots = TableQuery[BallotTableDef]
  val ballotsInserts = ballots returning ballots.map(_.id)
}

class BallotRepository @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)
                                (implicit executionContext: ExecutionContext)
  extends HasDatabaseConfigProvider[JdbcProfile] {

  def addBallotAndModelData(ballot: BallotDetailsDTO, fptpData:Option[FPTPModelDTO]) : Future[Long] = {
    Console.println(s"Adding Ballot and model data to db, ballot: ${ballot.toString}, fptp: ${fptpData.toString}")
    val dbBallot : Ballot = Ballot(ballot.id.getOrElse(0), ballot.slateID, ballot.voter)
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

  def getBallotsForSlate(slateID: Long): Future[Seq[Ballot]] = {
    //Console.println(s"running getBallotsForSlate in ballot repo - total ballots ${BallotRepository.ballots.length.results}")
    dbConfig.db.run(BallotRepository.ballots.filter(_.slateID === slateID).result)
  }


}
