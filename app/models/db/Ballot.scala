package models.db

import com.google.inject.Inject
import models.dto.{ApprovalModelDTO, BallotDTO, BallotDetailsDTO, FPTPChoiceDTO, FPTPModelDTO, RangeModelDTO, RankedModelDTO}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.dbio.DBIOAction
import slick.jdbc.H2Profile.api._
import slick.jdbc.JdbcProfile
import models.User

import scala.concurrent.{ExecutionContext, Future}

class BallotRepository @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)
                                (implicit executionContext: ExecutionContext)
  extends HasDatabaseConfigProvider[JdbcProfile] with DBTableDefinitions {

  def saveBallot(ballot: BallotDTO): Future[Long] ={
    Console.println(s"Adding Ballot and model data to db, ballot: ${ballot.toString}, fptp: ${ballot.fptpModel.toString}")
    val dbBallot : Ballot = Ballot(ballot.details.id.getOrElse(0), ballot.details.slateID, ballot.details.voter, ballot.details.anonymous)
    val tx = for {
      newBallotId <- ballotsInserts += dbBallot
      _ <- { if(ballot.fptpModel.isDefined)
              fptpResults ++= ballot.fptpModel.get.choices.map(entry => FPTPChoice(newBallotId, entry.questionID, entry.candidateID))
              else DBIOAction.successful()}
      _ <- { if(ballot.approvalModel.isDefined)
              approvalResults ++= ballot.approvalModel.get.choices.flatten.filter(_.approved).map(entry => ApprovalChoice(newBallotId,entry.questionID, entry.candidateID))
            else DBIOAction.successful()}
      _ <- { if(ballot.rankedModel.isDefined)
            rankedResults ++= ballot.rankedModel.get.questions.flatMap(_.choices).map(entry => RankedChoice(newBallotId, entry.questionID, entry.candidateID, entry.rank))
            else DBIOAction.successful()}
      _ <- { if(ballot.rangeModel.isDefined)
            rangeResults ++= ballot.rangeModel.get.choices.flatten.map(entry => RangeChoice(newBallotId, entry.questionID, entry.candidateID, entry.score))
            else DBIOAction.successful()}
    } yield newBallotId

    tx.transactionally
    db.run(tx)
  }

  def add(ballot: Ballot) : Future[Long] = {
    dbConfig.db.run(ballotsInserts += ballot)
  }

  def addDTO(ballot: BallotDTO) : Future[Long] = {
    dbConfig.db.run(ballotsInserts += Ballot(ballot.details.id.getOrElse(0), ballot.details.slateID,
      ballot.details.voter, ballot.details.anonymous))
  }

  def get(id: Long): Future[Option[Ballot]] = {
    dbConfig.db.run(ballots.filter(_.id === id).result.headOption)
  }

  def listAll() = {
    dbConfig.db.run(ballots.result)
  }

  def getBallotsForUser(user: User) = {
    val q = for {
      ballotIDs <- ballots.filter(_.voter === user.userID.toString).result
    } yield {
      ballotIDs.map(b => BallotDTO(BallotDetailsDTO(Option(b.id), b.voter, b.slateID, b.anonymous)))
    }
    db.run(q)
  }

//  //TODO - is this needed any more? Will we ever need to load raw ballot data not part of the results?
//  def getBallotsForSlate(slateID: Long): Future[Seq[BallotDTO]] = {
//    val ballotsQ: DBIO[Seq[Ballot]] = ballots.filter(_.slateID === slateID).result
//
//    val tx = for {
//      ballots <- ballotsQ
//      fptpChoices <- fptpResults.filter(_.ballotID.inSet(ballots.map(_.id))).result
//    } yield {
//      ballots.map{
//        ballot =>
//          val details = new BallotDetailsDTO(Option(ballot.id), ballot.voter, ballot.slateID, ballot.anonymous)
//          val choicesForBallot = fptpChoices.filter(_.ballotID == ballot.id)
//          val fptpModel = new FPTPModelDTO(choicesForBallot.map(choice => FPTPChoiceDTO(choice.questionID, choice.candidateID)))
//          new BallotDTO(details, Option(fptpModel))
//      }
//    }
//    db.run(tx)
//  }

}
