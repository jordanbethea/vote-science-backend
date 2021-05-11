package controllers

import javax.inject._
import models.db.{BallotRepository, SlateRepository, VotingResultsRepository}
import models.dto._
import play.api.data._
import play.api.data.Forms._
import scala.concurrent.ExecutionContext

@Singleton
class CreationWebController @Inject()(slatesRepo: SlateRepository,
                                      ballotRepo: BallotRepository,
                                      resultsRepo: VotingResultsRepository,
                                      scc: SilhouetteControllerComponents)
                                     (implicit ex: ExecutionContext) extends AbstractAuthController(scc) {


  def slateInfo(slateID: Long) = silhouette.UserAwareAction.async { implicit request =>

    Console.println(s"Controller - slate info request: ${request.identity}")
    for {
      slateInfo <- slatesRepo.getFullSlate(slateID)
      slateResults <- resultsRepo.getSlateResults(slateID)
    } yield {
      Ok(views.html.slateInfo(slateInfo, slateResults, request.identity))
    }
  }

  def slateList() = silhouette.UserAwareAction.async { implicit request =>
      for {
        slateList <- slatesRepo.getFullSlates()
      } yield {
        Ok(views.html.slateList(slateList.toList, request.identity))
    }
  }

  /**
   * Renders the form to create a new slate
   */
  def createSlateForm() = silhouette.UserAwareAction { implicit request =>
    Ok(views.html.createSlateForm(slateForm, request.identity))
  }

  def createSlate() = silhouette.UserAwareAction.async { implicit request =>
    val slateData = slateForm.bindFromRequest().get
Console.println(s"Submitted slate: ${slateData.toString}")
    slatesRepo.fullAdd(slateData).map {
      result =>
        Console.println(s"Saves slate. Result: ${result}")
        Redirect(routes.CreationWebController.slateInfo(result))
    }
  }

  val slateForm = Form(
    mapping(
      "id" -> optional(longNumber),
      "title" -> text,
      "creator" -> text,
      "anonymous" -> boolean,
      "questions" -> seq(mapping(
        "id" -> optional(longNumber),
        "text" -> text,
        "candidates" -> seq(mapping(
          "id" -> optional(longNumber),
          "name" -> text,
          "description" -> text
        )(CandidateDTO.apply)(CandidateDTO.unapply))
      )(QuestionDTO.apply)(QuestionDTO.unapply))
    )(SlateSaveDTO.apply)(SlateSaveDTO.unapply)
  )

}
