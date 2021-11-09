package models.dto.votingResults

import models.dto.SlateLoadDTO
import play.api.libs.json._

import java.util.UUID

case class NonscoredResultsDTO(totalBallots:Int, totalCounts:Map[UUID, NonscoredQuestionResult]){
  def getCountForCandidate(questionID: UUID, candidateID:UUID):Int = {
    try{ totalCounts(questionID).candidateCounts(candidateID).totalVotes}
    catch { case e: NoSuchElementException => 0 }
  }

  def getAllCountsForQuestion(questionID: UUID): Seq[(UUID, Int)] = {
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

  def getChartJsonForQuestion(questionID:UUID, slate:Option[SlateLoadDTO] = None):JsValue = {
    val counts = getAllCountsForQuestion(questionID)
    val data = Json.obj(
      "data" -> Json.obj(
      "labels" -> Json.toJson(counts.map{
        q =>
          val name = for{
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

  def getChartJsonAsString(questionID:UUID, slate:Option[SlateLoadDTO]=None):String = {
    Json.stringify(getChartJsonForQuestion(questionID, slate))
  }
}
case class NonscoredQuestionResult(questionID:UUID, candidateCounts:Map[UUID, NonscoredCandidateResult])
case class NonscoredCandidateResult(candidateID:UUID, totalVotes:Int)
