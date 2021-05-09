package models.db

import javax.inject.Inject
import models.dto.{FPTPCandidateResult, FPTPQuestionResult, FPTPResultsDTO, SlateResultsDTO}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile
import slick.jdbc.H2Profile.api._

import scala.collection.View.Empty.toMap
import scala.concurrent.{ExecutionContext, Future}

class SlateResultsRepository @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)
                            (implicit executionContext: ExecutionContext)
  extends HasDatabaseConfigProvider[JdbcProfile] with DBTableDefinitions {

  //TODO - update this to use slick aggregations? Not sure if it's worth it yet
  def getSlateResults(slateID: Long):Future[SlateResultsDTO] = {
    val query = for {
      ballotIDs <- ballots.filter(_.slateID === slateID).map(_.id).result
      fptpResultsQ <- fptpResults.filter(_.ballotID.inSet(ballotIDs)).result
    } yield {
      val FPTPresult = getFPTPResults(fptpResultsQ)
      SlateResultsDTO(slateID, FPTPresult)
    }
    db.run(query)
  }

  private def getFPTPResults(fptpResults: Seq[FPTPChoice]): FPTPResultsDTO = {
    val questionIDs = fptpResults.map(_.questionID).distinct
    val answerCounts = for(qid <- questionIDs) yield {
      val candidateIDs = fptpResults.filter(_.questionID == qid).map(_.candidateID).distinct
      val aCounts = for(cID <- candidateIDs) yield { (cID -> FPTPCandidateResult(cID, fptpResults.filter(_.candidateID == cID).length))}
      (qid -> FPTPQuestionResult(qid, aCounts.toMap))
    }
    FPTPResultsDTO(fptpResults.length, answerCounts.toMap)
  }
}
