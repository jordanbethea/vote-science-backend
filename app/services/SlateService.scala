package services

import models.User
import models.dto.{SlateLoadDTO, SlateSaveDTO}

import scala.concurrent.Future

/**
 * Handles creating and editing slates, the raw questionnaires.
 */
trait SlateService {

  /**
   * Take in slate data and create new slate in database
   */
  def saveSlate(slateData: SlateSaveDTO): Future[Long]

  /**
   * Return full slate info for given slate ID
   */
  def slateInfo(slateID: Long): Future[Option[SlateLoadDTO]]

  /**
   * Return list of all slates, for viewing page of all slates. Filtering ability will be added later as needed.
   */
  def slateList(): Future[Seq[SlateLoadDTO]]

  /**
   *
   */
  def slatesByUser(user:User): Future[Seq[SlateLoadDTO]]

}
