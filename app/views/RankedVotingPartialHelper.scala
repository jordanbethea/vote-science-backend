package views

import models.dto.{BallotDTO, QuestionDTO, SlateLoadDTO}
import play.api.data.{Field, Form, FormError}

case class radioRankingData(rank: Int, showLabel:Boolean, candidateIDOptions: Seq[Long])

object RankedVotingPartialHelper {

  /**
   * Helper function for Radio button version. Not currently used. Takes in size of candidate list
   * returns combo of 'Rank' and boolean for whether to display label for that rank of radio buttons
   */
  def rankDisplayCreator(candidateListSize: Int): Seq[(Int, Boolean)] = {
    (1 until candidateListSize).toSeq.map(_ -> false) :+ (candidateListSize -> true)
  }
  /** Used for Radio button version. Not currently used. */
  def radioOptionsHelper(question: QuestionDTO, showLabel: Boolean): Seq[(String, Any)] = {
    question.candidates.map(c => (c.id.getOrElse(0).toString, if(showLabel) c.name else ""))
  }

  def createFields(ballotForm: Form[BallotDTO], question: QuestionDTO, questionCount: Int): List[Field] = {
    (1 to question.candidates.length).toList.flatMap{ rankChoice =>
      question.candidates.flatMap(c => Seq(
        ballotForm(s"rankedModel.choices[$questionCount][$rankChoice].candidateID"),
        ballotForm(s"rankedModel.choices[$questionCount][$rankChoice].questionID"),
        ballotForm(s"rankedModel.choices[$questionCount][$rankChoice].rank")))
    }
  }

  def createFieldOptions(question: QuestionDTO): Seq[(Symbol, Any)] = {
    (1 to question.candidates.length).toList.flatMap { rankChoice =>
      question.candidates.flatMap( a =>
        //Seq(Symbol("value") -> null, Symbol("value") -> question.id.getOrElse(0), Symbol("value") -> rankChoice)
        Seq(Symbol("value") -> null, Symbol("value") -> null, Symbol("value") -> null)
      )
    }
  }

  def getErrorForQuestion(ballotForm: Form[BallotDTO], questionPosition: Int, slateInfo: SlateLoadDTO): Option[FormError] = {
    val key = s"rankedModel.questions[${questionPosition}]"
    val errorOpt = ballotForm.error(key)
    Console.println(s"Error for key ${key}: ${errorOpt.toString}")
    errorOpt match {
      case Some(error) => {
        val cID:Long = error.args.head.asInstanceOf[Long]
        val candidateText = slateInfo.candidateName(cID).getOrElse("")
        Option(FormError(key, error.message, Seq(candidateText)))
      }
      case None => None
    }
  }
}