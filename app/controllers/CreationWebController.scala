package controllers

import javax.inject._
import models.db.{CandidateRepository, QuestionRepository, SlateRepository}
import models.dto._
import play.api.data._
import play.api.data.Forms._
import play.api.mvc._

import scala.concurrent.ExecutionContext

@Singleton
class CreationWebController @Inject()(slatesRepo: SlateRepository, questionRepo: QuestionRepository,
                                      candidateRepo: CandidateRepository,
                                       val controllerComponents: ControllerComponents,
                                      messagesAction: MessagesActionBuilder)
                                     (implicit ex: ExecutionContext) extends BaseController {


  def slateInfo(slateID: Long) = Action.async {
    slatesRepo.getFullSlate(slateID).map { result =>
      Ok(views.html.slateInfo(result))
    }
  }

  def slateList() = Action.async {
    val allSlatesF = slatesRepo.listAll
    val allQuestionsF = questionRepo.listAll
    val allCandidatesF = candidateRepo.listAll

    for {
      slates <- allSlatesF
      questions <- allQuestionsF
      candidates <- allCandidatesF
    } yield {
      Console.println("Running actual get slates: :")
//      Console.println(Json.toJson(SlateRepository.constructSlateDTO(slates, questions, candidates)))
//      val jsonSlate = Json.toJson(SlateRepository.constructSlateDTO(slates, questions, candidates))
      val slateList = SlateRepository.constructSlateDTO(slates, questions, candidates)
      Ok(views.html.slateList(slateList.toList))
    }
  }

  /**
   * Renders the form to create a new slate
   */
  def createSlateForm() = messagesAction {
    implicit request : MessagesRequest[AnyContent] =>
    Ok(views.html.createSlateForm(slateForm))
  }

  def createSlate() = Action.async {
    implicit request =>
    val slateData = slateForm.bindFromRequest.get
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
      "questions" -> seq(mapping(
        "id" -> optional(longNumber),
        "text" -> text,
        "candidates" -> seq(mapping(
          "id" -> optional(longNumber),
          "name" -> text,
          "description" -> text
        )(CandidateDTO.apply)(CandidateDTO.unapply))
      )(QuestionDTO.apply)(QuestionDTO.unapply))
    )(SlateDTO.apply)(SlateDTO.unapply)
  )

}
