package controllers.api

import javax.inject.{Inject, Singleton}
import models.db.{BallotRepository, SlateRepository}
import models.dto.{BallotDTO, BallotDetailsDTO, FPTPModelDTO}
import play.api.libs.json.{JsResult, Json, _}
import play.api.mvc.{AbstractController, ControllerComponents}

import java.util.UUID
import scala.concurrent.ExecutionContext

/**
 *
 * OUT OF DATE, this needs to be updated later. For now ignore this
 * @param slatesRepo
 * @param ballotRepo
 * @param cc
 * @param ex
 */

@Singleton
class VotingController @Inject()(slatesRepo: SlateRepository, ballotRepo: BallotRepository,
                                 cc: ControllerComponents)
(implicit ex: ExecutionContext) extends AbstractController(cc){

  def loadBallot(ballotID: UUID) = Action.async {
    for {
      slate <- slatesRepo.getSingleSlate(ballotID)
    } yield {
      slate match {
        case Some(x) => Ok("") //Ok(Json.toJson(x))
        case None => NotFound
      }
    }
  }

  def saveBallot(ballotID: Long) = Action.async(parse.json) { request =>
    Console.println(s"Running save ballot, request body: ${request.body}")
    //top level ballot info
    val ballotInfo = (request.body \ "details").get
    val ballotJson: JsResult[BallotDetailsDTO] = Json.fromJson[BallotDetailsDTO](ballotInfo)
    val ballot: BallotDetailsDTO = ballotJson match {
      case JsSuccess(value: BallotDetailsDTO, path: JsPath) => value
      case e @ JsError(_) =>
        Console.println(s"Parse Errors: ${JsError.toJson(e).toString()}")
        null
    }

    //First Past the post model data
    val fptpjson = request.body \ "fptpModel"
    val fptpResult: Option[FPTPModelDTO] = if(fptpjson.isDefined) {
      Json.fromJson[FPTPModelDTO](fptpjson.get) match {
        case JsSuccess(value: FPTPModelDTO, path) => Option(value)
        case e @ JsError(_) => {
          Console.println(s"Parse errors on fptp model: ${JsError.toJson(e).toString}")
          None
        }
      }
    } else None

    //save all data, return result
    for( ballotResult <- ballotRepo.saveBallot(BallotDTO(ballot)))
      yield {
        Ok(Json.toJson(ballotResult))
      }
  }

//  def getSlateResults(slateID: Long) = Action.async {
//    Console.println("running getSlateResults")
//    //for {
//      //voteResults <- ballotRepo.getBallotsForSlate(slateID)
//      //fptpResults <- fptpRepo.getChoicesForBallots(voteResults.map(_.id))
//
//    } yield {
//      //val details: Seq[BallotDetailsDTO] = voteResults.map(ballot => BallotDetailsDTO(Option(ballot.id), ballot.voter, ballot.slateID))
//      //val fptpModel : Seq[FPTPChoiceDTO] = fptpResults.map(choices => FPTPChoiceDTO(choices.questionID, choices.candidateID))
//      //TODO - update to use the updated FPTPChoiceDTO without the ballot ID
//      //val ballots = constructFullBallots(details, fptpModel)
//      //Console.println(s"slate results length: ${ballots.length}, full value: ${ballots.mkString(" ")}")
//      //Ok(Json.toJson(ballots))
//      Ok("TODO: Fix Later")
//    }
//  }

  //TODO - update to use the updated FPTPChoiceDTO without the ballot ID
//  def constructFullBallots(ballotDetails: Seq[BallotDetailsDTO], fptpChoices: Seq[FPTPChoiceDTO]) : Seq[BallotDTO] = {
//    for {
//      ballotDetail <- ballotDetails
//      ballotFPTP = fptpChoices.filter(_.ballotID == ballotDetail.id.getOrElse(0))
//    } yield {
//      BallotDTO(ballotDetail, Option(FPTPModelDTO(ballotFPTP)))
//    }
//  }


}
