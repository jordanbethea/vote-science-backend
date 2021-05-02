package controllers

import javax.inject._
import models.db.{BallotRepository, CandidateRepository, QuestionRepository, SlateRepository}
import models.dto._
import play.api.data._
import play.api.data.Forms._
import play.api.mvc._

import scala.concurrent.ExecutionContext

@Singleton
class CreationWebController @Inject()(slatesRepo: SlateRepository, questionRepo: QuestionRepository,
                                      candidateRepo: CandidateRepository,
                                      ballotRepo: BallotRepository,
                                      scc: SilhouetteControllerComponents,
                                      messagesAction: MessagesActionBuilder)
                                     (implicit ex: ExecutionContext) extends AbstractAuthController(scc) {


  def slateInfo(slateID: Long) = silhouette.UserAwareAction.async { implicit request =>

    for {
      slateInfo <- slatesRepo.getFullSlate(slateID)
      ballots <- ballotRepo.getBallotsForSlate(slateID)
    } yield {
      Ok(views.html.slateInfo(slateInfo, ballots, request.identity))
    }

  }

  def slateList() = silhouette.UserAwareAction.async { implicit request =>
    val allSlatesF = slatesRepo.listAll
    val allQuestionsF = questionRepo.listAll
    val allCandidatesF = candidateRepo.listAll

    for {
      slates <- allSlatesF
      questions <- allQuestionsF
      candidates <- allCandidatesF
    } yield {
      Console.println("Running actual get slates: :")
      val slateList = SlateRepository.constructSlateDTO(slates, questions, candidates).toList
      Ok(views.html.slateList(slateList, request.identity))
    }
  }

  /**
   * Renders the form to create a new slate
   */
  def createSlateForm() = silhouette.UserAwareAction { implicit request =>
    Ok(views.html.createSlateForm(slateForm, request.identity))
  }

  def createSlate() = silhouette.UserAwareAction.async { implicit request =>
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
    )(SlateDTO.apply)(SlateDTO.unapply)
  )

}
