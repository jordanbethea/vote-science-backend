package models.db

import javax.inject.Inject
import models.dto.{IRVDataSingleQuestionAllBallots, IRVDataSingleQuestionSingleBallot, IRVResult, IRVSingleRank, NonscoredCandidateResult, NonscoredQuestionResult, NonscoredResultsDTO, RankedChoiceCandidateResult, RankedChoiceIRVData, RankedChoiceQuestionResult, ScoredRankResultsDTO, SlateResultsDTO}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile
import slick.jdbc.H2Profile.api._

import scala.concurrent.{ExecutionContext, Future}

class VotingResultsRepository @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)
                                       (implicit executionContext: ExecutionContext)
  extends HasDatabaseConfigProvider[JdbcProfile] with DBTableDefinitions {

  //TODO - update this to use slick aggregations? Not sure if it's worth it yet
  //Simple version first
  def getSlateResults(slateID: Long):Future[SlateResultsDTO] = {
    val query = for {
      ballotIDs <- ballots.filter(_.slateID === slateID).map(_.id).result
      fptpResultsQ <- fptpResults.filter(_.ballotID.inSet(ballotIDs)).result
      approvalResultsQ <- approvalResults.filter(_.ballotID.inSet(ballotIDs)).result
      rankedResultsQ <- rankedResults.filter(_.ballotID.inSet(ballotIDs)).result
      questionInfoQ <- (questions.filter(_.slateID === slateID) join candidates on (_.id === _.questionID)).result
    } yield {
      val totalBallots = ballotIDs.length
      val FPTPresult = getFPTPResults(fptpResultsQ)
      val approvalResult = getApprovalResults(approvalResultsQ)
      val rankedResult = getRankedResults(rankedResultsQ)
      val rankedIRVResult = getIRVModel(rankedResultsQ, questionInfoQ, totalBallots)
      SlateResultsDTO(slateID, FPTPresult, approvalResult, rankedResult, rankedIRVResult)
    }
    db.run(query)
  }

  private def getFPTPResults(fptpResults: Seq[FPTPChoice]): NonscoredResultsDTO = {
    val questionIDs = fptpResults.map(_.questionID).distinct
    val answerCounts = for(qid <- questionIDs) yield {
      val candidateIDs = fptpResults.filter(_.questionID == qid).map(_.candidateID).distinct
      val aCounts = for(cID <- candidateIDs) yield { (cID -> NonscoredCandidateResult(cID, fptpResults.filter(_.candidateID == cID).length))}
      (qid -> NonscoredQuestionResult(qid, aCounts.toMap))
    }
    NonscoredResultsDTO(fptpResults.length, answerCounts.toMap)
  }

  private def getApprovalResults(approvalResults: Seq[ApprovalChoice]): NonscoredResultsDTO = {
    val questionIDs = approvalResults.map(_.questionID).distinct
    val answerCounts = for(qid <- questionIDs) yield {
      val candidateIDs = approvalResults.filter(_.questionID == qid).map(_.candidateID).distinct
      val aCounts = for(cID <- candidateIDs) yield { (cID -> NonscoredCandidateResult(cID, approvalResults.filter(_.candidateID == cID).length))}
      (qid -> NonscoredQuestionResult(qid, aCounts.toMap))
    }
    NonscoredResultsDTO(approvalResults.length, answerCounts.toMap)
  }

  private def getRankedResults(rankedResults: Seq[RankedChoice]): ScoredRankResultsDTO = {
    val questionIDs = rankedResults.map(_.questionID).distinct
    val choicesByQuestion = (for(qid <- questionIDs) yield {
      val questionChoices = rankedResults.filter(_.questionID == qid)
      val allRanks = questionChoices.map(_.rank).distinct
      val candidateIDs = questionChoices.map(_.candidateID).distinct
      val rankedChoices = (for{cid <- candidateIDs
          rank <- allRanks
          } yield {
        val count = questionChoices.filter(q => q.rank == rank && q.candidateID == cid).length
        RankedChoiceCandidateResult(cid, rank, count)
      }).groupBy(_.candidateID)
      qid -> RankedChoiceQuestionResult(qid, allRanks.length, rankedChoices)
    }).toMap
    ScoredRankResultsDTO(choicesByQuestion)
  }

  private def getIRVModel(rankedResults: Seq[RankedChoice], questionInfo: Seq[(Question, Candidate)], totalBallots:Int): RankedChoiceIRVData = {
    val allQuestionIDs: Seq[Long] = questionInfo.distinctBy(_._1.id).map(_._1.id)
    val questionCandidateCountMap: Map[Long, Seq[Long]] = allQuestionIDs.map(id => (id -> questionInfo.filter(_._1.id == id).map(_._2.id))).toMap

    val questionIDs = rankedResults.map(_.questionID).distinct
    val choices = for(qid <- questionIDs) yield {
      val questionChoices = rankedResults.filter(_.questionID == qid)
      val ballotIDs = questionChoices.map(_.ballotID).distinct
      val singleVoterQuestionChoices = for(ballotID <- ballotIDs) yield {
        val questionRankings: Seq[IRVSingleRank] = questionChoices.filter(_.ballotID == ballotID).map(choice => IRVSingleRank(choice.candidateID, choice.rank))
        IRVDataSingleQuestionSingleBallot(questionRankings)
      }
      IRVDataSingleQuestionAllBallots(qid, questionCandidateCountMap.get(qid).getOrElse(Nil), singleVoterQuestionChoices)
    }

    RankedChoiceIRVData(choices, allQuestionIDs, totalBallots)
  }
}
