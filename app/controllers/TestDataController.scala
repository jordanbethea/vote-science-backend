package controllers

import models.dto._
import services.{BallotService, SlateService}

import java.util.UUID
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
        slateID <- slateService.saveSlate(sampleSlate1)
        slate <- slateService.slateInfo(slateID)
        ballotResult <- saveBallots(slate.get)
      } yield {
        Redirect(routes.CreationWebController.slateInfo(slateID)).flashing("info" -> "Created test slates")
      }
  }

  def saveBallots(slate: SlateLoadDTO): Future[List[UUID]] ={
    val ballotTimes:List[Int] = (1 to 50).toList
    Future.sequence(ballotTimes.map[Future[UUID]](x => ballotService.saveBallot(generateBallot(slate))))
  }

  val sampleSlate1 = SlateSaveNewDTO("Sample Slate", None, Option("Jordan"), Seq(
    NewQuestionDTO("Pick a board game to play:", Seq(
      NewCandidateDTO("Food Chain Magnate", "A long brutal complicated game with runaway winners"),
      NewCandidateDTO("Battlestar Galactica", "A long brutal simple game about arguing"),
      NewCandidateDTO("Carcassonne", "A short modular game about competitive farming"),
      NewCandidateDTO("Puerto Rico", "A medium game about welcoming immigrants"),
      NewCandidateDTO("For Sale", "A short game about selling property that no one understands"),
      NewCandidateDTO("Terra Mystica", "A long complicated game about terraforming and hating neighbors")
    ))
  ))

  def randomUUID(ids: Seq[UUID]): UUID = {
    val r = new Random()
    val rand = r.between(0, ids.size)
    ids(rand)
  }

  def generateBallot(slate: SlateLoadDTO) = {
    val r = new Random()
    val bID = UUID.randomUUID()
    val bDetails = BallotDetailsDTO(Option(bID), None, slate.id, Option("Fake Voter"))
    val bFPTP = Option(FPTPModelDTO(slate.questions.map(q => FPTPChoiceDTO(q.id, randomUUID(slate.questions.head.candidates.map(_.id))))))
    val bApproval = Option(ApprovalModelDTO(slate.questions.map(q => slate.questions.head.candidates.map(c => ApprovalChoiceDTO(q.id, c.id, r.nextBoolean())))))

    val rankedQuestionData = for(q <- slate.questions) yield {
      val optionsShuffled = r.shuffle(q.candidates.map(_.id).toList)
      val choiceAndRank = (1 to optionsShuffled.size + 1).zip(optionsShuffled)
      RankedChoiceQuestionDTO(choiceAndRank.map(x => RankedChoiceDTO(q.id, x._2, x._1)))
    }
    val bRanked = Option(RankedModelDTO(rankedQuestionData))

    val rangeQuestionData = for(q <- slate.questions) yield {
        for(c <- q.candidates) yield {
          RangeChoiceDTO(q.id, c.id, r.between(1, 10))
        }
    }
    val bRange = Option(RangeModelDTO(rangeQuestionData))

    BallotDTO(bDetails, bFPTP, bApproval, bRanked, bRange)
  }
}
