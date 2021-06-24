package models.dto.votingResults

import play.api.libs.json._

case class NonscoredResultsDTO(totalBallots:Int, totalCounts:Map[Long, NonscoredQuestionResult]){
  def getCountForCandidate(questionID: Option[Long], candidateID:Option[Long]):Int = {
    try{ totalCounts(questionID.get).candidateCounts(candidateID.get).totalVotes}
    catch { case e: NoSuchElementException => 0 }
  }

  def getAllCountsForQuestion(questionID: Long): Seq[(Long, Int)] = {
    totalCounts.get(questionID).flatMap(s => Option(s.candidateCounts.toSeq.map {
      c =>
        (c._2.candidateID, c._2.totalVotes)
    })).getOrElse(Nil).sortWith(_._2 < _._2)
  }

  val baseChartObject = Json.obj(
    "type" -> "bar",
    "options" -> Json.obj(
      "indexAxis" -> "x",
      "responsive" -> false
    )
  )

  def getChartJsonForQuestion(questionID:Long):JsValue = {
    val counts = getAllCountsForQuestion(questionID)
    val data = Json.obj(
      "data" -> Json.obj(
      "labels" -> Json.toJson(counts.map(q => JsNumber(q._1))),
      "datasets" -> Json.arr(
        Json.obj(
        "label" -> "Votes per Choice",
          "data" -> Json.toJson(counts.map(q => JsNumber(q._2)))
        )
      )
    ))
    baseChartObject ++ data
  }

  def getChartJsonAsString(questionID:Long):String = {
    Json.stringify(getChartJsonForQuestion(questionID))
  }
}
case class NonscoredQuestionResult(questionID:Long, candidateCounts:Map[Long, NonscoredCandidateResult])
case class NonscoredCandidateResult(candidateID:Long, totalVotes:Int)
