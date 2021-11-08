package services

import models.User
import models.dto.{SlateLoadDTO, SlateSaveNewDTO}

import java.util.UUID
import scala.concurrent.Future

/**
 * Handles creating and editing slates, the raw questionnaires.
 */
trait SlateService {

  /**
   * Take in slate data and create new slate in database
   */
  def saveSlate(slateData: SlateSaveNewDTO): Future[UUID]

  /**
   * Return full slate info for given slate ID
   */
  def slateInfo(slateID: UUID): Future[Option[SlateLoadDTO]]

  /**
   * Return list of all slates, for viewing page of all slates. Filtering ability will be added later as needed.
   */
  def slateList(): Future[Seq[SlateLoadDTO]]

  /**
   * Retrieve all slates created by a given user
   */
  def slatesByUser(user:User): Future[Seq[SlateLoadDTO]]

  /**
   * Retrieve all slates based on input list of slate ids
   */
  def slatesFromList(slateIDs: Seq[UUID]): Future[Seq[SlateLoadDTO]]

}
