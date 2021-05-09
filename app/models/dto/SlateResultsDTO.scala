package models.dto

case class SlateResultsDTO (slateID: Long, fptpResults: FPTPResultsDTO)

case class FPTPResultsDTO(totalBallots:Int, totalCounts: Map[Long, FPTPQuestionResult]) {
  def getCountForCandidate(questionID: Option[Long], candidateID: Option[Long]): Int = {
    try {
      totalCounts(questionID.get).candidateCounts(candidateID.get).totalVotes
    } catch {
      //smooth out all the Option gets into a this
      case e:NoSuchElementException => 0
    }
  }
}
case class FPTPQuestionResult(questionID: Long, candidateCounts: Map[Long, FPTPCandidateResult])
case class FPTPCandidateResult(candidateID: Long, totalVotes:Int)