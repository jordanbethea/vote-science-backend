package services

import models.dto.SlateResultsDTO

import scala.concurrent.Future

trait ResultsService {
  /**
   * Return general voting results for a given slate
   */
  def getSlateResults(slateID:Long): Future[SlateResultsDTO]
}
