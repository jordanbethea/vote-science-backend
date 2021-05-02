package controllers

import javax.inject.{Inject, Singleton}
import models.db.{BallotRepository, FPTPRepository, SlateRepository}
import models.dto.{BallotDTO, BallotDetailsDTO, FPTPChoiceDTO, FPTPModelDTO}
import play.api.libs.json.{JsResult, Json, _}
import play.api.data._
import play.api.data.Forms._
import play.api.mvc.{AbstractController, AnyContent, BaseController, ControllerComponents, MessagesActionBuilder, MessagesRequest}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class VotingWebController @Inject() (slatesRepo: SlateRepository, ballotRepo: BallotRepository,
                                     scc: SilhouetteControllerComponents)
                                    (implicit ex: ExecutionContext) extends AbstractAuthController(scc) {

  def slateVoteForm(slateID: Long) = silhouette.UserAwareAction.async { implicit request =>
    for {
      slate <- slatesRepo.getFullSlate(slateID)
    } yield {
      slate match {
        case Some(x) => Ok(views.html.votingForm(slate.get, Form(ballotMapping), request.identity))
        case None => NotFound
      }
    }
  }

  def slateVote(slateID: Long) = silhouette.UserAwareAction.async {
    implicit request =>
    Form(ballotMapping).bindFromRequest.fold(
      formWithErrors => {
        Console.println(s"bad form: ${formWithErrors}")
        Future(BadRequest("Bad Form"))
        //BadRequest(views.html.votingForm(null, formWithErrors))
      },
      ballotData => {
        ballotRepo.saveBallot(ballotData).map {
          result =>
            Console.println(s"Voted on slate. Result: ${result}")
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

  val ballotMapping = mapping(
    "details" -> ballotDetailsMapping,
    "fptpModel" -> optional(fptpMapping)
  )(BallotDTO.apply)(BallotDTO.unapply)
}
