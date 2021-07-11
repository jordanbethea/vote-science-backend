package models.dto.votingResults

import play.api.libs.json.{JsNumber, JsValue, Json}

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

  val baseChartObject = Json.obj(
    "type" -> "bar",
    "options" -> Json.obj(
      "indexAxis" -> "x",
      "responsive" -> false
    )
  )

  def getChartJsonInner(questionID:Long):JsValue = {
    val qResultCounts = qResults.find(_.questionID == questionID).flatMap(o => Option(
      o.questionResults.map(qResult => (qResult.candidateID, qResult.getTotalScore()))
    )).getOrElse(Nil)
    val data = Json.obj(
      "data" -> Json.obj(
        "labels" -> Json.toJson(qResultCounts.map(q => JsNumber(q._1))),
        "datasets" -> Json.arr(
          Json.obj(
            "label" -> "Score per Choice",
            "data" -> Json.toJson(qResultCounts.map(q => JsNumber(q._2)))
          )
        )
      ))
    baseChartObject ++ data
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
