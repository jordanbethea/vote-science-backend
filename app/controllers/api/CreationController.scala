package controllers.api

import javax.inject.{Inject, Singleton}
import models.db._
import models.dto.{SlateSaveNewDTO}
import models.dto.SlateDTO._
import play.api.libs.json.{JsResult, Json, _}
import play.api.mvc.{AbstractController, ControllerComponents}

import scala.concurrent.ExecutionContext

@Singleton
class CreationController @Inject()(slatesRepo: SlateRepository, cc: ControllerComponents)
                                  (implicit ex: ExecutionContext) extends AbstractController(cc) {

  def createSlate = Action.async(parse.json) { request =>
    val newSlateParsing: JsResult[SlateSaveNewDTO] = Json.fromJson[SlateSaveNewDTO](request.body)
    val newSlate:SlateSaveNewDTO = newSlateParsing match {
      case JsSuccess(value: SlateSaveNewDTO, path: JsPath) => value
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
    for {
      slateList <- slatesRepo.getFullSlates()
    } yield {
      Console.println("Running actual get slates: :")
      //val jsonSlate = Json.toJson(slateList)
      //Ok(jsonSlate)
      Ok("")
    }
  }

}
