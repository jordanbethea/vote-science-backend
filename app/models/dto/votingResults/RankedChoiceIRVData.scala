package models.dto.votingResults

import models.dto.SlateLoadDTO
import play.api.libs.json.{JsNumber, JsString, JsValue, Json}

import java.util.UUID

/* Data for calculating IRV results */
case class RankedChoiceIRVData(questionChoices: Seq[IRVDataSingleQuestionAllBallots], allQuestionIDs: Seq[UUID], totalBallots: Int) {
  //recursive function - pass in 1) list of ballot data 2) eliminated candidates 3) RoundResult data from previous rounds
  def roundOfIRVVoting(round: Int, choices: IRVDataSingleQuestionAllBallots, eliminatedCandidates: Seq[UUID], prevRounds: Seq[IRVRoundResult]): Seq[IRVRoundResult] = {
    val totals = choices.calculateCurrentQuestionResults(eliminatedCandidates, totalBallots)
    val percents = for(total <- totals) yield { total.candidateID -> total.voteCount.toFloat / totalBallots.toFloat}
    Console.println(s"percents: ${percents.toString}")
    val winner = percents.find(_._2 > .5).flatMap(w => Option(w._1))
    val eliminated = if(winner.isDefined) None else Option(percents.minBy(_._2)._1)

    val totRounds = IRVRoundResult(round, totals, winner, eliminated) +: prevRounds

    if(winner.isDefined) totRounds
    else roundOfIRVVoting(round +1, choices, eliminated ++: eliminatedCandidates, totRounds)
  }

  def calculateFullIRVResults(): Seq[IRVResult] = {
    for(questionChoice <- questionChoices) yield {
      IRVResult(questionChoice.questionID, roundOfIRVVoting(1, questionChoice, Nil, Nil).reverse)
    }
  }

  def singleQuestionIRVResults(questionID:UUID): Option[IRVResult] = {
    questionChoices.find(_.questionID == questionID) flatMap { data =>
      Option(IRVResult(questionID, roundOfIRVVoting(0, data, Nil, Nil).reverse))
    }
  }

  //as singleQuestionIRVResults, but altered for easier use in template
  def singleQuestionRoundResults(question: UUID): Seq[IRVRoundResult] = {
    (for {
      rawRes <- singleQuestionIRVResults(question)
    } yield rawRes.roundResults).getOrElse(Nil)
  }
}
/* Seq of Seqs is by list of rankings, grouped by voter/ballot */
case class IRVDataSingleQuestionAllBallots(questionID: UUID, allCandidates:Seq[UUID], rankings: Seq[IRVDataSingleQuestionSingleBallot]){
  def calculateCurrentQuestionResults(excluded:Seq[UUID], totalBallots:Int): Seq[IRVSingleVoteTotal] = {
    val rawResults = rankings.map(_.findCurrentVote(excluded, allCandidates)).flatten
    val uniqueCandidates = rawResults.distinct
    for(c <- uniqueCandidates) yield {
      val candidateVotes = rawResults.filter(id => id == c).size
      IRVSingleVoteTotal(c, candidateVotes, candidateVotes.toFloat / totalBallots.toFloat * 100 )
    }
  }
}
case class IRVDataSingleQuestionSingleBallot(choices: Seq[IRVSingleRank]){
  def findCurrentVote(excluded: Seq[UUID], allCandidates:Seq[UUID]): Option[UUID] = {
    val maxRank = allCandidates.length + 1
    val noVoteCandidates = allCandidates.filterNot(c1 => choices.contains(IRVSingleRank(c1, _)))
    val missingRanks = for(c <- noVoteCandidates) yield { IRVSingleRank(c, maxRank) }
    val fullRankings: Seq[IRVSingleRank] = choices :++ missingRanks
    val sortedRankings = fullRankings.sortBy(_.rank)
    val currentVoteOption = sortedRankings.find(rank => !excluded.contains(rank.candidateID))
    for(option <- currentVoteOption) yield { option.candidateID }
  }
}
case class IRVSingleRank(candidateID: UUID, rank:Int)


/* Results of IRV calculation */
case class IRVResult(questionID: UUID, roundResults: Seq[IRVRoundResult]){

}
case class IRVRoundResult(round:Int, voteTotals:Seq[IRVSingleVoteTotal], winner:Option[UUID], eliminated:Option[UUID]) {
  val baseChartObject = Json.obj(
    "type" -> "bar",
    "options" -> Json.obj(
      "indexAxis" -> "x",
      "responsive" -> false
    )
  )
  def getChartJsonForRound(round:Int, slate:Option[SlateLoadDTO]=None):JsValue = {
    val counts = voteTotals.map(t => t.candidateID -> t.voteCount)
    val data = Json.obj(
      "data" -> Json.obj(
        "labels" -> Json.toJson(counts.map { q =>
          val name = for {
            s <- slate
            n <- s.candidateName(q._1)
          } yield JsString(n)
          name.getOrElse(JsString(q._1.toString))
        }),
        "datasets" -> Json.arr(
          Json.obj(
            "label" -> "Votes per Choice",
            "data" -> Json.toJson(counts.map(q => JsNumber(q._2)))
          )
        )
      ))
    baseChartObject ++ data
  }
}
case class IRVSingleVoteTotal(candidateID:UUID, voteCount:Int, percent:Float)
