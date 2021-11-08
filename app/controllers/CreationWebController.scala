package controllers

import javax.inject._
import models.dto._
import play.api.data._
import play.api.data.Forms._
import play.api.i18n.Messages
import services.{ResultsService, SlateService}

import java.util.UUID
import scala.concurrent.ExecutionContext

@Singleton
class CreationWebController @Inject()(slateService: SlateService,
                                      resultsService: ResultsService,
                                      scc: SilhouetteControllerComponents)
                                     (implicit ex: ExecutionContext) extends AbstractAuthController(scc) {


  def slateInfo(slateID: UUID) = silhouette.UserAwareAction.async { implicit request =>

    Console.println(s"Controller - slate info request: ${request.identity}")
    for {
      slateInfo <- slateService.slateInfo(slateID)
      slateResults <- resultsService.getSlateResults(slateID)
    } yield {
      Ok(views.html.slateInfo(slateInfo, slateResults, request.identity))
    }
  }

  def slateList() = silhouette.UserAwareAction.async { implicit request =>
      for {
        slateList <- slateService.slateList()
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
    slateService.saveSlate(slateData).map {
      result =>
        Console.println(s"Saves slate. Result: ${result}")
        Redirect(routes.CreationWebController.slateInfo(result)).flashing("info" -> Messages("createSlate.success"))
    }
  }

  val slateForm = Form(
    mapping(
      "title" -> nonEmptyText(maxLength=250),
      "creatorID" -> optional(uuid),
      "creatorText" -> optional(text(maxLength=250)),
      "questions" -> seq(mapping(
        "text" -> text,
        "candidates" -> seq(mapping(
          "name" -> text,
          "description" -> text
        )(NewCandidateDTO.apply)(NewCandidateDTO.unapply))
      )(NewQuestionDTO.apply)(NewQuestionDTO.unapply))
    )(SlateSaveNewDTO.apply)(SlateSaveNewDTO.unapply)
  )
}
