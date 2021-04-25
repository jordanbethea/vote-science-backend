package controllers.api

import javax.inject.{Inject, Singleton}
import models.db._
import models.dto.SlateDTO
import play.api.libs.json.{JsResult, Json, _}
import play.api.mvc.{AbstractController, ControllerComponents}

import scala.concurrent.ExecutionContext

@Singleton
class CreationController @Inject()(slatesRepo: SlateRepository,
                                   questionRepo: QuestionRepository,
                                   candidateRepo: CandidateRepository,
                                   cc: ControllerComponents)
                                  (implicit ex: ExecutionContext) extends AbstractController(cc) {

  def createSlate = Action.async(parse.json) { request =>
    val newSlateParsing: JsResult[SlateDTO] = Json.fromJson[SlateDTO](request.body);
    val newSlate:SlateDTO = newSlateParsing match {
      case JsSuccess(value: SlateDTO, path: JsPath) => value
      case e @ JsError(_) =>
        Console.println(s"Parse errors: ${JsError.toJson(e).toString()} ")
        null
    }

    //val questions = newSlate.questions
    //val candidates = questions.flatMap(question => question.candidates)

    Console.println(s"Request: ${request.toString()}");
    Console.println(s"params?: ${request.body}");
    Console.println(s"parsed slate: ${(if(newSlate == null) "it's null" else newSlate.toString())}")

    slatesRepo.fullAdd(newSlate).map { result =>
      Ok(Json.obj("content" -> s"Successfully created Slate: $result"))
    }
  }

  def getSlates = Action.async {val allSlatesF = slatesRepo.listAll
    val allQuestionsF = questionRepo.listAll
    val allCandidatesF = candidateRepo.listAll

    for {
      slates <- allSlatesF
      questions <- allQuestionsF
      candidates <- allCandidatesF
    } yield {
      Console.println("Running actual get slates: :")
      Console.println(Json.toJson(SlateRepository.constructSlateDTO(slates, questions, candidates)))
      val jsonSlate = Json.toJson(SlateRepository.constructSlateDTO(slates, questions, candidates))
      Ok(jsonSlate)
    }

  }

}
