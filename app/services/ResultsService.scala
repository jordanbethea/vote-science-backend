package services

import models.dto.votingResults.SlateResultsDTO

import java.util.UUID
import scala.concurrent.Future

trait ResultsService {
  /**
   * Return general voting results for a given slate
   */
  def getSlateResults(slateID:UUID): Future[SlateResultsDTO]
}
