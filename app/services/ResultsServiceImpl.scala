package services

import javax.inject.Inject
import models.db.VotingResultsRepository
import models.dto.SlateResultsDTO

import scala.concurrent.{ExecutionContext, Future}

class ResultsServiceImpl @Inject() (resultsRepo: VotingResultsRepository) (implicit val ExecutionContext: ExecutionContext)
  extends ResultsService {

  override def getSlateResults(slateID: Long): Future[SlateResultsDTO] = {
    resultsRepo.getSlateResults(slateID)
  }
}
