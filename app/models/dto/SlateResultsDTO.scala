package models.dto

case class SlateResultsDTO (slateID: Long, fptpResults: NonscoredResultsDTO, approvalResults: NonscoredResultsDTO,
                            rankedResults: RankedResultsDTO)

case class NonscoredResultsDTO(totalBallots:Int, totalCounts:Map[Long, NonscoredQuestionResult]){
  def getCountForCandidate(questionID: Option[Long], candidateID:Option[Long]):Int = {
    try{ totalCounts(questionID.get).candidateCounts(candidateID.get).totalVotes}
    catch { case e: NoSuchElementException => 0 }
  }
}
case class NonscoredQuestionResult(questionID:Long, candidateCounts:Map[Long, NonscoredCandidateResult])
case class NonscoredCandidateResult(candidateID:Long, totalVotes:Int)


case class RankedResultsDTO(choicesByQuestion:Map[Long, RankedChoiceQuestionResult]) {

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
}

case class RankedChoiceQuestionResult(questionID:Long, candidateCounts: Map[Long, Seq[RankedChoiceCandidateResult]])
case class RankedChoiceCandidateResult(candidateID:Long, rankChosen:Int, totalVotes:Int)