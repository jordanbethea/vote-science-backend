package models.dto.votingResults

import models.dto.SlateLoadDTO
import play.api.libs.json.{JsNumber, JsString, JsValue, Json}

import java.util.UUID

/* Range Voting results */
case class RangeResultBySlate(qResults: Seq[RangeResultByQuestion]){
  def getScore(questionID:UUID, candidateID:UUID): Int = {
    Console.println(s"running getScore:")
    val result = qResults.find(_.questionID == questionID).flatMap(q => Option(q.getTotalScoreForCandidate(candidateID))).getOrElse(0)
    Console.println(s"getScore result: $result")
    result
  }

  def getRawResults(questionID:UUID, candidateID:UUID): Seq[RangeResultByScore] = {
    qResults.find(_.questionID == questionID).flatMap(q => q.questionResults.find(_.candidateID == candidateID).flatMap(c => Option(c.results))).getOrElse(Nil)
  }

  val baseChartObject = Json.obj(
    "type" -> "bar",
    "options" -> Json.obj(
      "indexAxis" -> "x",
      "responsive" -> false
    )
  )

  def getChartJsonInner(questionID:UUID, slate:Option[SlateLoadDTO]=None):JsValue = {
    val qResultCounts = qResults.find(_.questionID == questionID).flatMap(o => Option(
      o.questionResults.map(qResult => (qResult.candidateID, qResult.getTotalScore()))
    )).getOrElse(Nil)
    val data = Json.obj(
      "data" -> Json.obj(
        "labels" -> Json.toJson(qResultCounts.map{q =>
          val name = for {
            s <- slate
            n <- s.candidateName(q._1)
          } yield JsString(n)
          name.getOrElse(JsString(q._1.toString))
        }),
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
case class RangeResultByQuestion(questionID: UUID, questionResults: Seq[RangeResultByCandidate]){
  def getTotalScoreForCandidate(candidateID: UUID): Int = {
    Console.println("running getScoreForCandidate")
    questionResults.find(_.candidateID == candidateID).flatMap(r => Option(r.getTotalScore())).getOrElse(0)
  }
}
case class RangeResultByCandidate(candidateID: UUID, results: Seq[RangeResultByScore]){
  def getTotalScore():Int = {
    results.map(r => r.score * r.votes).sum
  }
}
case class RangeResultByScore(score:Int, votes:Int)
