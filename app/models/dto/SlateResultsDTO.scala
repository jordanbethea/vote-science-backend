package models.dto

case class SlateResultsDTO (slateID: Long, fptpResults: NonscoredResultsDTO, approvalResults: NonscoredResultsDTO,
                            rankedResults: ScoredRankResultsDTO, irvResult: RankedChoiceIRVData)

case class NonscoredResultsDTO(totalBallots:Int, totalCounts:Map[Long, NonscoredQuestionResult]){
  def getCountForCandidate(questionID: Option[Long], candidateID:Option[Long]):Int = {
    try{ totalCounts(questionID.get).candidateCounts(candidateID.get).totalVotes}
    catch { case e: NoSuchElementException => 0 }
  }
}
case class NonscoredQuestionResult(questionID:Long, candidateCounts:Map[Long, NonscoredCandidateResult])
case class NonscoredCandidateResult(candidateID:Long, totalVotes:Int)

/**
 * Class for generating Ranked voting results for counting methods involving scoring - IE Borda or Dowdall method
 */
case class ScoredRankResultsDTO(choicesByQuestion:Map[Long, RankedChoiceQuestionResult]) {

  def getOrderedRanksForCandidates(questionID: Long, candidateID:Long): Seq[RankedChoiceCandidateResult] = {
    val choicesForQ = for {
      choicesByQ <- choicesByQuestion.get(questionID)
      choicesByC <- choicesByQ.candidateCounts.get(candidateID)
    } yield { choicesByC }

    choicesForQ match {
      case None => Nil
      case Some(items) => items.sortBy(_.rankChosen)
    }
  }

  def getBordaScoreForCandidate(questionID:Long, candidateID:Long): Int = {
    val choicesForQ = for {
      choicesByQ <- choicesByQuestion.get(questionID)
      choicesByC <- choicesByQ.candidateCounts.get(candidateID)
    } yield { choicesByC }

    if(choicesForQ.nonEmpty){
      val maxRank = choicesByQuestion.get(questionID).get.maxRank
      choicesForQ.get.map(c => c.totalVotes * (maxRank - c.rankChosen)).sum
    } else 0
  }

  def getDowdallScoreForCandidate(questionID:Long, candidateID:Long): Float = {
    val choicesForQ = for {
      choicesByQ <- choicesByQuestion.get(questionID)
      choicesByC <- choicesByQ.candidateCounts.get(candidateID)
    } yield { choicesByC }

    if(choicesForQ.nonEmpty){
      val maxRank = choicesByQuestion.get(questionID).get.maxRank
      choicesForQ.get.map(c => c.totalVotes * (1f / c.rankChosen)).sum
    } else 0
  }
}

case class RankedChoiceQuestionResult(questionID:Long, maxRank: Int, candidateCounts: Map[Long, Seq[RankedChoiceCandidateResult]])
case class RankedChoiceCandidateResult(candidateID:Long, rankChosen:Int, totalVotes:Int)


/* Data for calculating IRV results */
case class RankedChoiceIRVData(questionChoices: Seq[IRVDataSingleQuestionAllBallots]) {
  //recursive function - pass in 1) list of ballot data 2) eliminated candidates 3) RoundResult data from previous rounds
  def roundOfIRVVoting(round: Int, choices: IRVDataSingleQuestionAllBallots, eliminatedCandidates: Seq[Long], prevRounds: Seq[IRVRoundResult]): Seq[IRVRoundResult] = {
    val totals = choices.calculateCurrentQuestionResults(eliminatedCandidates)
    val percents = for(total <- totals) yield { total.candidateID -> total.voteCount.toFloat / totals.size.toFloat}
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

  def singleQuestionIRVResults(questionID:Long): Option[IRVResult] = {
    questionChoices.find(_.questionID == questionID) flatMap { data =>
      Option(IRVResult(questionID, roundOfIRVVoting(0, data, Nil, Nil).reverse))
    }

  }
}
/* Seq of Seqs is by list of rankings, grouped by voter/ballot */
case class IRVDataSingleQuestionAllBallots(questionID: Long, rankings: Seq[IRVDataSingleQuestionSingleBallot]){
  def calculateCurrentQuestionResults(excluded:Seq[Long]): Seq[IRVSingleVoteTotal] = {
    val rawResults = rankings.map(_.findCurrentVote(excluded)).flatten
    val uniqueCandidates = rawResults.distinct
    for(c <- uniqueCandidates) yield {
      IRVSingleVoteTotal(c, rawResults.filter(id => id == c).size)
    }
  }
}
case class IRVDataSingleQuestionSingleBallot(choices: Seq[IRVSingleRank]){
  def findCurrentVote(excluded: Seq[Long]): Option[Long] = {
    val sortedRankings = choices.sortBy(_.rank)
    val currentVoteOption = sortedRankings.find(rank => !excluded.contains(rank.candidateID))
    for(option <- currentVoteOption) yield { option.candidateID }
  }
}
case class IRVSingleRank(candidateID: Long, rank:Int)


/* Results of IRV calculation */
case class IRVResult(questionID: Long, roundResults: Seq[IRVRoundResult])
case class IRVRoundResult(round:Int, voteTotals:Seq[IRVSingleVoteTotal], winner:Option[Long], eliminated:Option[Long])
case class IRVSingleVoteTotal(candidateID:Long, voteCount:Int)