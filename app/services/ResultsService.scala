package services

import models.dto.votingResults.SlateResultsDTO

import scala.concurrent.Future

trait ResultsService {
  /**
   * Return general voting results for a given slate
   */
  def getSlateResults(slateID:Long): Future[SlateResultsDTO]
}
