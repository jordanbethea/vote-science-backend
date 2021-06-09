package controllers

import javax.inject.{Inject, Singleton}
import models.dto._
import play.api.data._
import play.api.data.Forms._
import services.{BallotService, SlateService}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class VotingWebController @Inject() (slateService: SlateService,
                                     ballotService: BallotService,
                                     scc: SilhouetteControllerComponents)
                                    (implicit ex: ExecutionContext) extends AbstractAuthController(scc) {

  def slateVoteForm(slateID: Long) = silhouette.UserAwareAction.async { implicit request =>
    for {
      slateO <- slateService.slateInfo(slateID)
    } yield {
      slateO match {
        case Some(slate) => Ok(views.html.votingForm(slate, Form(ballotMapping), request.identity))
        case None => NotFound
      }
    }
  }

  def slateVote(slateID: Long) = silhouette.UserAwareAction.async {
    implicit request =>
    Form(ballotMapping).bindFromRequest().fold(
      formWithErrors => {
        Console.println(s"bad form. errors: ${formWithErrors.errors}")
        for {
          slate <- slateService.slateInfo(slateID)
        } yield {
          slate match {
            case Some(slate) => BadRequest (views.html.votingForm (slate, formWithErrors) )
            case None => NotFound
          }
        }
      },
      ballotData => {
        ballotService.saveBallot(ballotData).map {
          result =>
            Console.println(s"Voted on slate. Result: $result")
            Redirect(routes.CreationWebController.slateInfo(slateID))
        }
      }
    )
  }



  val ballotDetailsMapping = mapping(
    "id" -> optional(longNumber),
    "voter" -> text,
    "slateID" -> longNumber,
    "anonymous" -> boolean
  )(BallotDetailsDTO.apply)(BallotDetailsDTO.unapply)

  val fptpMapping = mapping (
    "choices" -> seq(mapping(
      "questionID" -> longNumber,
      "candidateID" -> longNumber
    )(FPTPChoiceDTO.apply)(FPTPChoiceDTO.unapply))
  )(FPTPModelDTO.apply)(FPTPModelDTO.unapply)

  val approvalMapping = mapping(
    "choices" -> seq(seq(mapping(
      "questionID" -> longNumber,
      "candidateID" -> longNumber,
      "approved" -> boolean
    )(ApprovalChoiceDTO.apply)(ApprovalChoiceDTO.unapply)))
  )(ApprovalModelDTO.apply)(ApprovalModelDTO.unapply)

  val rankedMapping = mapping(
    "questions" -> seq(
      mapping("choices" -> seq(mapping(
      "questionID" -> longNumber,
      "candidateID" -> longNumber,
      "rank" -> number
      )(RankedChoiceDTO.apply)(RankedChoiceDTO.unapply))
      )(RankedChoiceQuestionDTO.apply)(RankedChoiceQuestionDTO.unapply).verifying(RankedChoiceQuestionDTO.rankedValuesConstraint)
    ))(RankedModelDTO.apply)(RankedModelDTO.unapply)

  val rangeMapping = mapping(
    "choices" -> seq(seq(mapping(
      "questionID" -> longNumber,
      "candidateID" -> longNumber,
      "score" -> number(min=1, max=10)
    )(RangeChoiceDTO.apply)(RangeChoiceDTO.unapply)))
  )(RangeModelDTO.apply)(RangeModelDTO.unapply)

  val ballotMapping = mapping(
    "details" -> ballotDetailsMapping,
    "fptpModel" -> optional(fptpMapping),
    "approvalModel" -> optional(approvalMapping),
    "rankedModel" -> optional(rankedMapping),
    "rangeModel" -> optional(rangeMapping)
  )(BallotDTO.apply)(BallotDTO.unapply)
}
