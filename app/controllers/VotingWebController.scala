package controllers

import javax.inject.{Inject, Singleton}
import models.db.{BallotRepository, FPTPRepository, SlateRepository}
import models.dto.{BallotDTO, BallotDetailsDTO, FPTPChoiceDTO, FPTPModelDTO}
import play.api.libs.json.{JsResult, Json, _}
import play.api.mvc.{AbstractController, ControllerComponents}

import scala.concurrent.ExecutionContext

@Singleton
class VotingWebController @Inject() (slatesRepo: SlateRepository, ballotRepo: BallotRepository,
                                     fptpRepo: FPTPRepository, cc: ControllerComponents)
                                    (implicit ex: ExecutionContext) extends AbstractController(cc) {

  def loadBallot(ballotID: Long) = Action.async {
    for {
      slate <- slatesRepo.getFullSlate(ballotID)
    } yield {
      slate match {
        case Some(x) => Ok(views.html.votingForm(slate.get))
        case None => NotFound
      }
    }
  }


}
