package models.dto.votingResults

import play.api.libs.json.{JsNumber, JsValue, Json}

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

  def getBordaScoreForCandidate(questionID:Long, candidateID:Long): Float = {
    choicesByQuestion.get(questionID).flatMap(r => Option(r.bordaCountForCandidate(candidateID))).getOrElse(0f)
  }

  def getBordaScores(questionID:Long): Iterable[(Long, Float)] = {
    choicesByQuestion.get(questionID).flatMap(r => Option(r.bordaCount())).getOrElse(Nil)
  }

  def getDowdalScores(questionID:Long): Iterable[(Long, Float)] = {
    choicesByQuestion.get(questionID).flatMap(r => Option(r.dowdalCount())).getOrElse(Nil)
  }

  def getDowdallScoreForCandidate(questionID:Long, candidateID:Long): Float = {
    choicesByQuestion.get(questionID).flatMap(r => Option(r.dowdalCountForCandidate(candidateID))).getOrElse(0f)
  }

  val baseChartObject = Json.obj(
    "type" -> "bar",
    "options" -> Json.obj(
      "indexAxis" -> "x",
      "responsive" -> false
    )
  )

  def getBordaChartJson(questionID:Long): JsValue = {
    val scores = getBordaScores(questionID)
    getChartJsonInner(questionID, scores)
  }

  def getDowdalChartJson(questionID:Long): JsValue = {
    val scores = getDowdalScores(questionID)
    getChartJsonInner(questionID, scores)
  }

  private def getChartJsonInner(questionID:Long, results:Iterable[(Long, Float)]):JsValue = {
    val data = Json.obj(
      "data" -> Json.obj(
        "labels" -> Json.toJson(results.map(q => JsNumber(q._1))),
        "datasets" -> Json.arr(
          Json.obj(
            "label" -> "Score per Choice",
            "data" -> Json.toJson(results.map(q => JsNumber(q._2)))
          )
        )
      ))
    baseChartObject ++ data
  }
}

case class RankedChoiceQuestionResult(questionID:Long, maxRank: Int, candidateCounts: Map[Long, Seq[RankedChoiceCandidateResult]]){
  private val bordaVal: RankedChoiceCandidateResult => Float = { res => res.totalVotes * (maxRank - res.rankChosen)}
  private val dowdalVal: RankedChoiceCandidateResult => Float = { res => res.totalVotes * (1f / res.rankChosen)}

  private def countForSingleCandidate(candidateID: Long, counterScheme:RankedChoiceCandidateResult => Float):Float = {
    val optcount:Option[Float] = candidateCounts.get(candidateID).flatMap(results => Option(results.map(counterScheme).sum))
    optcount.getOrElse(0f)
  }
  private def countsForAllCandidates(counterScheme:RankedChoiceCandidateResult => Float):Iterable[(Long, Float)] = {
    for(candidateID <- candidateCounts.keys) yield {
        (candidateID, countForSingleCandidate(candidateID, counterScheme))
    }
  }

  def bordaCountForCandidate(candidateID:Long) = countForSingleCandidate(candidateID, bordaVal)
  def dowdalCountForCandidate(candidateID:Long) = countForSingleCandidate(candidateID, dowdalVal)
  def bordaCount() = countsForAllCandidates(bordaVal)
  def dowdalCount() = countsForAllCandidates(dowdalVal)
}
case class RankedChoiceCandidateResult(candidateID:Long, rankChosen:Int, totalVotes:Int)

