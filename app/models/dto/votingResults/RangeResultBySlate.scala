package models.dto.votingResults

/* Range Voting results */
case class RangeResultBySlate(qResults: Seq[RangeResultByQuestion]){
  def getScore(questionID:Long, candidateID:Long): Int = {
    Console.println(s"running getScore:")
    val result = qResults.find(_.questionID == questionID).flatMap(q => Option(q.getTotalScoreForCandidate(candidateID))).getOrElse(0)
    Console.println(s"getScore result: $result")
    result
  }

  def getRawResults(questionID:Long, candidateID:Long): Seq[RangeResultByScore] = {
    qResults.find(_.questionID == questionID).flatMap(q => q.questionResults.find(_.candidateID == candidateID).flatMap(c => Option(c.results))).getOrElse(Nil)
  }
}
case class RangeResultByQuestion(questionID: Long, questionResults: Seq[RangeResultByCandidate]){
  def getTotalScoreForCandidate(candidateID: Long): Int = {
    Console.println("running getScoreForCandidate")
    questionResults.find(_.candidateID == candidateID).flatMap(r => Option(r.getTotalScore())).getOrElse(0)
  }
}
case class RangeResultByCandidate(candidateID: Long, results: Seq[RangeResultByScore]){
  def getTotalScore():Int = {
    results.map(r => r.score * r.votes).sum
  }
}
case class RangeResultByScore(score:Int, votes:Int)
