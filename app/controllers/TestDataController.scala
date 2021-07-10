package controllers

import models.dto._
import services.{BallotService, SlateService}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Random

/*
To be used for creating test data. Should be removed once code is actually used by users.
 */
class TestDataController @Inject()(slateService: SlateService,
                                   ballotService: BallotService,
                                   scc: SilhouetteControllerComponents)
                                  (implicit ex: ExecutionContext) extends AbstractAuthController(scc){
  def addSlates() = silhouette.UnsecuredAction.async {
    implicit request =>
      for {
        slateResult <- slateService.saveSlate(sampleSlate1)
        ballotResult <- saveBallots()
      } yield {
        Redirect(routes.CreationWebController.slateInfo(slateResult)).flashing("info" -> "Created test slates")
      }
  }

  def saveBallots(): Future[List[Long]] ={
    val ballotTimes:List[Int] = (1 to 50).toList
    Future.sequence(ballotTimes.map[Future[Long]](x => ballotService.saveBallot(generateBallot())))
  }

  val sampleSlate1 = SlateSaveDTO(None, "Sample Slate", "Jordan", true, Seq(
    QuestionDTO(None, "Pick a board game to play:", Seq(
      CandidateDTO(None, "Food Chain Magnate", "A long brutal complicated game with runaway winners"),
      CandidateDTO(None, "Battlestar Galactica", "A long brutal simple game about arguing"),
      CandidateDTO(None, "Carcassonne", "A short modular game about competitive farming"),
      CandidateDTO(None, "Puerto Rico", "A medium game about welcoming immigrants"),
      CandidateDTO(None, "For Sale", "A short game about selling property that no one understands"),
      CandidateDTO(None, "Terra Mystica", "A long complicated game about terraforming and hating neighbors")
    ))
  ))

  def generateBallot() = {
    val r = new Random()
    val bDetails = BallotDetailsDTO(None, "Fake Voter", 1, true)
    val bFPTP = Option(FPTPModelDTO(Seq(FPTPChoiceDTO(1, r.between(1, 6)))))
    val bApproval = Option(ApprovalModelDTO(Seq((1 to 6).map(ApprovalChoiceDTO(1, _, r.nextBoolean())))))

    val optionsShuffled = r.shuffle((1 to 6).toList)
    val choiceAndRank = (1 to 6).toSeq.zip(optionsShuffled)
    val bRanked = Option(RankedModelDTO(Seq(RankedChoiceQuestionDTO(
      choiceAndRank.map(x => RankedChoiceDTO(1, x._1, x._2))
    ))))

    val bRange = Option(RangeModelDTO(Seq(
      (1 to 6).toSeq.map(x => RangeChoiceDTO(1, x, r.between(1, 10)))
    )))

    BallotDTO(bDetails, bFPTP, bApproval, bRanked, bRange)
  }
}
