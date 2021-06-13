package models.dto.votingResults

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

