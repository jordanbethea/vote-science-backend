package services

import javax.inject.Inject
import models.db.VotingResultsRepository
import models.dto.votingResults.SlateResultsDTO

import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}

class ResultsServiceImpl @Inject() (resultsRepo: VotingResultsRepository) (implicit val ExecutionContext: ExecutionContext)
  extends ResultsService {

  override def getSlateResults(slateID: UUID): Future[SlateResultsDTO] = {
    resultsRepo.getSlateResults(slateID)
  }
}
